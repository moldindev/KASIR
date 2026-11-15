package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.PriceTextLarge
import com.example.ui.theme.PriceTextMedium
import com.example.ui.theme.PriceTextSmall
import com.example.ui.viewmodel.CartLineItem
import com.example.util.formatRupiah
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentSheet(
  cartItems: List<CartLineItem>,
  totalAmount: Long,
  cashInputText: String,
  cashPaid: Long,
  changeAmount: Long,
  canCompleteSale: Boolean,
  isSaving: Boolean,
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onCashInputChange: (String) -> Unit,
  onSelectQuickCash: (Long) -> Unit,
  onCompleteSale: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    CheckoutPaymentContent(
      cartItems = cartItems,
      totalAmount = totalAmount,
      cashInputText = cashInputText,
      cashPaid = cashPaid,
      changeAmount = changeAmount,
      canCompleteSale = canCompleteSale,
      isSaving = isSaving,
      onIncrementItem = onIncrementItem,
      onDecrementItem = onDecrementItem,
      onClearCart = onClearCart,
      onCashInputChange = onCashInputChange,
      onSelectQuickCash = onSelectQuickCash,
      onCompleteSale = onCompleteSale,
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutPaymentContent(
  cartItems: List<CartLineItem>,
  totalAmount: Long,
  cashInputText: String,
  cashPaid: Long,
  changeAmount: Long,
  canCompleteSale: Boolean,
  isSaving: Boolean,
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onCashInputChange: (String) -> Unit,
  onSelectQuickCash: (Long) -> Unit,
  onCompleteSale: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val quickCashValues = listOf(10000L, 20000L, 50000L, 100000L)

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Rincian & Pembayaran",
        style = MaterialTheme.typography.headlineSmall,
      )
      if (cartItems.isNotEmpty()) {
        TextButton(
          onClick = onClearCart,
          modifier = Modifier.testTag("clear_cart_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.DeleteSweep,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.size(4.dp))
          Text("Kosongkan")
        }
      }
    }

    if (cartItems.isEmpty()) {
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Belum ada barang dipilih. Ketuk kartu barang atau scan barcode untuk memulai.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(16.dp),
        )
      }
    } else {
      // Item list
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp).heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          cartItems.forEachIndexed { index, item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                  text = item.product.name,
                  style = MaterialTheme.typography.titleSmall,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${formatRupiah(item.product.price)} / ${item.product.unit}",
                  style = PriceTextSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                FilledTonalIconButton(
                  onClick = { onDecrementItem(item.product.id) },
                  modifier = Modifier.size(40.dp).testTag("cart_minus_${item.product.id}"),
                  colors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Kurangi ${item.product.name}",
                    modifier = Modifier.size(18.dp),
                  )
                }

                Box(
                  modifier =
                    Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                      .padding(horizontal = 10.dp, vertical = 4.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                }

                FilledTonalIconButton(
                  onClick = { onIncrementItem(item) },
                  modifier = Modifier.size(40.dp).testTag("cart_plus_${item.product.id}"),
                  colors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah ${item.product.name}",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            }
            if (index < cartItems.lastIndex) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
          }
        }
      }

      // Total Belanja card
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Total Belanja",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatRupiah(totalAmount),
            style = PriceTextLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      // Quick cash chips
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Uang Diterima",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          FilterChip(
            selected = cashPaid == totalAmount && totalAmount > 0L,
            onClick = { onSelectQuickCash(totalAmount) },
            label = { Text("Uang Pas") },
            modifier = Modifier.testTag("quick_cash_exact"),
          )
          quickCashValues.forEach { nominal ->
            FilterChip(
              selected = cashPaid == nominal,
              onClick = { onSelectQuickCash(nominal) },
              label = { Text(formatRupiah(nominal)) },
              modifier = Modifier.testTag("quick_cash_$nominal"),
            )
          }
        }
      }

      OutlinedTextField(
        value = cashInputText,
        onValueChange = onCashInputChange,
        label = { Text("Nominal Tunai") },
        prefix = { Text("Rp ") },
        placeholder = { Text("Ketik uang pembeli") },
        trailingIcon = {
          if (cashInputText.isNotEmpty()) {
            androidx.compose.material3.IconButton(onClick = { onCashInputChange("") }) {
              Icon(imageVector = Icons.Filled.Close, contentDescription = "Hapus nominal")
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("cash_input_field"),
        shape = MaterialTheme.shapes.small,
      )

      // Live change display
      val (bannerBg, bannerFg, bannerLabel, bannerValue) =
        when {
          cashPaid == 0L ->
            Quadruple(
              MaterialTheme.colorScheme.surfaceContainer,
              MaterialTheme.colorScheme.onSurfaceVariant,
              "Kembalian",
              "Masukkan uang pembeli",
            )
          changeAmount < 0L ->
            Quadruple(
              MaterialTheme.colorScheme.errorContainer,
              MaterialTheme.colorScheme.onErrorContainer,
              "Uang Kurang",
              formatRupiah(abs(changeAmount)),
            )
          else ->
            Quadruple(
              MaterialTheme.colorScheme.primaryContainer,
              MaterialTheme.colorScheme.onPrimaryContainer,
              "Kembalian",
              formatRupiah(changeAmount),
            )
        }

      Surface(
        shape = MaterialTheme.shapes.medium,
        color = bannerBg,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = bannerLabel,
            style = MaterialTheme.typography.titleMedium,
            color = bannerFg,
          )
          Text(
            text = bannerValue,
            style = if (cashPaid > 0L) PriceTextLarge else MaterialTheme.typography.bodyMedium,
            color = bannerFg,
          )
        }
      }

      Button(
        onClick = onCompleteSale,
        enabled = canCompleteSale,
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("complete_sale_button"),
        shape = MaterialTheme.shapes.small,
      ) {
        Text(
          text = if (isSaving) "Menyimpan..." else stringResource(R.string.action_complete_sale),
          style = MaterialTheme.typography.titleMedium,
        )
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
