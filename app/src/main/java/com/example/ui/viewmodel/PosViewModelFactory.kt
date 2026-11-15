package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.SembakoPosRepository

class PosViewModelFactory(private val repository: SembakoPosRepository) :
  ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when {
      modelClass.isAssignableFrom(CashierViewModel::class.java) ->
        CashierViewModel(repository) as T
      modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
        HistoryViewModel(repository) as T
      else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
