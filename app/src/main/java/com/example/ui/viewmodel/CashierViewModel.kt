package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProductEntity
import com.example.data.SembakoPosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScannerMode {
  CHECKOUT,
  PICK_FOR_PRODUCT,
}

data class CartLineItem(
  val product: ProductEntity,
  val quantity: Int,
) {
  val subtotal: Long
    get() = product.price * quantity
}

data class ProductEditorState(
  val existingId: Long? = null,
  val name: String = "",
  val priceText: String = "",
  val unit: String = "pcs",
  val barcode: String = "",
  val isFromUnknownCheckoutScan: Boolean = false,
  val addToCartOnSave: Boolean = false,
)

data class ReceiptData(
  val transactionId: Long,
  val timestamp: Long,
  val items: List<ReceiptLineItem>,
  val totalAmount: Long,
  val cashPaid: Long,
  val changeAmount: Long,
  val isNewSaleConfirmation: Boolean = false,
)

data class ReceiptLineItem(
  val name: String,
  val unit: String,
  val unitPrice: Long,
  val quantity: Int,
) {
  val subtotal: Long
    get() = unitPrice * quantity
}

data class CashierUiState(
  val allProducts: List<ProductEntity> = emptyList(),
  val filteredProducts: List<ProductEntity> = emptyList(),
  val searchQuery: String = "",
  val cartQuantities: Map<Long, Int> = emptyMap(),
  val cartItems: List<CartLineItem> = emptyList(),
  val totalAmount: Long = 0L,
  val totalItems: Int = 0,
  val isScannerOpen: Boolean = false,
  val scannerMode: ScannerMode = ScannerMode.CHECKOUT,
  val isPaymentSheetOpen: Boolean = false,
  val productEditorState: ProductEditorState? = null,
  val duplicateBarcodeProductName: String? = null,
  val cashInputText: String = "",
  val cashPaid: Long = 0L,
  val changeAmount: Long = 0L,
  val canCompleteSale: Boolean = false,
  val isSavingSale: Boolean = false,
  val scanBannerMessage: String? = null,
  val completedReceipt: ReceiptData? = null,
)

private data class CashierDraftState(
  val searchQuery: String = "",
  val cartQuantities: Map<Long, Int> = emptyMap(),
  val isScannerOpen: Boolean = false,
  val scannerMode: ScannerMode = ScannerMode.CHECKOUT,
  val isPaymentSheetOpen: Boolean = false,
  val productEditorState: ProductEditorState? = null,
  val cashInputText: String = "",
  val isSavingSale: Boolean = false,
  val scanBannerMessage: String? = null,
  val completedReceipt: ReceiptData? = null,
)

class CashierViewModel(private val repository: SembakoPosRepository) : ViewModel() {
  private val draftState = MutableStateFlow(CashierDraftState())

