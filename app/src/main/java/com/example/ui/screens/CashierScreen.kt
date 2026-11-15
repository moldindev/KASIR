package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ProductEntity
import com.example.ui.components.CheckoutPaymentContent
import com.example.ui.theme.PriceTextLarge
import com.example.ui.theme.PriceTextMedium
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
  onCashInputChange: (String) -> Unit,
  onSelectQuickCash: (Long) -> Unit,
  onCompleteSale: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideLayout = maxWidth >= 720.dp

    if (isWideLayout) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        CatalogColumn(
          uiState = uiState,
          onSearchQueryChange = onSearchQueryChange,
          onAddToCart = onAddToCart,
          onOpenScanner = onOpenScanner,
          onOpenAddProduct = onOpenAddProduct,
          onOpenEditProduct = onOpenEditProduct,
          onLoadStarterProducts = onLoadStarterProducts,
          bottomContentPadding = 24.dp,
          modifier = Modifier.weight(1.25f).fillMaxHeight(),
        )

        Surface(
          modifier = Modifier.weight(0.95f).fillMaxHeight().padding(vertical = 12.dp),
          shape = MaterialTheme.shapes.large,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          CheckoutPaymentContent(
            cartItems = uiState.cartItems,
            totalAmount = uiState.totalAmount,
            cashInputText = uiState.cashInputText,
            cashPaid = uiState.cashPaid,
            changeAmount = uiState.changeAmount,
            canCompleteSale = uiState.canCompleteSale,
            isSaving = uiState.isSavingSale,
            onIncrementItem = onIncrementCartItem,
            onDecrementItem = onDecrementCartItem,
            onClearCart = onClearCart,
            onCashInputChange = onCashInputChange,
            onSelectQuickCash = onSelectQuickCash,
            onCompleteSale = onCompleteSale,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
          )
        }
      }
    } else {
      Box(modifier = Modifier.fillMaxSize()) {
        CatalogColumn(
          uiState = uiState,
          onSearchQueryChange = onSearchQueryChange,
          onAddToCart = onAddToCart,
          onOpenScanner = onOpenScanner,
          onOpenAddProduct = onOpenAddProduct,
          onOpenEditProduct = onOpenEditProduct,
          onLoadStarterProducts = onLoadStarterProducts,
          bottomContentPadding = if (uiState.totalItems > 0) 96.dp else 16.dp,
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        )

        AnimatedVisibility(
          visible = uiState.totalItems > 0,
          enter = slideInVertically { it } + fadeIn(),
          exit = slideOutVertically { it } + fadeOut(),
          modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
          StickyCartBar(
            totalItems = uiState.totalItems,
            totalAmount = uiState.totalAmount,
            onClick = onOpenPaymentSheet,
          )
        }
      }
    }
  }
}

@Composable
private fun CatalogColumn(
  uiState: CashierUiState,
  onSearchQueryChange: (String) -> Unit,
  onAddToCart: (ProductEntity) -> Unit,
  onOpenScanner: () -> Unit,
  onOpenAddProduct: () -> Unit,
  onOpenEditProduct: (ProductEntity) -> Unit,
  onLoadStarterProducts: () -> Unit,
  bottomContentPadding: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Spacer(modifier = Modifier.height(12.dp))

    // Top Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = "Ketuk barang atau scan barcode",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(
          onClick = onOpenAddProduct,
          modifier = Modifier.height(48.dp).testTag("add_product_button"),
          shape = MaterialTheme.shapes.small,
          contentPadding = PaddingValues(horizontal = 14.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
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
          contentPadding = PaddingValues(horizontal = 14.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Scan")
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search bar
    OutlinedTextField(
      value = uiState.searchQuery,
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
        if (uiState.searchQuery.isNotEmpty()) {
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

    Spacer(modifier = Modifier.height(12.dp))

    when {
      uiState.allProducts.isEmpty() -> {
        EmptyCatalogState(
          onAddProduct = onOpenAddProduct,
          onLoadStarterProducts = onLoadStarterProducts,
          modifier = Modifier.fillMaxSize(),
        )
      }
      uiState.filteredProducts.isEmpty() -> {
        EmptySearchState(
          query = uiState.searchQuery,
          onClearSearch = { onSearchQueryChange("") },
          onAddProduct = onOpenAddProduct,
          modifier = Modifier.fillMaxSize(),
        )
      }
      else -> {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 154.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = bottomContentPadding),
          modifier = Modifier.fillMaxSize(),
        ) {
          items(uiState.filteredProducts, key = { it.id }) { product ->
            val qtyInCart = uiState.cartQuantities[product.id] ?: 0
            ProductGridCard(
              product = product,
              qtyInCart = qtyInCart,
              onTap = { onAddToCart(product) },
              onEditClick = { onOpenEditProduct(product) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ProductGridCard(
  product: ProductEntity,
  qtyInCart: Int,
  onTap: () -> Unit,
  onEditClick: () -> Unit,
) {
  val inCart = qtyInCart > 0
  Card(
    onClick = onTap,
    modifier = Modifier.fillMaxWidth().testTag("product_card_${product.id}"),
    shape = MaterialTheme.shapes.medium,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (inCart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
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
      modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
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
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
          if (!product.barcode.isNullOrBlank()) {
            Icon(
              imageVector = Icons.Filled.QrCode2,
              contentDescription = "Memiliki barcode",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        IconButton(
          onClick = onEditClick,
          modifier = Modifier.minimumInteractiveComponentSize().size(36.dp).testTag("edit_product_${product.id}"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Ubah ${product.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Text(
        text = product.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2,
        modifier = Modifier.padding(end = 6.dp),
      )

      Row(
        modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
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
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StickyCartBar(
  totalItems: Int,
  totalAmount: Long,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.primary,
    shadowElevation = 6.dp,
    modifier = Modifier.fillMaxWidth().testTag("sticky_cart_bar"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.ShoppingBasket,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        Column {
          Text(
            text = "$totalItems barang dipilih",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
          )
          Text(
            text = formatRupiah(totalAmount),
            style = PriceTextLarge,
            color = MaterialTheme.colorScheme.onPrimary,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondary,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = stringResource(R.string.action_pay),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondary,
          )
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(16.dp),
          )
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
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("empty_add_product_button"),
      shape = MaterialTheme.shapes.small,
    ) {
      Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Tambah Barang Pertama")
    }
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedButton(
      onClick = onLoadStarterProducts,
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("load_starter_products_button"),
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
    modifier = modifier.padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Filled.Search,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(44.dp),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Tidak ada barang cocok dengan \"$query\"",
      style = MaterialTheme.typography.titleMedium,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      OutlinedButton(
        onClick = onClearSearch,
        modifier = Modifier.height(48.dp),
        shape = MaterialTheme.shapes.small,
      ) {
        Text("Reset Cari")
      }
      Button(
        onClick = onAddProduct,
        modifier = Modifier.height(48.dp),
        shape = MaterialTheme.shapes.small,
      ) {
        Text("+ Tambah Barang")
      }
    }
  }
}
