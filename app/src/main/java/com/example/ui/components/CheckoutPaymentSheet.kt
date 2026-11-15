package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.PriceTextLarge
import com.example.ui.theme.PriceTextMedium
import com.example.ui.theme.PriceTextSmall
import com.example.ui.viewmodel.CartLineItem
import com.example.ui.viewmodel.RUPIAH_BANKNOTES
import com.example.util.formatRupiah
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentSheet(
  cartItems: List<CartLineItem>,
  totalAmount: Long,
  billCounts: Map<Long, Int>,
  isExactCash: Boolean,
  cashPaid: Long,
  changeAmount: Long,
  canCompleteSale: Boolean,
  isSaving: Boolean,
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onIncrementBill: (Long) -> Unit,
  onDecrementBill: (Long) -> Unit,
  onSelectExactCash: () -> Unit,
  onResetCash: () -> Unit,
  onCompleteSale: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.background,
  ) {
    CheckoutPaymentContent(
      cartItems = cartItems,
      totalAmount = totalAmount,
      billCounts = billCounts,
      isExactCash = isExactCash,
      cashPaid = cashPaid,
      changeAmount = changeAmount,
      canCompleteSale = canCompleteSale,
      isSaving = isSaving,
      onIncrementItem = onIncrementItem,
      onDecrementItem = onDecrementItem,
      onClearCart = onClearCart,
      onIncrementBill = onIncrementBill,
      onDecrementBill = onDecrementBill,
      onSelectExactCash = onSelectExactCash,
      onResetCash = onResetCash,
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

@Composable
fun CheckoutPaymentContent(
  cartItems: List<CartLineItem>,
  totalAmount: Long,
  billCounts: Map<Long, Int>,
  isExactCash: Boolean,
  cashPaid: Long,
  changeAmount: Long,
  canCompleteSale: Boolean,
  isSaving: Boolean,
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onIncrementBill: (Long) -> Unit,
  onDecrementBill: (Long) -> Unit,
  onSelectExactCash: () -> Unit,
  onResetCash: () -> Unit,
  onCompleteSale: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

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
        color = MaterialTheme.colorScheme.onBackground,
      )
      if (cartItems.isNotEmpty()) {
        TextButton(
          onClick = onClearCart,
          modifier = Modifier.height(48.dp).testTag("clear_cart_button"),
        ) {
          Icon(
            imageVector = Icons.Filled.DeleteSweep,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Kosongkan")
        }
      }
    }

    if (cartItems.isEmpty()) {
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Belum ada barang dipilih. Ketuk kartu barang atau scan barcode untuk memulai.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(18.dp),
        )
      }
    } else {
      // Cart Item Details with Split Left (−) / Right (+) Touch Zones
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Rincian Barang",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "Ketuk kiri −  |  Ketuk kanan +",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Column(
          modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          cartItems.forEach { item ->
            SplitCartItemCard(
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

      // Total Belanja card
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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

      // Zero-Typing Cash Received Section
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Uang Diterima",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = formatRupiah(cashPaid),
              style = PriceTextLarge,
              color =
                if (cashPaid > 0L) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.testTag("cash_paid_display"),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (cashPaid > 0L) {
              OutlinedButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onResetCash()
                },
                modifier = Modifier.height(48.dp).testTag("reset_cash_button"),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 12.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
              }
            }

            FilledTonalButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelectExactCash()
              },
              modifier = Modifier.height(48.dp).testTag("quick_cash_exact"),
              shape = MaterialTheme.shapes.small,
              colors =
                ButtonDefaults.filledTonalButtonColors(
                  containerColor =
                    if (isExactCash) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer,
                  contentColor =
                    if (isExactCash) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                ),
              contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
              if (isExactCash) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text("Uang Pas")
            }
          }
        }

        Text(
          text = "Ketuk kiri (−) kurangi lembar  |  Ketuk kanan (+) tambah lembar",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val pairedDenominations = RUPIAH_BANKNOTES.dropLast(1).chunked(2)
        val topBanknote = RUPIAH_BANKNOTES.last()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          pairedDenominations.forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              pair.forEach { denom ->
                val count = billCounts[denom] ?: 0
                SplitBanknoteCard(
                  denomination = denom,
                  count = count,
                  onDecrement = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDecrementBill(denom)
                  },
                  onIncrement = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onIncrementBill(denom)
                  },
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }

          SplitBanknoteCard(
            denomination = topBanknote,
            count = billCounts[topBanknote] ?: 0,
            onDecrement = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onDecrementBill(topBanknote)
            },
            onIncrement = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onIncrementBill(topBanknote)
            },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      // Live change display
      val (bannerBg, bannerFg, bannerLabel, bannerValue) =
        when {
          cashPaid == 0L ->
            Quadruple(
              MaterialTheme.colorScheme.surfaceContainer,
              MaterialTheme.colorScheme.onSurfaceVariant,
              "Kembalian",
              "Pilih pecahan atau Uang Pas",
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
            modifier = Modifier.testTag("change_amount_display"),
          )
        }
      }

      Button(
        onClick = onCompleteSale,
        enabled = canCompleteSale,
        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("complete_sale_button"),
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(
          text = if (isSaving) "Menyimpan..." else stringResource(R.string.action_complete_sale),
          style = MaterialTheme.typography.titleMedium,
        )
      }
    }
  }
}