  val uiState: StateFlow<CashierUiState> =
    combine(repository.allProducts, draftState) { products, draft ->
      val productMap = products.associateBy { it.id }
      val validQuantities = draft.cartQuantities.filterKeys { productMap.containsKey(it) }
      val cartItems =
        validQuantities.mapNotNull { (productId, qty) ->
          val product = productMap[productId]
          if (product != null && qty > 0) CartLineItem(product, qty) else null
        }

      val query = draft.searchQuery.trim()
      val filtered =
        if (query.isEmpty()) {
          products
        } else {
          products.filter { item ->
            item.name.contains(query, ignoreCase = true) ||
              (item.barcode?.contains(query, ignoreCase = true) == true)
          }
        }

      val editorBarcode = draft.productEditorState?.barcode?.trim().orEmpty()
      val duplicateProduct =
        if (editorBarcode.isNotEmpty()) {
          products.firstOrNull {
            it.barcode == editorBarcode && it.id != draft.productEditorState?.existingId
          }
        } else {
          null
        }

      val totalAmount = cartItems.sumOf { it.subtotal }
      val totalItems = cartItems.sumOf { it.quantity }
      val cashPaid = draft.cashInputText.toLongOrNull() ?: 0L
      val changeAmount = cashPaid - totalAmount
      val canComplete = totalItems > 0 && cashPaid >= totalAmount && !draft.isSavingSale

      CashierUiState(
        allProducts = products,
        filteredProducts = filtered,
        searchQuery = draft.searchQuery,
        cartQuantities = validQuantities,
        cartItems = cartItems,
        totalAmount = totalAmount,
        totalItems = totalItems,
        isScannerOpen = draft.isScannerOpen,
        scannerMode = draft.scannerMode,
        isPaymentSheetOpen = draft.isPaymentSheetOpen,
        productEditorState = draft.productEditorState,
        duplicateBarcodeProductName = duplicateProduct?.name,
        cashInputText = draft.cashInputText,
        cashPaid = cashPaid,
        changeAmount = changeAmount,
        canCompleteSale = canComplete,
        isSavingSale = draft.isSavingSale,
        scanBannerMessage = draft.scanBannerMessage,
        completedReceipt = draft.completedReceipt,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = CashierUiState(),
    )

  fun onSearchQueryChange(query: String) {
    draftState.update { it.copy(searchQuery = query) }
  }

  fun addToCart(product: ProductEntity) {
    draftState.update { state ->
      val currentQty = state.cartQuantities[product.id] ?: 0
      state.copy(cartQuantities = state.cartQuantities + (product.id to currentQty + 1))
    }
  }

  fun decrementFromCart(productId: Long) {
    draftState.update { state ->
      val currentQty = state.cartQuantities[productId] ?: 0
      val updated =
        if (currentQty <= 1) {
          state.cartQuantities - productId
        } else {
          state.cartQuantities + (productId to currentQty - 1)
        }
      state.copy(cartQuantities = updated)
    }
  }

  fun clearCart() {
    draftState.update {
      it.copy(
        cartQuantities = emptyMap(),
        cashInputText = "",
        isPaymentSheetOpen = false,
      )
    }
  }

  fun openCheckoutScanner() {
    draftState.update {
      it.copy(
        isScannerOpen = true,
        scannerMode = ScannerMode.CHECKOUT,
        scanBannerMessage = null,
      )
    }
  }

  fun openProductBarcodeScanner() {
    draftState.update {
      it.copy(
        isScannerOpen = true,
        scannerMode = ScannerMode.PICK_FOR_PRODUCT,
        scanBannerMessage = null,
      )
    }
  }

  fun closeScanner() {
    draftState.update { it.copy(isScannerOpen = false, scanBannerMessage = null) }
  }

  fun clearScanBanner() {
    draftState.update { it.copy(scanBannerMessage = null) }
  }

  fun onBarcodeScanned(rawCode: String) {
    val code = rawCode.trim()
    if (code.isEmpty()) return

    val currentMode = draftState.value.scannerMode
    if (currentMode == ScannerMode.PICK_FOR_PRODUCT) {
      draftState.update { state ->
        val updatedEditor = state.productEditorState?.copy(barcode = code)
          ?: ProductEditorState(barcode = code)
        state.copy(
          isScannerOpen = false,
          scanBannerMessage = null,
          productEditorState = updatedEditor,
        )
      }
      return
    }

    viewModelScope.launch {
      val existing = repository.findProductByBarcode(code)
      if (existing != null) {
        draftState.update { state ->
          val currentQty = state.cartQuantities[existing.id] ?: 0
          val nextQty = currentQty + 1
          state.copy(
            cartQuantities = state.cartQuantities + (existing.id to nextQty),
            scanBannerMessage = "Ditambahkan: ${existing.name} (×$nextQty)",
          )
        }
      } else {
        draftState.update { state ->
          state.copy(
            isScannerOpen = false,
            scanBannerMessage = null,
            productEditorState =
              ProductEditorState(
                existingId = null,
                barcode = code,
                isFromUnknownCheckoutScan = true,
                addToCartOnSave = true,
              ),
          )
        }
      }
    }
  }

  fun openAddProduct() {
    draftState.update {
      it.copy(
        productEditorState =
          ProductEditorState(
            name = it.searchQuery.trim(),
            addToCartOnSave = false,
          )
      )
    }
  }

  fun openEditProduct(product: ProductEntity) {
    draftState.update {
      it.copy(
        productEditorState =
          ProductEditorState(
            existingId = product.id,
            name = product.name,
            priceText = product.price.toString(),
            unit = product.unit,
            barcode = product.barcode.orEmpty(),
            isFromUnknownCheckoutScan = false,
            addToCartOnSave = false,
          )
      )
    }
  }

  fun updateEditorName(name: String) {
    draftState.update { state ->
      state.copy(productEditorState = state.productEditorState?.copy(name = name))
    }
  }

  fun updateEditorPrice(rawInput: String) {
    val digits = rawInput.filter { it.isDigit() }.take(9)
    draftState.update { state ->
      state.copy(productEditorState = state.productEditorState?.copy(priceText = digits))
    }
  }

  fun updateEditorUnit(unit: String) {
    draftState.update { state ->
      state.copy(productEditorState = state.productEditorState?.copy(unit = unit))
    }
  }

  fun updateEditorBarcode(barcode: String) {
    draftState.update { state ->
      state.copy(productEditorState = state.productEditorState?.copy(barcode = barcode.trim()))
    }
  }

  fun updateEditorAddToCart(addToCart: Boolean) {
    draftState.update { state ->
      state.copy(productEditorState = state.productEditorState?.copy(addToCartOnSave = addToCart))
    }
  }

  fun dismissProductEditor() {
    draftState.update { it.copy(productEditorState = null) }
  }

  fun saveProductFromEditor() {
    val editor = draftState.value.productEditorState ?: return
    val cleanName = editor.name.trim()
    val parsedPrice = editor.priceText.toLongOrNull() ?: 0L
    if (cleanName.isEmpty() || parsedPrice <= 0L) return

    viewModelScope.launch {
      val cleanBarcode = editor.barcode.trim().ifBlank { null }
      if (cleanBarcode != null) {
        val existingWithBarcode = repository.findProductByBarcode(cleanBarcode)
        if (existingWithBarcode != null && existingWithBarcode.id != editor.existingId) {
          return@launch
        }
      }
      val savedId =
        repository.saveProduct(
          id = editor.existingId,
          name = cleanName,
          price = parsedPrice,
          unit = editor.unit,
          barcode = cleanBarcode,
        )
      draftState.update { state ->
        val nextCart =
          if (editor.addToCartOnSave && savedId > 0L) {
            val currentQty = state.cartQuantities[savedId] ?: 0
            state.cartQuantities + (savedId to currentQty + 1)
          } else {
            state.cartQuantities
          }
        state.copy(
          productEditorState = null,
          cartQuantities = nextCart,
        )
      }
    }
  }

  fun deleteProduct(productId: Long) {
    viewModelScope.launch {
      repository.deleteProduct(productId)
      draftState.update { state ->
        state.copy(
          productEditorState = null,
          cartQuantities = state.cartQuantities - productId,
        )
      }
    }
  }

  fun loadStarterSembakoProducts() {
    viewModelScope.launch { repository.loadStarterSembakoProducts() }
  }

  fun openPaymentSheet() {
    draftState.update { it.copy(isPaymentSheetOpen = true) }
  }

  fun dismissPaymentSheet() {
    draftState.update { it.copy(isPaymentSheetOpen = false) }
  }

  fun onCashInputChange(rawDigits: String) {
    val digitsOnly = rawDigits.filter { it.isDigit() }.take(10)
    draftState.update { it.copy(cashInputText = digitsOnly) }
  }

  fun setQuickCash(amount: Long) {
    draftState.update { it.copy(cashInputText = amount.toString()) }
  }

  fun completeSale() {
    val current = uiState.value
    if (!current.canCompleteSale) return

    draftState.update { it.copy(isSavingSale = true) }
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val itemsToSave = current.cartItems.map { it.product to it.quantity }
      val txId =
        repository.recordSale(
          totalAmount = current.totalAmount,
          cashPaid = current.cashPaid,
          changeAmount = current.changeAmount,
          items = itemsToSave,
        )
      val receipt =
        ReceiptData(
          transactionId = txId,
          timestamp = now,
          items =
            current.cartItems.map {
              ReceiptLineItem(
                name = it.product.name,
                unit = it.product.unit,
                unitPrice = it.product.price,
                quantity = it.quantity,
              )
            },
          totalAmount = current.totalAmount,
          cashPaid = current.cashPaid,
          changeAmount = current.changeAmount,
          isNewSaleConfirmation = true,
        )
      draftState.update {
        it.copy(
          cartQuantities = emptyMap(),
          cashInputText = "",
          isPaymentSheetOpen = false,
          isSavingSale = false,
          completedReceipt = receipt,
        )
      }
    }
  }

  fun dismissCompletedReceipt() {
    draftState.update { it.copy(completedReceipt = null) }
  }
}
