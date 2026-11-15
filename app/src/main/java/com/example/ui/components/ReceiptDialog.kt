package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.PriceTextLarge
import com.example.ui.theme.PriceTextMedium
import com.example.ui.theme.PriceTextSmall
import com.example.ui.viewmodel.ReceiptData
import com.example.util.formatFullDateTime
import com.example.util.formatRupiah

@Composable
fun ReceiptDialog(
  receipt: ReceiptData,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector =
              if (receipt.isNewSaleConfirmation) Icons.Filled.CheckCircle
              else Icons.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
          )
          Column {
            Text(
              text =
                if (receipt.isNewSaleConfirmation) "Transaksi Berhasil"
                else "Struk #${receipt.transactionId.toString().padStart(4, '0')}",
              style = MaterialTheme.typography.titleLarge,
            )
            Text(
              text = formatFullDateTime(receipt.timestamp),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        DashedReceiptDivider()

        Column(
          modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          receipt.items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                  text = item.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${item.quantity} ${item.unit} × ${formatRupiah(item.unitPrice)}",
                  style = PriceTextSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = formatRupiah(item.subtotal),
                style = PriceTextSmall,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }

        DashedReceiptDivider()

        ReceiptSummaryRow("Total Belanja", formatRupiah(receipt.totalAmount), bold = true)
        ReceiptSummaryRow("Tunai", formatRupiah(receipt.cashPaid), bold = false)

        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Kembalian",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
              text = formatRupiah(receipt.changeAmount),
              style = PriceTextLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("receipt_done_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Text(
            text =
              if (receipt.isNewSaleConfirmation) stringResource(R.string.action_new_sale)
              else "Tutup"
          )
        }
      }
    }
  }
}

@Composable
private fun ReceiptSummaryRow(label: String, value: String, bold: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style =
        if (bold) MaterialTheme.typography.titleSmall
        else MaterialTheme.typography.bodyMedium,
      color =
        if (bold) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = if (bold) PriceTextMedium else PriceTextSmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun DashedReceiptDivider() {
  val lineColor = MaterialTheme.colorScheme.outline
  Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
    drawLine(
      color = lineColor,
      start = Offset(0f, size.height / 2f),
      end = Offset(size.width, size.height / 2f),
      strokeWidth = 1.5.dp.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
    )
  }
}
