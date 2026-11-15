package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ProductEntity
import com.example.ui.components.CheckoutPaymentContent
import com.example.ui.theme.PriceTextMedium
import com.example.ui.theme.PriceTextSmall
import com.example.ui.viewmodel.CartLineItem
import com.example.ui.viewmodel.CashierUiState
import com.example.ui.viewmodel.QuantityStep
import com.example.util.formatQuarterQuantity
import com.example.util.formatRupiah
import kotlinx.coroutines.delay

@Composable
fun CashierScreen(
  uiState: CashierUiState,
  onSearchQueryChange: (String) -> Unit,
  onSelectQuantityStep: (QuantityStep) -> Unit,
  onAddToCart: (ProductEntity) -> Unit,
  onDecrementCartItem: (Long) -> Unit,
  onIncrementCartItem: (CartLineItem) -> Unit,
  onClearCart: () -> Unit,
  onOpenScanner: () -> Unit,
  onOpenAddProduct: () -> Unit,
  onOpenEditProduct: (ProductEntity) -> Unit,
  onLoadStarterProducts: () -> Unit,
  onOpenPaymentSheet: () -> Unit,
  onIncrementBill: (Long) -> Unit,
  onDecrementBill: (Long) -> Unit,
  onSelectExactCash: () -> Unit,
  onResetCash: () -> Unit,
  onCompleteSale: () -> Unit,
  onEditShopNameClick: () -> Unit,
  onDismissShopNameDialog: () -> Unit,
  onSaveShopName: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideLayout = maxWidth >= 720.dp

    if (isWideLayout) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Column(modifier = Modifier.weight(1.25f).fillMaxHeight()) {
          Spacer(modifier = Modifier.height(12.dp))
          CashierHeaderRow(
            shopName = uiState.shopName,
            onEditShopNameClick = onEditShopNameClick,
            onOpenAddProduct = onOpenAddProduct,
            onOpenScanner = onOpenScanner,
          )
          Spacer(modifier = Modifier.height(10.dp))
          ProductSearchField(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
          )
          Spacer(modifier = Modifier.height(8.dp))
          QuantityStepSelectorRow(
            selectedStep = uiState.quantityStep,
            onSelectStep = onSelectQuantityStep,
          )
          Spacer(modifier = Modifier.height(6.dp))
          ProductCatalogPane(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onAddToCart = onAddToCart,
            onOpenAddProduct = onOpenAddProduct,
            onOpenEditProduct = onOpenEditProduct,
            onLoadStarterProducts = onLoadStarterProducts,
            modifier = Modifier.weight(1f).fillMaxWidth(),
          )
        }

        Surface(
          modifier = Modifier.weight(0.95f).fillMaxHeight().padding(vertical = 12.dp),
          shape = MaterialTheme.shapes.large,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          CheckoutPaymentContent(
            cartItems = uiState.cartItems,
            totalAmount = uiState.totalAmount,
            quantityStep = uiState.quantityStep,
            billCounts = uiState.billCounts,
            isExactCash = uiState.isExactCash,
            cashPaid = uiState.cashPaid,
            changeAmount = uiState.changeAmount,
            canCompleteSale = uiState.canCompleteSale,
            isSaving = uiState.isSavingSale,
            onSelectQuantityStep = onSelectQuantityStep,
            onIncrementItem = onIncrementCartItem,
            onDecrementItem = onDecrementCartItem,
            onClearCart = onClearCart,
            onIncrementBill = onIncrementBill,
            onDecrementBill = onDecrementBill,
            onSelectExactCash = onSelectExactCash,
            onResetCash = onResetCash,
            onCompleteSale = onCompleteSale,
            lastAddedProductId = uiState.lastAddedProductId,
            lastAddedEventId = uiState.lastAddedEventId,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
          )
        }
      }
    } else {
      Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        CashierHeaderRow(
          shopName = uiState.shopName,
          onEditShopNameClick = onEditShopNameClick,
          onOpenAddProduct = onOpenAddProduct,
          onOpenScanner = onOpenScanner,
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProductSearchField(
          searchQuery = uiState.searchQuery,
          onSearchQueryChange = onSearchQueryChange,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.allProducts.isEmpty()) {
          EmptyCatalogState(
            onAddProduct = onOpenAddProduct,
            onLoadStarterProducts = onLoadStarterProducts,
            modifier = Modifier.weight(1f).fillMaxWidth(),
          )
        } else {
          // Side-by-side layout: Left = Pilihan Barang (Compact rows + long-press edit), Right = Keranjang (unchanged card size)
          Row(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Column(
              modifier = Modifier.weight(1f).fillMaxHeight(),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Pilihan Barang",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                  text = "Tahan = edit",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              QuantityStepSelectorRow(
                selectedStep = uiState.quantityStep,
                onSelectStep = onSelectQuantityStep,
              )

              ProductCatalogPane(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onAddToCart = onAddToCart,
                onOpenAddProduct = onOpenAddProduct,
                onOpenEditProduct = onOpenEditProduct,
                onLoadStarterProducts = onLoadStarterProducts,
                modifier = Modifier.weight(1f).fillMaxWidth(),
              )
            }

            SideCartPane(
              cartItems = uiState.cartItems,
              totalItems = uiState.totalItems,
              totalAmount = uiState.totalAmount,
              lastAddedProductId = uiState.lastAddedProductId,
              lastAddedEventId = uiState.lastAddedEventId,
              onIncrementItem = onIncrementCartItem,
              onDecrementItem = onDecrementCartItem,
              onClearCart = onClearCart,
              onOpenPaymentSheet = onOpenPaymentSheet,
              modifier = Modifier.weight(1f).fillMaxHeight(),
            )
          }
        }
      }
    }
  }

  if (uiState.isShopNameDialogOpen) {
    ShopNameEditDialog(
      currentName = uiState.shopName,
      onDismiss = onDismissShopNameDialog,
      onSave = onSaveShopName,
    )
  }
}

