package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TransactionWithItems
import com.example.ui.theme.PriceTextMedium
import com.example.ui.theme.PriceTextSmall
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryDark
import com.example.ui.viewmodel.HistoryUiState
import com.example.util.DateFilter
import com.example.util.formatDecimalQuantity
import com.example.util.formatFullDateTime
import com.example.util.formatRupiah
import com.example.util.formatTimeOnly

@Composable
fun HistoryScreen(
  uiState: HistoryUiState,
  onSelectFilter: (DateFilter) -> Unit,
  onOpenReceipt: (TransactionWithItems) -> Unit,
  onNavigateBackToCashier: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler { onNavigateBackToCashier() }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().padding(horizontal = 16.dp),
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Riwayat & Omzet",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        text = "Ringkasan penjualan tersimpan lokal di perangkat",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        DateFilter.entries.forEach { filter ->
          FilterChip(
            selected = uiState.selectedFilter == filter,
            onClick = { onSelectFilter(filter) },
            label = { Text(filter.label) },
            modifier = Modifier.testTag("history_filter_${filter.name}"),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      RevenueSummaryCard(
        filterLabel = uiState.selectedFilter.label,
        totalRevenue = uiState.totalRevenue,
        transactionCount = uiState.transactionCount,
        averageOrder = uiState.averageOrder,
      )

      Spacer(modifier = Modifier.height(16.dp))

      if (uiState.transactions.isEmpty()) {
        EmptyHistoryState(modifier = Modifier.fillMaxSize())
      } else {
        Text(
          text = "Daftar Transaksi (${uiState.transactionCount})",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 20.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          items(uiState.transactions, key = { it.transaction.id }) { txWithItems ->
            TransactionHistoryCard(
              txWithItems = txWithItems,
              showFullDate = uiState.selectedFilter == DateFilter.ALL,
              onClick = { onOpenReceipt(txWithItems) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RevenueSummaryCard(
  filterLabel: String,
  totalRevenue: Long,
  transactionCount: Int,
  averageOrder: Long,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.large)
        .background(
          Brush.linearGradient(
            colors = listOf(SagePrimary, SagePrimaryDark)
          )
        )
        .padding(20.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Omzet $filterLabel",
          style = MaterialTheme.typography.labelLarge,
          color = Color.White.copy(alpha = 0.85f),
        )
        Text(
          text = formatRupiah(totalRevenue),
          style = MaterialTheme.typography.displaySmall,
          color = Color.White,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        SummaryMetricPill(
          label = "Transaksi",
          value = "$transactionCount",
          modifier = Modifier.weight(1f),
        )
        SummaryMetricPill(
          label = "Rata-rata",
          value = formatRupiah(averageOrder),
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun SummaryMetricPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = Color.White.copy(alpha = 0.14f),
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.8f),
      )
      Text(
        text = value,
        style = PriceTextMedium,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun TransactionHistoryCard(
  txWithItems: TransactionWithItems,
  showFullDate: Boolean,
  onClick: () -> Unit,
) {
  val tx = txWithItems.transaction
  val itemSummary =
    txWithItems.items.joinToString(", ") { "${formatDecimalQuantity(it.quantity)}× ${it.productName}" }.ifEmpty {
      "${tx.totalItems} barang"
    }

  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth().testTag("history_tx_${tx.id}"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Outlined.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
          )
        }
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text =
            if (showFullDate) formatFullDateTime(tx.timestamp)
            else "Pukul ${formatTimeOnly(tx.timestamp)} · ${tx.totalItems} barang",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = itemSummary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = formatRupiah(tx.totalAmount),
          style = PriceTextMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Kembali ${formatRupiah(tx.changeAmount)}",
          style = PriceTextSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.size(72.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Outlined.ReceiptLong,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = stringResource(R.string.empty_history_title),
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = stringResource(R.string.empty_history_desc),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
  }
}
