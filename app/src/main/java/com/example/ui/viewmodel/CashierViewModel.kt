package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProductEntity
import com.example.data.SembakoPosRepository
import com.example.data.ShopPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val RUPIAH_BANKNOTES = listOf(1000L, 2000L, 5000L, 10000L, 20000L, 50000L, 100000L)

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
  val shopName: String = ShopPreferencesRepository.DEFAULT_SHOP_NAME,
  val isShopNameDialogOpen: Boolean = false,
  val allProducts: List<ProductEntity> = emptyList(),
  val filteredProducts: List<ProductEntity> = emptyList(),
  val searchQuery: String = "",
  val cartQuantities: Map<Long, Int> = emptyMap(),
  val cartItems: List<CartLineItem> = emptyList(),
  val totalAmount: Long = 0L,
  val totalItems: Int = 0,
  val isScannerOpen: Boolean = false,
  val scannerMode: ScannerMode = ScannerMode.CHECKOUT,
  val isSoundMuted: Boolean = false,
  val isPaymentSheetOpen: Boolean = false,
  val productEditorState: ProductEditorState? = null,
  val duplicateBarcodeProductName: String? = null,
  val billCounts: Map<Long, Int> = emptyMap(),
  val isExactCash: Boolean = false,
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
  val isSoundMuted: Boolean = false,
  val isPaymentSheetOpen: Boolean = false,
  val isShopNameDialogOpen: Boolean = false,
  val productEditorState: ProductEditorState? = null,
  val billCounts: Map<Long, Int> = emptyMap(),
  val isExactCash: Boolean = false,
  val isSavingSale: Boolean = false,
  val scanBannerMessage: String? = null,
  val completedReceipt: ReceiptData? = null,
)

class CashierViewModel(
  private val repository: SembakoPosRepository,
  private val shopPreferences: ShopPreferencesRepository,
) : ViewModel() {
  private val draftState = MutableStateFlow(CashierDraftState())

  val uiState: StateFlow<CashierUiState> =
    combine(repository.allProducts, shopPreferences.shopName, draftState) {
      products,
      shopName,
      draft ->
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
      val effectiveExactCash = draft.isExactCash && totalAmount > 0L
      val cashPaid =
        if (effectiveExactCash) {
          totalAmount
        } else {
          draft.billCounts.entries.sumOf { (denom, count) -> denom * count }
        }
      val changeAmount = cashPaid - totalAmount
      val canComplete = totalItems > 0 && cashPaid >= totalAmount && !draft.isSavingSale

      CashierUiState(
        shopName = shopName,
        isShopNameDialogOpen = draft.isShopNameDialogOpen,
        allProducts = products,
        filteredProducts = filtered,
        searchQuery = draft.searchQuery,
        cartQuantities = validQuantities,
        cartItems = cartItems,
        totalAmount = totalAmount,
        totalItems = totalItems,
        isScannerOpen = draft.isScannerOpen,
        scannerMode = draft.scannerMode,
        isSoundMuted = draft.isSoundMuted,
        isPaymentSheetOpen = draft.isPaymentSheetOpen,
        productEditorState = draft.productEditorState,
        duplicateBarcodeProductName = duplicateProduct?.name,
        billCounts = if (effectiveExactCash) emptyMap() else draft.billCounts,
        isExactCash = effectiveExactCash,
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

  fun openShopNameDialog() {
    draftState.update { it.copy(isShopNameDialogOpen = true) }
  }

  fun dismissShopNameDialog() {
    draftState.update { it.copy(isShopNameDialogOpen = false) }
  }

  fun saveShopName(newName: String) {
    draftState.update { it.copy(isShopNameDialogOpen = false) }
    viewModelScope.launch { shopPreferences.updateShopName(newName) }
  }

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
      val shouldResetExact = updated.isEmpty()
      state.copy(
        cartQuantities = updated,
        isExactCash = if (shouldResetExact) false else state.isExactCash,
      )
    }
  }

  fun clearCart() {
    draftState.update {
      it.copy(
        cartQuantities = emptyMap(),
        billCounts = emptyMap(),
        isExactCash = false,
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

  fun toggleSoundMute() {
    draftState.update { it.copy(isSoundMuted = !it.isSoundMuted) }
  }

  fun clearScanBanner() {
    draftState.update { it.copy(scanBannerMessage = null) }
  }

  private var isLookingUpBarcode = false

  fun onBarcodeScanned(rawCode: String) {
    val code = rawCode.filter { it.code in 33..126 }.trim()
    if (code.isEmpty()) return

    val snapshot = draftState.value
    if (!snapshot.isScannerOpen) return

    if (snapshot.scannerMode == ScannerMode.PICK_FOR_PRODUCT) {
      draftState.update { state ->
        if (!state.isScannerOpen || state.scannerMode != ScannerMode.PICK_FOR_PRODUCT) {
          return@update state
        }
        val updatedEditor =
          state.productEditorState?.copy(barcode = code) ?: ProductEditorState(barcode = code)
        state.copy(
          isScannerOpen = false,
          scanBannerMessage = null,
          productEditorState = updatedEditor,
        )
      }
      return
    }

    if (isLookingUpBarcode) return
    isLookingUpBarcode = true

    viewModelScope.launch {
      try {
        val existing = repository.findProductByBarcode(code)
        if (!draftState.value.isScannerOpen) return@launch
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
      } finally {
        isLookingUpBarcode = false
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

  fun incrementBill(denomination: Long) {
    if (denomination <= 0L) return
    draftState.update { state ->
      val baseCounts = if (state.isExactCash) emptyMap() else state.billCounts
      val nextCount = (baseCounts[denomination] ?: 0) + 1
      state.copy(
        isExactCash = false,
        billCounts = baseCounts + (denomination to nextCount),
      )
    }
  }

  fun decrementBill(denomination: Long) {
    if (denomination <= 0L) return
    draftState.update { state ->
      if (state.isExactCash) {
        return@update state.copy(isExactCash = false, billCounts = emptyMap())
      }
      val currentCount = state.billCounts[denomination] ?: 0
      val updated =
        if (currentCount <= 1) {
          state.billCounts - denomination
        } else {
          state.billCounts + (denomination to currentCount - 1)
        }
      state.copy(billCounts = updated)
    }
  }

  fun selectExactCash() {
    draftState.update {
      it.copy(
        isExactCash = true,
        billCounts = emptyMap(),
      )
    }
  }

  fun resetCash() {
    draftState.update {
      it.copy(
        isExactCash = false,
        billCounts = emptyMap(),
      )
    }
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
          billCounts = emptyMap(),
          isExactCash = false,
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
