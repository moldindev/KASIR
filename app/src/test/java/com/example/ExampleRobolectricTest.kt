package com.example

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.PosDatabase
import com.example.data.ProductEntity
import com.example.data.SembakoPosRepository
import com.example.data.ShopPreferencesRepository
import com.example.ui.viewmodel.CashierViewModel
import com.example.ui.viewmodel.QuantityStep
import com.example.ui.viewmodel.ScannerMode
import com.example.util.DateFilter
import com.example.util.formatDecimalQuantity
import com.example.util.formatQuarterQuantity
import com.example.util.formatRupiah
import com.example.util.getDayRangeMillis
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val tempFolder = TemporaryFolder()

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var db: PosDatabase
  private lateinit var repository: SembakoPosRepository
  private lateinit var shopPreferences: ShopPreferencesRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, PosDatabase::class.java)
        .allowMainThreadQueries()
        .setTransactionExecutor(Runnable::run)
        .setQueryExecutor(Runnable::run)
        .build()
    repository = SembakoPosRepository(db.posDao())
    val prefsFile = File(tempFolder.newFolder(), "test_shop_prefs.preferences_pb")
    val dataStore = PreferenceDataStoreFactory.create(produceFile = { prefsFile })
    shopPreferences = ShopPreferencesRepository(dataStore)
  }

  @After
  fun tearDown() {
    db.close()
    Dispatchers.resetMain()
  }

  @Test
  fun `read string and raw beep sound resource from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kasir Sembako", appName)

    context.resources.openRawResource(R.raw.sfx_barcode_beep).use { stream ->
      val header = ByteArray(4)
      val read = stream.read(header)
      assertEquals(4, read)
      assertEquals("RIFF", String(header))
    }
  }

  @Test
  fun `format rupiah and fractional quantity helpers format cleanly`() {
    assertEquals("Rp 15.000", formatRupiah(15000L))
    assertEquals("Rp 0", formatRupiah(0L))
    assertEquals("1/4", formatQuarterQuantity(1))
    assertEquals("1/2", formatQuarterQuantity(2))
    assertEquals("3/4", formatQuarterQuantity(3))
    assertEquals("1", formatQuarterQuantity(4))
    assertEquals("1 1/2", formatQuarterQuantity(6))
    assertEquals("1/2", formatDecimalQuantity(0.5))
    assertEquals("2", formatDecimalQuantity(2.0))
  }

  @Test
  fun `save product lookup by barcode and record fractional sale updates today revenue`() = runTest {
    val productId =
      repository.saveProduct(
        name = "Telur Ayam Negeri",
        price = 28000L,
        unit = "kg",
        barcode = "8992761139018",
      )
    val found = repository.findProductByBarcode("8992761139018")
    assertNotNull(found)
    assertEquals(productId, found?.id)

    // Sell 1/2 kg Telur = Rp 14.000
    repository.recordSale(
      totalAmount = 14000L,
      cashPaid = 20000L,
      changeAmount = 6000L,
      items = listOf(found!! to 0.5),
    )

    val (start, end) = getDayRangeMillis(DateFilter.TODAY)
    val todaySales = repository.observeTransactions(start, end).first()
    assertEquals(1, todaySales.size)
    assertEquals(14000L, todaySales.first().transaction.totalAmount)
    assertEquals(6000L, todaySales.first().transaction.changeAmount)
    assertEquals(0.5, todaySales.first().items.first().quantity, 0.001)
  }

  @Test
  fun `fractional step checkout supports half and quarter units without typing`() = runTest {
    val telurId =
      repository.saveProduct(
        name = "Telur Ayam Negeri",
        price = 28000L,
        unit = "kg",
        barcode = null,
      )
    val telur = ProductEntity(id = telurId, name = "Telur Ayam Negeri", price = 28000L, unit = "kg", barcode = null)

    val viewModel = CashierViewModel(repository, shopPreferences)
    val collectJob = launch { viewModel.uiState.collect {} }

    // Select 1/2 step and tap Telur -> adds 1/2 kg (Rp 14.000)
    viewModel.selectQuantityStep(QuantityStep.HALF)
    viewModel.addToCart(telur)
    val afterHalf = viewModel.uiState.first { it.totalAmount == 14000L }
    assertEquals("1/2", afterHalf.cartItems.first().formattedQuantity)
    assertEquals(1, afterHalf.totalItems)

    // Switch to 1/4 step and add -> becomes 3/4 kg (Rp 21.000)
    viewModel.selectQuantityStep(QuantityStep.QUARTER)
    viewModel.addToCart(telur)
    val afterThreeQuarters = viewModel.uiState.first { it.totalAmount == 21000L }
    assertEquals("3/4", afterThreeQuarters.cartItems.first().formattedQuantity)

    // Pay with 20.000 + 2.000 = 22.000 -> change 1.000
    viewModel.incrementBill(20000L)
    viewModel.incrementBill(2000L)
    val readyState = viewModel.uiState.first { it.canCompleteSale }
    assertEquals(1000L, readyState.changeAmount)

    viewModel.completeSale()
    val receiptState = viewModel.uiState.first { it.completedReceipt != null }
    assertEquals(0.75, receiptState.completedReceipt!!.items.first().quantity, 0.001)
    assertEquals(21000L, receiptState.completedReceipt!!.totalAmount)

    collectJob.cancel()
  }

  @Test
  fun `register product with scanned barcode and checkout via barcode scan`() = runTest {
    val viewModel = CashierViewModel(repository, shopPreferences)
    val collectJob = launch { viewModel.uiState.collect {} }

    viewModel.openAddProduct()
    viewModel.updateEditorName("Kecap Manis 275ml")
    viewModel.updateEditorPrice("13500")
    viewModel.updateEditorUnit("botol")
    viewModel.openProductBarcodeScanner()
    advanceUntilIdle()
    assertEquals(ScannerMode.PICK_FOR_PRODUCT, viewModel.uiState.value.scannerMode)
    assertTrue(viewModel.uiState.value.isScannerOpen)

    viewModel.onBarcodeScanned("8999999036614")
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isScannerOpen)
    assertEquals("8999999036614", viewModel.uiState.value.productEditorState?.barcode)
    assertEquals("Kecap Manis 275ml", viewModel.uiState.value.productEditorState?.name)

    viewModel.saveProductFromEditor()
    val savedState = viewModel.uiState.first { it.allProducts.size == 1 && it.productEditorState == null }
    assertEquals(1, savedState.allProducts.size)

    viewModel.openCheckoutScanner()
    advanceUntilIdle()
    assertEquals(ScannerMode.CHECKOUT, viewModel.uiState.value.scannerMode)

    viewModel.onBarcodeScanned("8999999036614")
    val afterFirstScan = viewModel.uiState.first { it.totalItems == 1 }
    assertEquals(13500L, afterFirstScan.totalAmount)

    viewModel.onBarcodeScanned("8999999036614")
    val afterSecondScan = viewModel.uiState.first { it.totalItems == 2 }
    assertEquals(27000L, afterSecondScan.totalAmount)

    collectJob.cancel()
  }

  @Test
  fun `zero typing banknote counter increments decrements exact cash and shop name`() = runTest {
    val productId =
      repository.saveProduct(
        name = "Beras Pandan Wangi 5kg",
        price = 72000L,
        unit = "karung",
        barcode = null,
      )
    val product = ProductEntity(id = productId, name = "Beras Pandan Wangi 5kg", price = 72000L, unit = "karung", barcode = null)

    val viewModel = CashierViewModel(repository, shopPreferences)
    val collectJob = launch { viewModel.uiState.collect {} }

    viewModel.addToCart(product)
    val withCart = viewModel.uiState.first { it.totalAmount == 72000L }
    assertFalse(withCart.canCompleteSale)

    viewModel.incrementBill(50000L)
    viewModel.incrementBill(10000L)
    viewModel.incrementBill(10000L)
    viewModel.incrementBill(5000L)
    val afterBills = viewModel.uiState.first { it.cashPaid == 75000L }
    assertEquals(3000L, afterBills.changeAmount)
    assertTrue(afterBills.canCompleteSale)

    viewModel.decrementBill(10000L)
    val afterMinus = viewModel.uiState.first { it.cashPaid == 65000L }
    assertEquals(-7000L, afterMinus.changeAmount)
    assertFalse(afterMinus.canCompleteSale)

    viewModel.selectExactCash()
    val exactState = viewModel.uiState.first { it.isExactCash && it.cashPaid == 72000L }
    assertEquals(0L, exactState.changeAmount)
    assertTrue(exactState.canCompleteSale)

    viewModel.resetCash()
    val resetState = viewModel.uiState.first { !it.isExactCash && it.cashPaid == 0L }
    assertFalse(resetState.canCompleteSale)

    viewModel.saveShopName("Toko Berkah Ibu")
    val renamedState = viewModel.uiState.first { it.shopName == "Toko Berkah Ibu" }
    assertEquals("Toko Berkah Ibu", renamedState.shopName)

    collectJob.cancel()
  }

  @Test
  fun `barcode stabilizer rejects single-frame misread and confirms consistent barcode`() {
    val stabilizer =
      com.example.util.BarcodeStabilizer(
        requiredConsecutiveMatches = 3,
        matchWindowMillis = 900L,
        cooldownMillis = 1800L,
      )

    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139019",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 100L,
      )
    )

    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139025",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 150L,
      )
    )

    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139018",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 200L,
      )
    )
    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139018",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 250L,
      )
    )
    assertEquals(
      "8992761139018",
      stabilizer.onFrameDetected(
        rawValue = "8992761139018",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 300L,
      ),
    )

    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139018",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 800L,
      )
    )
  }

  @Test
  fun `upc-a and ean-13 leading zero variants normalize and resolve to same product`() = runTest {
    val stabilizer =
      com.example.util.BarcodeStabilizer(
        requiredConsecutiveMatches = 2,
        matchWindowMillis = 900L,
        cooldownMillis = 1500L,
      )

    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "036000291452\n",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A,
        nowMillis = 100L,
      )
    )
    val confirmedUpcAsEan13 =
      stabilizer.onFrameDetected(
        rawValue = "0036000291452",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 150L,
      )
    assertEquals("0036000291452", confirmedUpcAsEan13)

    val savedId =
      repository.saveProduct(
        name = "Tisu Kemasan",
        price = 12000L,
        unit = "pcs",
        barcode = "036000291452",
      )
    val foundFromEan13 = repository.findProductByBarcode("0036000291452")
    assertNotNull(foundFromEan13)
    assertEquals(savedId, foundFromEan13?.id)
  }

  @Test
  fun `adding items emits visual feedback event id and updates real-time total across long list`() =
    runTest {
      val products =
        (1..12).map { index ->
          val id =
            repository.saveProduct(
              name = "Barang Sembako $index",
              price = index * 1000L,
              unit = "pcs",
              barcode = null,
            )
          ProductEntity(
            id = id,
            name = "Barang Sembako $index",
            price = index * 1000L,
            unit = "pcs",
            barcode = null,
          )
        }

      val viewModel = CashierViewModel(repository, shopPreferences)
      val collectJob = launch { viewModel.uiState.collect {} }

      var expectedTotal = 0L
      products.forEachIndexed { idx, product ->
        viewModel.addToCart(product)
        expectedTotal += product.price
        val state = viewModel.uiState.first { it.lastAddedEventId == (idx + 1).toLong() }
        assertEquals(product.id, state.lastAddedProductId)
        assertEquals(expectedTotal, state.totalAmount)
        assertEquals(idx + 1, state.cartItems.size)
      }

      // Decrement should not advance lastAddedEventId, while total stays accurate
      viewModel.decrementFromCart(products.first().id)
      val afterDecrement = viewModel.uiState.first { it.totalAmount == expectedTotal - 1000L }
      assertEquals(12L, afterDecrement.lastAddedEventId)
      assertEquals(11, afterDecrement.cartItems.size)

      viewModel.clearCart()
      val cleared = viewModel.uiState.first { it.cartItems.isEmpty() }
      assertNull(cleared.lastAddedProductId)
      assertEquals(0L, cleared.totalAmount)

      collectJob.cancel()
    }
}
