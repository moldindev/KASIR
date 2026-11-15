package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.viewmodel.QuantityStep
import com.example.ui.viewmodel.RUPIAH_BANKNOTES
import com.example.util.formatRupiah
import kotlin.math.abs
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentSheet(
  cartItems: List<CartLineItem>,
  totalAmount: Long,
  quantityStep: QuantityStep,
  billCounts: Map<Long, Int>,
  isExactCash: Boolean,
  cashPaid: Long,
  changeAmount: Long,
  canCompleteSale: Boolean,
  isSaving: Boolean,
  onSelectQuantityStep: (QuantityStep) -> Unit,
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
      quantityStep = quantityStep,
      billCounts = billCounts,
      isExactCash = isExactCash,
      cashPaid = cashPaid,
      changeAmount = changeAmount,
      canCompleteSale = canCompleteSale,
      isSaving = isSaving,
      onSelectQuantityStep = onSelectQuantityStep,
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
          .padding(horizontal = 18.dp)
          .padding(bottom = 22.dp)
          .navigationBarsPadding(),
    )
  }
}

@Composable
fun CheckoutPaymentContent(
  cartItems: List<CartLineItem>,
  totalAmount: Long,
  quantityStep: QuantityStep,
  billCounts: Map<Long, Int>,
  isExactCash: Boolean,
  cashPaid: Long,
  changeAmount: Long,
  canCompleteSale: Boolean,
  isSaving: Boolean,
  onSelectQuantityStep: (QuantityStep) -> Unit,
  onIncrementItem: (CartLineItem) -> Unit,
  onDecrementItem: (Long) -> Unit,
  onClearCart: () -> Unit,
  onIncrementBill: (Long) -> Unit,
  onDecrementBill: (Long) -> Unit,
  onSelectExactCash: () -> Unit,
  onResetCash: () -> Unit,
  onCompleteSale: () -> Unit,
  modifier: Modifier = Modifier,
  lastAddedProductId: Long? = null,
  lastAddedEventId: Long = 0L,
) {
  val haptic = LocalHapticFeedback.current
  val cartScrollState = rememberScrollState()
  val knownProductIds = remember { cartItems.map { it.product.id }.toMutableSet() }
  var highlightedProductId by remember { mutableStateOf<Long?>(null) }
  var totalPulseActive by remember { mutableStateOf(false) }

  LaunchedEffect(cartItems) {
    val activeIds = cartItems.map { it.product.id }.toSet()
    knownProductIds.retainAll(activeIds)
  }

  LaunchedEffect(lastAddedEventId) {
    if (lastAddedEventId > 0L && lastAddedProductId != null) {
      highlightedProductId = lastAddedProductId
      totalPulseActive = true
      val index = cartItems.indexOfFirst { it.product.id == lastAddedProductId }
      if (index >= 0 && cartScrollState.maxValue > 0 && cartItems.size > 1) {
        val ratio = index.toFloat() / (cartItems.size - 1).coerceAtLeast(1)
        cartScrollState.animateScrollTo((cartScrollState.maxValue * ratio).toInt())
      }
      delay(850)
      highlightedProductId = null
      totalPulseActive = false
    }
  }

  val totalBgColor by
    animateColorAsState(
      targetValue =
        if (totalPulseActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.surfaceVariant,
      animationSpec = tween(durationMillis = 260),
      label = "checkoutTotalBg",
    )
  val totalBorderColor by
    animateColorAsState(
      targetValue =
        if (totalPulseActive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
      animationSpec = tween(durationMillis = 260),
      label = "checkoutTotalBorder",
    )

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Rincian & Pembayaran",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
      )
      if (cartItems.isNotEmpty()) {
        TextButton(
          onClick = onClearCart,
          modifier = Modifier.height(44.dp).testTag("clear_cart_button"),
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
          modifier = Modifier.padding(16.dp),
        )
      }
    } else {
      // Compact Cart Item Details with Fractional Step Selector + Split Left (−) / Right (+) Rows
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Rincian (Kiri − | Kanan +)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            QuantityStep.entries.forEach { step ->
              val selected = quantityStep == step
              Surface(
                onClick = { onSelectQuantityStep(step) },
                shape = RoundedCornerShape(10.dp),
                color =
                  if (selected) MaterialTheme.colorScheme.secondary
                  else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(34.dp).testTag("payment_step_${step.name}"),
              ) {
                Box(
                  modifier = Modifier.padding(horizontal = 10.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = step.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                      if (selected) MaterialTheme.colorScheme.onSecondary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }

        val isCartScrollable by remember { derivedStateOf { cartScrollState.maxValue > 0 } }
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(max = 220.dp)
              .clipToBounds()
              .smoothScrollStateVerticalScrollbar(
                state = cartScrollState,
                thumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeThumbColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
              )
              .verticalScroll(cartScrollState)
              .padding(end = if (isCartScrollable) 9.dp else 0.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          cartItems.forEach { item ->
            key(item.product.id) {
              val isNewEntry = remember(item.product.id) { knownProductIds.add(item.product.id) }
              CompactPaymentSplitItemCard(
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
              )
            }
          }
        }
      }

      // Total Belanja card
      Surface(
        shape = MaterialTheme.shapes.small,
        color = totalBgColor,
        border = BorderStroke(1.dp, totalBorderColor),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Total Belanja",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatRupiah(totalAmount),
            style = PriceTextLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      // Compact Zero-Typing Cash Received Section
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Uang Diterima",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
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

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (cashPaid > 0L) {
              OutlinedButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onResetCash()
                },
                modifier = Modifier.height(48.dp).testTag("reset_cash_button"),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 10.dp),
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
              contentPadding = PaddingValues(horizontal = 12.dp),
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
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val pairedDenominations = RUPIAH_BANKNOTES.dropLast(1).chunked(2)
        val topBanknote = RUPIAH_BANKNOTES.last()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          pairedDenominations.forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              pair.forEach { denom ->
                val count = billCounts[denom] ?: 0
                CompactSplitBanknoteCard(
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

          CompactSplitBanknoteCard(
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
        shape = MaterialTheme.shapes.small,
        color = bannerBg,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = bannerLabel,
            style = MaterialTheme.typography.titleSmall,
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

@Composable
private fun CompactPaymentSplitItemCard(
  item: CartLineItem,
  isNewEntry: Boolean,
  isHighlighted: Boolean,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
) {
  val cardShape = RoundedCornerShape(12.dp)
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
      targetValue = if (isHighlighted) 1.02f else 1f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "paymentItemScale",
    )
  val cardBgColor by
    animateColorAsState(
      targetValue =
        if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        else MaterialTheme.colorScheme.surface,
      animationSpec = tween(durationMillis = 250),
      label = "paymentItemBg",
    )
  val cardBorderColor by
    animateColorAsState(
      targetValue =
        if (isHighlighted) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.outline,
      animationSpec = tween(durationMillis = 250),
      label = "paymentItemBorder",
    )
  val cardBorderWidth by
    animateDpAsState(
      targetValue = if (isHighlighted) 2.dp else 1.dp,
      animationSpec = tween(durationMillis = 220),
      label = "paymentItemBorderWidth",
    )

  Surface(
    shape = cardShape,
    color = cardBgColor,
    border = BorderStroke(cardBorderWidth, cardBorderColor),
    modifier =
      Modifier.fillMaxWidth()
        .graphicsLayer {
          val p = slideProgress.value
          translationX = -(1f - p) * 48.dp.toPx()
          translationY = (1f - p) * 8.dp.toPx()
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

      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(30.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Remove,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
          Text(
            text = item.product.name,
            style = MaterialTheme.typography.titleSmall,
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

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondary,
        ) {
          Text(
            text = "×${item.formattedQuantity}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
          )
        }

        Text(
          text = formatRupiah(item.subtotal),
          style = PriceTextMedium,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
        )

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(30.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CompactSplitBanknoteCard(
  denomination: Long,
  count: Int,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSelected = count > 0
  val cardShape = RoundedCornerShape(12.dp)
  val borderColor =
    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
  val cardColor =
    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.surface

  Surface(
    shape = cardShape,
    color = cardColor,
    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
    modifier = modifier.height(52.dp).clip(cardShape),
  ) {
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
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
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "−",
          style = MaterialTheme.typography.titleMedium,
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
            style = PriceTextSmall,
            fontWeight = FontWeight.Bold,
            color =
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
          )
          if (isSelected) {
            Text(
              text = "×$count",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
        }

        Text(
          text = "+",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

private fun Modifier.smoothScrollStateVerticalScrollbar(
  state: ScrollState,
  thumbColor: Color,
  activeThumbColor: Color,
  trackColor: Color,
): Modifier = composed {
  val isScrollable by remember { derivedStateOf { state.maxValue > 0 } }
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
      label = "scrollStateAlpha",
    )
  val thumbWidthDp by
    animateDpAsState(
      targetValue = if (isScrolling) 5.dp else 3.5.dp,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "scrollStateWidth",
    )

  drawWithContent {
    drawContent()
    if (alpha <= 0.01f || state.maxValue <= 0) return@drawWithContent

    val widthPx = thumbWidthDp.toPx()
    val rightMarginPx = 1.dp.toPx()
    val verticalPadPx = 3.dp.toPx()
    val trackHeight = (size.height - verticalPadPx * 2f).coerceAtLeast(1f)
    val totalContentPx = size.height + state.maxValue.toFloat()
    val thumbFraction = (size.height / totalContentPx).coerceIn(0.16f, 1f)
    val minThumbPx = 24.dp.toPx().coerceAtMost(trackHeight)
    val thumbHeight = (trackHeight * thumbFraction).coerceIn(minThumbPx, trackHeight)
    val progress = (state.value.toFloat() / state.maxValue.toFloat()).coerceIn(0f, 1f)
    val thumbTop = verticalPadPx + (trackHeight - thumbHeight) * progress
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

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
