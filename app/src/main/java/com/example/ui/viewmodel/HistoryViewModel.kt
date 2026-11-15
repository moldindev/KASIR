package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SembakoPosRepository
import com.example.data.TransactionWithItems
import com.example.util.DateFilter
import com.example.util.getDayRangeMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HistoryUiState(
  val selectedFilter: DateFilter = DateFilter.TODAY,
  val totalRevenue: Long = 0L,
  val transactionCount: Int = 0,
  val averageOrder: Long = 0L,
  val transactions: List<TransactionWithItems> = emptyList(),
  val selectedReceipt: ReceiptData? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val repository: SembakoPosRepository) : ViewModel() {
  private val selectedFilter = MutableStateFlow(DateFilter.TODAY)
  private val selectedReceipt = MutableStateFlow<ReceiptData?>(null)

  private val transactionsFlow =
    selectedFilter.flatMapLatest { filter ->
      val (start, end) = getDayRangeMillis(filter)
      repository.observeTransactions(start, end)
    }

  val uiState: StateFlow<HistoryUiState> =
    combine(selectedFilter, transactionsFlow, selectedReceipt) { filter, txs, receipt ->
      val totalRevenue = txs.sumOf { it.transaction.totalAmount }
      val txCount = txs.size
      val average = if (txCount > 0) totalRevenue / txCount else 0L

      HistoryUiState(
        selectedFilter = filter,
        totalRevenue = totalRevenue,
        transactionCount = txCount,
        averageOrder = average,
        transactions = txs,
        selectedReceipt = receipt,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = HistoryUiState(),
    )

  fun selectFilter(filter: DateFilter) {
    selectedFilter.update { filter }
  }

  fun openReceipt(txWithItems: TransactionWithItems) {
    val tx = txWithItems.transaction
    selectedReceipt.value =
      ReceiptData(
        transactionId = tx.id,
        timestamp = tx.timestamp,
        items =
          txWithItems.items.map {
            ReceiptLineItem(
              name = it.productName,
              unit = it.unit,
              unitPrice = it.unitPrice,
              quantity = it.quantity,
            )
          },
        totalAmount = tx.totalAmount,
        cashPaid = tx.cashPaid,
        changeAmount = tx.changeAmount,
        isNewSaleConfirmation = false,
      )
  }

  fun dismissReceipt() {
    selectedReceipt.value = null
  }
}
