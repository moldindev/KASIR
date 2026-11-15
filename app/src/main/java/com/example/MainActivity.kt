package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PosDatabase
import com.example.data.SembakoPosRepository
import com.example.ui.components.BarcodeScannerOverlay
import com.example.ui.components.CheckoutPaymentSheet
import com.example.ui.components.ProductEditorSheet
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.CashierScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.KasirSembakoTheme
import com.example.ui.viewmodel.CashierViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.PosViewModelFactory

private enum class PosTab(
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
) {
  CASHIER(
    labelRes = R.string.nav_kasir,
    selectedIcon = Icons.Filled.PointOfSale,
    unselectedIcon = Icons.Outlined.PointOfSale,
    testTag = "nav_kasir",
  ),
  HISTORY(
    labelRes = R.string.nav_riwayat,
    selectedIcon = Icons.Filled.ReceiptLong,
    unselectedIcon = Icons.Outlined.ReceiptLong,
    testTag = "nav_riwayat",
  ),
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = PosDatabase.getInstance(applicationContext)
    val repository = SembakoPosRepository(database.posDao())
    val factory = PosViewModelFactory(repository)

    setContent {
      KasirSembakoTheme {
        val cashierViewModel: CashierViewModel = viewModel(factory = factory)
        val historyViewModel: HistoryViewModel = viewModel(factory = factory)
        KasirSembakoApp(
          cashierViewModel = cashierViewModel,
          historyViewModel = historyViewModel,
        )
      }
    }
  }
}

@Composable
private fun KasirSembakoApp(
  cashierViewModel: CashierViewModel,
  historyViewModel: HistoryViewModel,
) {
  var currentTab by rememberSaveable { mutableStateOf(PosTab.CASHIER) }
  val cashierState by cashierViewModel.uiState.collectAsStateWithLifecycle()
  val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 640.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets.safeDrawing,
      bottomBar = {
        if (!useNavigationRail && !cashierState.isScannerOpen) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
          ) {
            PosTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = stringResource(tab.labelRes),
                  )
                },
                label = { Text(stringResource(tab.labelRes)) },
                modifier = Modifier.testTag(tab.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useNavigationRail && !cashierState.isScannerOpen) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            PosTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = stringResource(tab.labelRes),
                  )
                },
                label = { Text(stringResource(tab.labelRes)) },
                modifier = Modifier.testTag(tab.testTag),
              )
            }
          }
        }

        when (currentTab) {
          PosTab.CASHIER -> {
            CashierScreen(
              uiState = cashierState,
              onSearchQueryChange = cashierViewModel::onSearchQueryChange,
              onAddToCart = cashierViewModel::addToCart,
              onDecrementCartItem = cashierViewModel::decrementFromCart,
              onIncrementCartItem = { item -> cashierViewModel.addToCart(item.product) },
              onClearCart = cashierViewModel::clearCart,
              onOpenScanner = cashierViewModel::openCheckoutScanner,
              onOpenAddProduct = cashierViewModel::openAddProduct,
              onOpenEditProduct = cashierViewModel::openEditProduct,
              onLoadStarterProducts = cashierViewModel::loadStarterSembakoProducts,
              onOpenPaymentSheet = cashierViewModel::openPaymentSheet,
              onCashInputChange = cashierViewModel::onCashInputChange,
              onSelectQuickCash = cashierViewModel::setQuickCash,
              onCompleteSale = cashierViewModel::completeSale,
              modifier = Modifier.fillMaxSize(),
            )
          }
          PosTab.HISTORY -> {
            HistoryScreen(
              uiState = historyState,
              onSelectFilter = historyViewModel::selectFilter,
              onOpenReceipt = historyViewModel::openReceipt,
              onNavigateBackToCashier = { currentTab = PosTab.CASHIER },
              modifier = Modifier.fillMaxSize(),
            )
          }
        }
      }
    }

    // Fullscreen Barcode Scanner overlay
    if (cashierState.isScannerOpen) {
      BarcodeScannerOverlay(
        scannerMode = cashierState.scannerMode,
        totalItems = cashierState.totalItems,
        totalAmount = cashierState.totalAmount,
        scanBannerMessage = cashierState.scanBannerMessage,
        onBarcodeDetected = cashierViewModel::onBarcodeScanned,
        onClearBanner = cashierViewModel::clearScanBanner,
        onOpenPayment = cashierViewModel::openPaymentSheet,
        onClose = cashierViewModel::closeScanner,
      )
    }
  }

  // Modal Payment Sheet (compact view)
  if (cashierState.isPaymentSheetOpen) {
    CheckoutPaymentSheet(
      cartItems = cashierState.cartItems,
      totalAmount = cashierState.totalAmount,
      cashInputText = cashierState.cashInputText,
      cashPaid = cashierState.cashPaid,
      changeAmount = cashierState.changeAmount,
      canCompleteSale = cashierState.canCompleteSale,
      isSaving = cashierState.isSavingSale,
      onIncrementItem = { item -> cashierViewModel.addToCart(item.product) },
      onDecrementItem = cashierViewModel::decrementFromCart,
      onClearCart = cashierViewModel::clearCart,
      onCashInputChange = cashierViewModel::onCashInputChange,
      onSelectQuickCash = cashierViewModel::setQuickCash,
      onCompleteSale = cashierViewModel::completeSale,
      onDismiss = cashierViewModel::dismissPaymentSheet,
    )
  }

  // Add / Edit Product Sheet (hidden temporarily while picking barcode via camera)
  val editorState = cashierState.productEditorState
  if (editorState != null && !cashierState.isScannerOpen) {
    ProductEditorSheet(
      editorState = editorState,
      duplicateBarcodeProductName = cashierState.duplicateBarcodeProductName,
      onNameChange = cashierViewModel::updateEditorName,
      onPriceChange = cashierViewModel::updateEditorPrice,
      onUnitChange = cashierViewModel::updateEditorUnit,
      onBarcodeChange = cashierViewModel::updateEditorBarcode,
      onAddToCartToggle = cashierViewModel::updateEditorAddToCart,
      onScanBarcodeClick = cashierViewModel::openProductBarcodeScanner,
      onSave = cashierViewModel::saveProductFromEditor,
      onDelete = cashierViewModel::deleteProduct,
      onDismiss = cashierViewModel::dismissProductEditor,
    )
  }

  // Post-Checkout Receipt Confirmation
  cashierState.completedReceipt?.let { receipt ->
    ReceiptDialog(
      receipt = receipt,
      onDismiss = cashierViewModel::dismissCompletedReceipt,
    )
  }

  // History Receipt Detail
  historyState.selectedReceipt?.let { receipt ->
    ReceiptDialog(
      receipt = receipt,
      onDismiss = historyViewModel::dismissReceipt,
    )
  }
}
