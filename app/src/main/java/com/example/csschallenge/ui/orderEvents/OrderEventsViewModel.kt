package com.example.csschallenge.ui.orderEvents

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventState
import com.example.csschallenge.domain.usecase.ResetDataUseCase
import com.example.csschallenge.domain.usecase.RetrieveOrderEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderEventsViewModel @Inject constructor(
    private val retrieveOrderEventsUseCase: RetrieveOrderEventsUseCase,
    private val resetDataUseCase: ResetDataUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<OrderEventsUiState>(OrderEventsUiState.Loading)
    val uiState: StateFlow<OrderEventsUiState> = _uiState

    init {
        observeOrderEvents()
    }

    private fun observeOrderEvents() {
        viewModelScope.launch(errorHandler()) {
            retrieveOrderEventsUseCase().collect { orderEvents ->
                if (orderEvents.isEmpty()) {
                    // Kitchen is closed
                    _uiState.value = OrderEventsUiState.Empty
                    return@collect
                }

                updateUiState(orderEvents)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch(resetErrorHandler()) {
            Log.d("OrderEventsViewModel", "Resetting all data")
            _uiState.value = OrderEventsUiState.Loading

            // Clear all cached data in database
            resetDataUseCase()

            Log.d("OrderEventsViewModel", "Data reset completed")
        }
    }

    private fun updateUiState(currentOrders: List<DomainOrderEvent>) {
        val shelfItems = currentOrders.groupBy { it.shelf }
        val statistics = calculateStatistics(currentOrders)

        val uiModel = OrderEventsUiModel(
            shelfItems = shelfItems,
            statistics = statistics
        )

        _uiState.value = OrderEventsUiState.Success(
            uiModel = uiModel
        )
    }

    private fun calculateStatistics(orders: List<DomainOrderEvent>): OrderStatistics {
        val deliveredOrders = orders.filter { it.state == OrderEventState.DELIVERED }
        val trashedOrders = orders.filter { it.state == OrderEventState.TRASHED }

        val totalSales = deliveredOrders.sumOf { it.price } // Convert cents to dollars
        val totalWaste = trashedOrders.sumOf { it.price } // Convert cents to dollars
        val totalRevenue = totalSales - totalWaste

        return OrderStatistics(
            ordersTrashed = trashedOrders.size,
            ordersDelivered = deliveredOrders.size,
            totalSales = totalSales,
            totalWaste = totalWaste,
            totalRevenue = totalRevenue
        )
    }

    private fun errorHandler() = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = OrderEventsUiState.Error
    }

    private fun resetErrorHandler() = CoroutineExceptionHandler { _, throwable ->
        Log.e("OrderEventsViewModel", "Error refreshing data", throwable)
        _uiState.value = OrderEventsUiState.Error
    }
}