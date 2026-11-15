package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.PosDatabase
import com.example.data.SembakoPosRepository
import com.example.ui.viewmodel.CashierViewModel
import com.example.ui.viewmodel.ScannerMode
import com.example.util.DateFilter
import com.example.util.formatRupiah
import com.example.util.getDayRangeMillis
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var db: PosDatabase
  private lateinit var repository: SembakoPosRepository

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
  }

  @After
  fun tearDown() {
    db.close()
    Dispatchers.resetMain()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kasir Sembako", appName)
  }

  @Test
  fun `format rupiah formats Indonesian thousands separator`() {
    assertEquals("Rp 15.000", formatRupiah(15000L))
    assertEquals("Rp 0", formatRupiah(0L))
  }

  @Test
  fun `save product lookup by barcode and record sale updates today revenue`() = runTest {
    val productId =
      repository.saveProduct(
        name = "Minyak Goreng 1 L",
        price = 17500L,
        unit = "pouch",
        barcode = "8992761139018",
      )
    val found = repository.findProductByBarcode("8992761139018")
    assertNotNull(found)
    assertEquals(productId, found?.id)

    repository.recordSale(
      totalAmount = 35000L,
      cashPaid = 50000L,
      changeAmount = 15000L,
      items = listOf(found!! to 2),
    )

    val (start, end) = getDayRangeMillis(DateFilter.TODAY)
    val todaySales = repository.observeTransactions(start, end).first()
    assertEquals(1, todaySales.size)
    assertEquals(35000L, todaySales.first().transaction.totalAmount)
    assertEquals(15000L, todaySales.first().transaction.changeAmount)
    assertEquals(2, todaySales.first().items.first().quantity)
  }

  @Test
  fun `register product with scanned barcode and checkout via barcode scan`() = runTest {
    val viewModel = CashierViewModel(repository)
    val collectJob = launch { viewModel.uiState.collect {} }

    // 1. Register a product using PICK_FOR_PRODUCT scanner mode
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

    // 2. Ring up the registered product via CHECKOUT scanner mode
    viewModel.openCheckoutScanner()
    advanceUntilIdle()
    assertEquals(ScannerMode.CHECKOUT, viewModel.uiState.value.scannerMode)

    viewModel.onBarcodeScanned("8999999036614")
    val afterFirstScan = viewModel.uiState.first { it.totalItems == 1 }
    assertEquals(13500L, afterFirstScan.totalAmount)

    // Scanning again increments quantity
    viewModel.onBarcodeScanned("8999999036614")
    val afterSecondScan = viewModel.uiState.first { it.totalItems == 2 }
    assertEquals(27000L, afterSecondScan.totalAmount)

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

    // Corrupted check digit on EAN-13 must be rejected
    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139019",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 100L,
      )
    )

    // Single glitchy frame (even if valid check digit) should not trigger confirmation
    assertNull(
      stabilizer.onFrameDetected(
        rawValue = "8992761139025",
        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
        nowMillis = 150L,
      )
    )

    // 3 consecutive consistent frames confirm the real barcode
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

    // Within cooldown window, same barcode does not double-fire
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

    // Same physical barcode decoded once as 12-digit UPC-A and once as 13-digit EAN-13
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

    // Even if product was saved as 12-digit UPC-A, lookup with 13-digit EAN-13 finds it
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
}