@Composable
private fun QuantityStepSelectorRow(
  selectedStep: QuantityStep,
  onSelectStep: (QuantityStep) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(5.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "Beli:",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    QuantityStep.entries.forEach { step ->
      val selected = selectedStep == step
      Surface(
        onClick = { onSelectStep(step) },
        shape = RoundedCornerShape(10.dp),
        color =
          if (selected) MaterialTheme.colorScheme.secondary
          else MaterialTheme.colorScheme.surface,
        border =
          BorderStroke(
            width = 1.dp,
            color =
              if (selected) MaterialTheme.colorScheme.secondary
              else MaterialTheme.colorScheme.outline,
          ),
        modifier = Modifier.weight(1f).height(34.dp).testTag("step_chip_${step.name}"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = step.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color =
              if (selected) MaterialTheme.colorScheme.onSecondary
              else MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun CashierHeaderRow(
  shopName: String,
  onEditShopNameClick: () -> Unit,
  onOpenAddProduct: () -> Unit,
  onOpenScanner: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier =
        Modifier.weight(1f)
          .padding(end = 6.dp)
          .clip(RoundedCornerShape(14.dp))
          .clickable(
            role = Role.Button,
            onClickLabel = stringResource(R.string.action_edit_shop_name),
            onClick = onEditShopNameClick,
          )
          .padding(vertical = 4.dp, horizontal = 2.dp)
          .testTag("edit_shop_name_button"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_shop_mascot),
        contentDescription = "Logo $shopName",
        contentScale = ContentScale.Crop,
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
      )

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = shopName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = stringResource(R.string.action_edit_shop_name),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
          )
        }
        Text(
          text = "Ketuk barang atau scan",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      FilledTonalButton(
        onClick = onOpenAddProduct,
        modifier = Modifier.height(48.dp).testTag("add_product_button"),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 10.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text("Barang")
      }

      Button(
        onClick = onOpenScanner,
        modifier = Modifier.height(48.dp).testTag("scan_barcode_button"),
        shape = MaterialTheme.shapes.small,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
          ),
        contentPadding = PaddingValues(horizontal = 12.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.QrCodeScanner,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text("Scan")
      }
    }
  }
}

@Composable
private fun ProductSearchField(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
) {
  OutlinedTextField(
    value = searchQuery,
    onValueChange = onSearchQueryChange,
    placeholder = { Text(stringResource(R.string.search_placeholder)) },
    leadingIcon = {
      Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    trailingIcon = {
      if (searchQuery.isNotEmpty()) {
        IconButton(
          onClick = { onSearchQueryChange("") },
          modifier = Modifier.testTag("clear_search_button"),
        ) {
          Icon(imageVector = Icons.Filled.Close, contentDescription = "Hapus pencarian")
        }
      }
    },
    singleLine = true,
    shape = RoundedCornerShape(14.dp),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
      ),
    modifier = Modifier.fillMaxWidth().testTag("search_product_input"),
  )
}

@Composable
private fun ProductCatalogPane(
  uiState: CashierUiState,
  onSearchQueryChange: (String) -> Unit,
  onAddToCart: (ProductEntity) -> Unit,
  onOpenAddProduct: () -> Unit,
  onOpenEditProduct: (ProductEntity) -> Unit,
  onLoadStarterProducts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when {
    uiState.allProducts.isEmpty() -> {
      EmptyCatalogState(
        onAddProduct = onOpenAddProduct,
        onLoadStarterProducts = onLoadStarterProducts,
        modifier = modifier,
      )
    }
    uiState.filteredProducts.isEmpty() -> {
      EmptySearchState(
        query = uiState.searchQuery,
        onClearSearch = { onSearchQueryChange("") },
        onAddProduct = onOpenAddProduct,
        modifier = modifier,
      )
    }
    else -> {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = modifier,
      ) {
        items(uiState.filteredProducts, key = { it.id }) { product ->
          val quarterQtyInCart = uiState.cartQuantities[product.id] ?: 0
          CompactProductRowCard(
            product = product,
            quarterQtyInCart = quarterQtyInCart,
            onTap = { onAddToCart(product) },
            onLongPressEdit = { onOpenEditProduct(product) },
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactProductRowCard(
  product: ProductEntity,
  quarterQtyInCart: Int,
  onTap: () -> Unit,
  onLongPressEdit: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val inCart = quarterQtyInCart > 0
  val cardShape = RoundedCornerShape(12.dp)

  Surface(
    shape = cardShape,
    color =
      if (inCart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
      else MaterialTheme.colorScheme.surface,
    border =
      BorderStroke(
        width = if (inCart) 1.5.dp else 1.dp,
        color =
          if (inCart) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.outline,
      ),
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 52.dp)
        .clip(cardShape)
        .combinedClickable(
          onClick = onTap,
          onClickLabel = "Tambah ${product.name}",
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongPressEdit()
          },
          onLongClickLabel = "Ubah ${product.name}",
        )
        .testTag("product_card_${product.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f).padding(end = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "per ${product.unit}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (!product.barcode.isNullOrBlank()) {
            Icon(
              imageVector = Icons.Filled.QrCode2,
              contentDescription = "Memiliki barcode",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(13.dp),
            )
          }
        }
      }

      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = formatRupiah(product.price),
          style = PriceTextSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
        )
        if (inCart) {
          Surface(
            shape = RoundedCornerShape(7.dp),
            color = MaterialTheme.colorScheme.secondary,
          ) {
            Text(
              text = "×${formatQuarterQuantity(quarterQtyInCart)}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SideCartPane(
  cartItems: List<CartLineItem>,
  totalItems: Int,
  totalAmount: Long,
  lastAddedProductId: Long?,
  lastAddedEventId: Long,
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onOpenPaymentSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val listState = rememberLazyListState()
  val knownProductIds = remember { cartItems.map { it.product.id }.toMutableSet() }
  var highlightedProductId by remember { mutableStateOf<Long?>(null) }
  var addedFeedbackLabel by remember { mutableStateOf<String?>(null) }
  var totalPulseActive by remember { mutableStateOf(false) }

  LaunchedEffect(cartItems) {
    val activeIds = cartItems.map { it.product.id }.toSet()
    knownProductIds.retainAll(activeIds)
  }

  LaunchedEffect(lastAddedEventId) {
    if (lastAddedEventId > 0L && lastAddedProductId != null) {
      val targetIndex = cartItems.indexOfFirst { it.product.id == lastAddedProductId }
      val targetItem = if (targetIndex >= 0) cartItems[targetIndex] else null
      highlightedProductId = lastAddedProductId
      totalPulseActive = true
      addedFeedbackLabel =
        if (targetItem != null) {
          "+ ${targetItem.product.name} (×${targetItem.formattedQuantity})"
        } else {
          null
        }

      if (targetIndex >= 0) {
        val layoutInfo = listState.layoutInfo
        val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
        val isFullyVisible =
          visibleItem != null &&
            visibleItem.offset >= layoutInfo.viewportStartOffset &&
            visibleItem.offset + visibleItem.size <= layoutInfo.viewportEndOffset
        if (!isFullyVisible) {
          listState.animateScrollToItem(targetIndex)
        }
      }

      delay(900)
      highlightedProductId = null
      totalPulseActive = false
      addedFeedbackLabel = null
    }
  }

  LaunchedEffect(cartItems.isEmpty()) {
    if (cartItems.isEmpty()) {
      highlightedProductId = null
      totalPulseActive = false
      addedFeedbackLabel = null
    }
  }

  val badgeScale by
    animateFloatAsState(
      targetValue = if (totalPulseActive) 1.16f else 1f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "cartBadgeScale",
    )

  val totalScale by
    animateFloatAsState(
      targetValue = if (totalPulseActive) 1.04f else 1f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "cartTotalScale",
    )

  val totalCardColor by
    animateColorAsState(
      targetValue =
        if (totalPulseActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      animationSpec = tween(durationMillis = 280),
      label = "totalCardColor",
    )

  val totalCardBorderColor by
    animateColorAsState(
      targetValue =
        if (totalPulseActive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
      animationSpec = tween(durationMillis = 280),
      label = "totalCardBorderColor",
    )

  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f),
        ) {
          Text(
            text = "Keranjang",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (totalItems > 0) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.secondary,
              modifier =
                Modifier.graphicsLayer {
                  scaleX = badgeScale
                  scaleY = badgeScale
                },
            ) {
              Text(
                text = "$totalItems",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }

        if (cartItems.isNotEmpty()) {
          IconButton(
            onClick = onClearCart,
            modifier = Modifier.size(36.dp).testTag("clear_cart_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteSweep,
              contentDescription = "Kosongkan keranjang",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

      AnimatedVisibility(
        visible = addedFeedbackLabel != null,
        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
        exit = fadeOut(tween(240)) + shrinkVertically(tween(240)),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth().testTag("cart_added_feedback"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = addedFeedbackLabel.orEmpty(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      if (cartItems.isEmpty()) {
        Box(
          modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.size(48.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Outlined.ShoppingBasket,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(24.dp),
                )
              }
            }
            Text(
              text = "Belum ada barang",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
            )
            Text(
              text = "Ketuk barang di sisi kiri atau scan barcode",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        Text(
          text = "Kiri (−)  •  Kanan (+)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )

        val surfaceColor = MaterialTheme.colorScheme.surface
        val canScrollTop by remember { derivedStateOf { listState.canScrollBackward } }
        val canScrollBottom by remember { derivedStateOf { listState.canScrollForward } }

        Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
          LazyColumn(
            state = listState,
            modifier =
              Modifier.fillMaxSize()
                .smoothLazyVerticalScrollbar(
                  state = listState,
                  thumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  activeThumbColor = MaterialTheme.colorScheme.primary,
                  trackColor = MaterialTheme.colorScheme.outline,
                )
                .testTag("side_cart_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 9.dp, bottom = 4.dp),
          ) {
            items(cartItems, key = { it.product.id }) { item ->
              val isNewEntry = remember(item.product.id) { knownProductIds.add(item.product.id) }
              CompactSplitCartCard(
                item = item,
                isNewEntry = isNewEntry,
                isHighlighted = highlightedProductId == item.product.id,
                onDecrement = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDecrementItem(item.product.id)
                },
                onIncrement = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onIncrementItem(item)
                },
                modifier =
                  Modifier.animateItem(
                    fadeInSpec = null,
                    placementSpec =
                      spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy,
                      ),
                    fadeOutSpec = tween(durationMillis = 180),
                  ),
              )
            }
          }

          if (canScrollTop) {
            Box(
              modifier =
                Modifier.align(Alignment.TopCenter)
                  .fillMaxWidth()
                  .height(10.dp)
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(surfaceColor.copy(alpha = 0.9f), Color.Transparent),
                    )
                  )
            )
          }

          if (canScrollBottom) {
            Box(
              modifier =
                Modifier.align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .height(14.dp)
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(Color.Transparent, surfaceColor.copy(alpha = 0.92f)),
                    )
                  )
            )
          }
        }
      }

      // Sticky real-time Total & Bayar footer (always visible even for long cart lists)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = totalCardColor,
        border = BorderStroke(1.dp, totalCardBorderColor),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Total Belanja",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (cartItems.isNotEmpty()) {
              Text(
                text = "${cartItems.size} jenis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Text(
            text = formatRupiah(totalAmount),
            style = PriceTextMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
              Modifier.graphicsLayer {
                scaleX = totalScale
                scaleY = totalScale
              }.testTag("side_cart_total_amount"),
          )
        }
      }

      Button(
        onClick = onOpenPaymentSheet,
        enabled = totalItems > 0,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sticky_cart_bar"),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 10.dp),
      ) {
        Text(
          text = stringResource(R.string.action_pay),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

private fun Modifier.smoothLazyVerticalScrollbar(
  state: LazyListState,
  thumbColor: Color,
  activeThumbColor: Color,
  trackColor: Color,
): Modifier = composed {
  val isScrollable by remember {
    derivedStateOf { state.canScrollForward || state.canScrollBackward }
  }
  val isScrolling = state.isScrollInProgress

  val alpha by
    animateFloatAsState(
      targetValue =
        when {
          !isScrollable -> 0f
          isScrolling -> 0.92f
          else -> 0.45f
        },
      animationSpec = tween(durationMillis = 220),
      label = "lazyScrollbarAlpha",
    )

  val thumbWidthDp by
    animateDpAsState(
      targetValue = if (isScrolling) 5.dp else 3.5.dp,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "lazyScrollbarWidth",
    )

  val scrollMetrics by remember {
    derivedStateOf {
      val layoutInfo = state.layoutInfo
      val visibleItems = layoutInfo.visibleItemsInfo
      val totalItems = layoutInfo.totalItemsCount
      if (visibleItems.isEmpty() || totalItems <= 0) {
        0f to 1f
      } else {
        val viewportHeight =
          (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
        val avgItemSize =
          visibleItems.sumOf { it.size }.toFloat() / visibleItems.size.coerceAtLeast(1)
        val spacing = layoutInfo.mainAxisItemSpacing.toFloat()
        val estimatedTotalHeight =
          (avgItemSize * totalItems + spacing * (totalItems - 1).coerceAtLeast(0))
            .coerceAtLeast(viewportHeight)
        val thumbFraction = (viewportHeight / estimatedTotalHeight).coerceIn(0.14f, 1f)
        val maxScrollPx = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)
        val firstItem = visibleItems.first()
        val estimatedOffsetPx = (firstItem.index * (avgItemSize + spacing)) - firstItem.offset
        val progress =
          when {
            !state.canScrollBackward -> 0f
            !state.canScrollForward -> 1f
            else -> (estimatedOffsetPx / maxScrollPx).coerceIn(0f, 1f)
          }
        progress to thumbFraction
      }
    }
  }

  val animatedProgress by
    animateFloatAsState(
      targetValue = scrollMetrics.first,
      animationSpec =
        spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioNoBouncy),
      label = "lazyScrollbarProgress",
    )

  val animatedThumbFraction by
    animateFloatAsState(
      targetValue = scrollMetrics.second,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "lazyScrollbarThumbFraction",
    )

  drawWithContent {
    drawContent()
    if (alpha <= 0.01f) return@drawWithContent

    val widthPx = thumbWidthDp.toPx()
    val rightMarginPx = 1.dp.toPx()
    val verticalPadPx = 3.dp.toPx()
    val trackHeight = (size.height - verticalPadPx * 2f).coerceAtLeast(1f)
    val minThumbPx = 28.dp.toPx().coerceAtMost(trackHeight)
    val thumbHeight = (trackHeight * animatedThumbFraction).coerceIn(minThumbPx, trackHeight)
    val scrollableTrack = (trackHeight - thumbHeight).coerceAtLeast(0f)
    val thumbTop = verticalPadPx + scrollableTrack * animatedProgress
    val barX = size.width - widthPx - rightMarginPx
    val corner = CornerRadius(widthPx / 2f, widthPx / 2f)

    drawRoundRect(
      color = trackColor.copy(alpha = alpha * 0.30f),
      topLeft = Offset(barX, verticalPadPx),
      size = Size(widthPx, trackHeight),
      cornerRadius = corner,
    )

    val activeColor = if (isScrolling) activeThumbColor else thumbColor
    drawRoundRect(
      color = activeColor.copy(alpha = alpha),
      topLeft = Offset(barX, thumbTop),
      size = Size(widthPx, thumbHeight),
      cornerRadius = corner,
    )
  }
}

@Composable
private fun CompactSplitCartCard(
  item: CartLineItem,
  isNewEntry: Boolean,
  isHighlighted: Boolean,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val cardShape = RoundedCornerShape(14.dp)
  val slideProgress = remember(item.product.id) { Animatable(if (isNewEntry) 0f else 1f) }
  LaunchedEffect(item.product.id) {
    if (slideProgress.value < 1f) {
      slideProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          spring(
            dampingRatio = 0.80f,
            stiffness = Spring.StiffnessMediumLow,
          ),
      )
    }
  }

  val cardScale by
    animateFloatAsState(
      targetValue = if (isHighlighted) 1.025f else 1f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "cartCardScale",
    )
  val cardContainerColor by
    animateColorAsState(
      targetValue =
        if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.background,
      animationSpec = tween(durationMillis = 260),
      label = "cartCardBg",
    )
  val cardBorderColor by
    animateColorAsState(
      targetValue =
        if (isHighlighted) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.outline,
      animationSpec = tween(durationMillis = 260),
      label = "cartCardBorder",
    )
  val cardBorderWidth by
    animateDpAsState(
      targetValue = if (isHighlighted) 2.dp else 1.dp,
      animationSpec = tween(durationMillis = 220),
      label = "cartCardBorderWidth",
    )

  Surface(
    shape = cardShape,
    color = cardContainerColor,
    border = BorderStroke(cardBorderWidth, cardBorderColor),
    modifier =
      modifier
        .fillMaxWidth()
        .graphicsLayer {
          val p = slideProgress.value
          translationX = -(1f - p) * 52.dp.toPx()
          translationY = (1f - p) * 10.dp.toPx()
          alpha = p.coerceIn(0f, 1f)
          val entranceScale = 0.94f + 0.06f * p
          scaleX = entranceScale * cardScale
          scaleY = entranceScale * cardScale
        }
        .clip(cardShape),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      Row(modifier = Modifier.matchParentSize()) {
        Box(
          modifier =
            Modifier.weight(1f)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
              .clickable(
                role = Role.Button,
                onClickLabel = "Kurangi ${item.product.name}",
                onClick = onDecrement,
              )
              .semantics { contentDescription = "Kurangi ${item.product.name}" }
              .testTag("side_cart_minus_${item.product.id}")
        )
        Box(
          modifier =
            Modifier.weight(1f)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f))
              .clickable(
                role = Role.Button,
                onClickLabel = "Tambah ${item.product.name}",
                onClick = onIncrement,
              )
              .semantics { contentDescription = "Tambah ${item.product.name}" }
              .testTag("side_cart_plus_${item.product.id}")
        )
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Text(
            text = item.product.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 4.dp),
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color =
              if (isHighlighted) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.secondary,
          ) {
            Text(
              text = "×${item.formattedQuantity}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color =
                if (isHighlighted) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(24.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
              )
            }
          }

          Text(
            text = formatRupiah(item.subtotal),
            style = PriceTextSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
          )

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(24.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ShopNameEditDialog(
  currentName: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit,
) {
  var nameInput by remember(currentName) { mutableStateOf(currentName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = stringResource(R.string.action_edit_shop_name),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Nama toko akan tampil di layar kasir dan di bagian atas struk transaksi.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
          value = nameInput,
          onValueChange = { if (it.length <= 40) nameInput = it },
          label = { Text("Nama Toko") },
          placeholder = { Text("Contoh: Toko Berkah Jaya") },
          singleLine = true,
          shape = MaterialTheme.shapes.small,
          modifier = Modifier.fillMaxWidth().testTag("shop_name_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(nameInput) },
        enabled = nameInput.isNotBlank(),
        modifier = Modifier.height(48.dp).testTag("save_shop_name_button"),
        shape = MaterialTheme.shapes.small,
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.height(48.dp),
      ) {
        Text("Batal")
      }
    },
  )
}

@Composable
private fun EmptyCatalogState(
  onAddProduct: () -> Unit,
  onLoadStarterProducts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_empty_sembako),
      contentDescription = "Ilustrasi keranjang sembako",
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.fillMaxWidth(0.68f)
          .height(170.dp)
          .clip(MaterialTheme.shapes.large),
    )
    Spacer(modifier = Modifier.height(18.dp))
    Text(
      text = stringResource(R.string.empty_products_title),
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = stringResource(R.string.empty_products_desc),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(18.dp))
    Button(
      onClick = onAddProduct,
      modifier = Modifier.fillMaxWidth().height(50.dp).testTag("empty_add_product_button"),
      shape = MaterialTheme.shapes.small,
    ) {
      Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Tambah Barang Pertama")
    }
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedButton(
      onClick = onLoadStarterProducts,
      modifier = Modifier.fillMaxWidth().height(50.dp).testTag("load_starter_products_button"),
      shape = MaterialTheme.shapes.small,
    ) {
      Text(stringResource(R.string.action_load_starter))
    }
  }
}

@Composable
private fun EmptySearchState(
  query: String,
  onClearSearch: () -> Unit,
  onAddProduct: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Filled.Search,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(36.dp),
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = "Tidak ada \"$query\"",
      style = MaterialTheme.typography.titleSmall,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedButton(
      onClick = onClearSearch,
      modifier = Modifier.fillMaxWidth().height(48.dp),
      shape = MaterialTheme.shapes.small,
    ) {
      Text("Reset Cari")
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
      onClick = onAddProduct,
      modifier = Modifier.fillMaxWidth().height(48.dp),
      shape = MaterialTheme.shapes.small,
    ) {
      Text("+ Barang")
    }
  }
}