@Composable
private fun SplitCartItemCard(
  item: CartLineItem,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
) {
  val cardShape = RoundedCornerShape(16.dp)
  Surface(
    shape = cardShape,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().clip(cardShape),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      // Split 50% Left (−) and 50% Right (+) hit zones
      Row(modifier = Modifier.matchParentSize()) {
        Box(
          modifier =
            Modifier.weight(1f)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
              .clickable(
                role = Role.Button,
                onClickLabel = "Kurangi ${item.product.name}",
                onClick = onDecrement,
              )
              .semantics { contentDescription = "Kurangi ${item.product.name}" }
              .testTag("cart_minus_${item.product.id}")
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
              .testTag("cart_plus_${item.product.id}")
        )
      }

      // Foreground clear visual layout
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Remove,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = item.product.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${formatRupiah(item.product.price)} / ${item.product.unit}",
            style = PriceTextSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondary,
          ) {
            Text(
              text = "×${item.quantity}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            )
          }
          Text(
            text = formatRupiah(item.subtotal),
            style = PriceTextMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SplitBanknoteCard(
  denomination: Long,
  count: Int,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSelected = count > 0
  val cardShape = RoundedCornerShape(16.dp)
  val borderColor =
    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
  val cardColor =
    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.surface

  Surface(
    shape = cardShape,
    color = cardColor,
    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
    modifier = modifier.height(64.dp).clip(cardShape),
  ) {
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
      // Split 50% Left (−1 sheet) and 50% Right (+1 sheet)
      Row(modifier = Modifier.matchParentSize()) {
        Box(
          modifier =
            Modifier.weight(1f)
              .fillMaxHeight()
              .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                else Color.Transparent
              )
              .clickable(
                role = Role.Button,
                onClickLabel = "Kurangi 1 lembar ${formatRupiah(denomination)}",
                onClick = onDecrement,
              )
              .semantics { contentDescription = "Kurangi pecahan ${formatRupiah(denomination)}" }
              .testTag("cash_minus_$denomination")
        )
        Box(
          modifier =
            Modifier.weight(1f)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f))
              .clickable(
                role = Role.Button,
                onClickLabel = "Tambah 1 lembar ${formatRupiah(denomination)}",
                onClick = onIncrement,
              )
              .semantics { contentDescription = "Tambah pecahan ${formatRupiah(denomination)}" }
              .testTag("cash_plus_$denomination")
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "−",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color =
            if (isSelected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Text(
            text = formatRupiah(denomination),
            style = PriceTextMedium,
            color =
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
          )
          if (isSelected) {
            Text(
              text = "×$count lembar",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
        }

        Text(
          text = "+",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
