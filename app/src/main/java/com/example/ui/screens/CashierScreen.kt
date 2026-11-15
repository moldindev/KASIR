package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.util.formatRupiah

@Composable
fun CashierScreen(
  uiState: CashierUiState,
  onSearchQueryChange: (String) -> Unit,
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
          Spacer(modifier = Modifier.height(10.dp))
          ProductCatalogPane(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onAddToCart = onAddToCart,
            onOpenAddProduct = onOpenAddProduct,
            onOpenEditProduct = onOpenEditProduct,
            onLoadStarterProducts = onLoadStarterProducts,
            compactCard = false,
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
            billCounts = uiState.billCounts,
            isExactCash = uiState.isExactCash,
            cashPaid = uiState.cashPaid,
            changeAmount = uiState.changeAmount,
            canCompleteSale = uiState.canCompleteSale,
            isSaving = uiState.isSavingSale,
            onIncrementItem = onIncrementCartItem,
            onDecrementItem = onDecrementCartItem,
            onClearCart = onClearCart,
            onIncrementBill = onIncrementBill,
            onDecrementBill = onDecrementBill,
            onSelectExactCash = onSelectExactCash,
            onResetCash = onResetCash,
            onCompleteSale = onCompleteSale,
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

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.allProducts.isEmpty()) {
          EmptyCatalogState(
            onAddProduct = onOpenAddProduct,
            onLoadStarterProducts = onLoadStarterProducts,
            modifier = Modifier.weight(1f).fillMaxWidth(),
          )
        } else {
          // Side-by-side layout: Left = Pilihan Barang, Right = Keranjang
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
                  text = "${uiState.filteredProducts.size}",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              ProductCatalogPane(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onAddToCart = onAddToCart,
                onOpenAddProduct = onOpenAddProduct,
                onOpenEditProduct = onOpenEditProduct,
                onLoadStarterProducts = onLoadStarterProducts,
                compactCard = true,
                modifier = Modifier.weight(1f).fillMaxWidth(),
              )
            }

            SideCartPane(
              cartItems = uiState.cartItems,
              totalItems = uiState.totalItems,
              totalAmount = uiState.totalAmount,
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
  compactCard: Boolean,
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
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 148.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = modifier,
      ) {
        items(uiState.filteredProducts, key = { it.id }) { product ->
          val qtyInCart = uiState.cartQuantities[product.id] ?: 0
          ProductGridCard(
            product = product,
            qtyInCart = qtyInCart,
            compactCard = compactCard,
            onTap = { onAddToCart(product) },
            onEditClick = { onOpenEditProduct(product) },
          )
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
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onOpenPaymentSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Header row
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

        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 4.dp),
        ) {
          items(cartItems, key = { it.product.id }) { item ->
            CompactSplitCartCard(
              item = item,
              onDecrement = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDecrementItem(item.product.id)
              },
              onIncrement = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onIncrementItem(item)
              },
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline)

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Total Belanja",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatRupiah(totalAmount),
            style = PriceTextMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
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
}

@Composable
private fun CompactSplitCartCard(
  item: CartLineItem,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
) {
  val cardShape = RoundedCornerShape(14.dp)
  Surface(
    shape = cardShape,
    color = MaterialTheme.colorScheme.background,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().clip(cardShape),
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
            color = MaterialTheme.colorScheme.secondary,
          ) {
            Text(
              text = "×${item.quantity}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondary,
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
private fun ProductGridCard(
  product: ProductEntity,
  qtyInCart: Int,
  compactCard: Boolean,
  onTap: () -> Unit,
  onEditClick: () -> Unit,
) {
  val inCart = qtyInCart > 0
  val padStart = if (compactCard) 10.dp else 14.dp
  val padBottom = if (compactCard) 10.dp else 14.dp

  Card(
    onClick = onTap,
    modifier = Modifier.fillMaxWidth().testTag("product_card_${product.id}"),
    shape = MaterialTheme.shapes.medium,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (inCart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
          else MaterialTheme.colorScheme.surface
      ),
    border =
      BorderStroke(
        width = if (inCart) 1.5.dp else 1.dp,
        color =
          if (inCart) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.outline,
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(start = padStart, end = 6.dp, top = 6.dp, bottom = padBottom),
      verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Text(
              text = product.unit,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
          }
          if (!product.barcode.isNullOrBlank()) {
            Icon(
              imageVector = Icons.Filled.QrCode2,
              contentDescription = "Memiliki barcode",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(15.dp),
            )
          }
        }

        IconButton(
          onClick = onEditClick,
          modifier = Modifier.minimumInteractiveComponentSize().size(34.dp).testTag("edit_product_${product.id}"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Ubah ${product.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp),
          )
        }
      }

      Text(
        text = product.name,
        style = if (compactCard) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2,
        modifier = Modifier.padding(end = 4.dp),
      )

      Row(
        modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = formatRupiah(product.price),
          style = PriceTextMedium,
          color = MaterialTheme.colorScheme.primary,
        )

        if (inCart) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondary,
          ) {
            Text(
              text = "×$qtyInCart",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
          }
        }
      }
    }
  }
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
