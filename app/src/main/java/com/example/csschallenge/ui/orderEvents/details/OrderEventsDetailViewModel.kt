package com.example.csschallenge.ui.orderEvents.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.usecase.RetrieveOrderHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderEventsDetailViewModel @Inject constructor(
    private val retrieveOrderHistoryUseCase: RetrieveOrderHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderEventsDetailUiState>(OrderEventsDetailUiState.Loading)
    val uiState: StateFlow<OrderEventsDetailUiState> = _uiState

    private var currentOrderId: String = ""

    fun loadOrderDetail(orderId: String) {
        currentOrderId = orderId
        observeOrderDetail()
    }

    private fun observeOrderDetail() {
        viewModelScope.launch(errorHandler()) {
            retrieveOrderHistoryUseCase(currentOrderId).collect { orderHistory ->
                if (orderHistory.isEmpty()) {
                    _uiState.value = OrderEventsDetailUiState.NotFound
                    return@collect
                }

                updateUiState(orderHistory)
            }
        }
    }

    private fun updateUiState(orderHistory: List<DomainOrderEvent>) {
        val currentOrder = orderHistory.last() // Most recent event
        val changelog = createChangelog(orderHistory)

        val uiModel = OrderEventsDetailUiModel(
            currentOrder = currentOrder,
            orderHistory = orderHistory,
            changelog = changelog
        )

        _uiState.value = OrderEventsDetailUiState.Success(uiModel = uiModel)
    }

    private fun createChangelog(orderHistory: List<DomainOrderEvent>): List<OrderChangelogEntry> {
        val changelog = mutableListOf<OrderChangelogEntry>()

        orderHistory.forEachIndexed { index, event ->
            when (index) {
                0 -> {
                    // First event - order created
                    changelog.add(
                        OrderChangelogEntry(
                            timestamp = event.timestamp,
                            state = event.state,
                            shelf = event.shelf,
                            changeType = ChangeType.ORDER_CREATED,
                        )
                    )
                }

                else -> {
                    val previousEvent = orderHistory[index - 1]
                    val stateChanged = previousEvent.state != event.state
                    val shelfChanged = previousEvent.shelf != event.shelf

                    val changeType = when {
                        stateChanged && shelfChanged -> ChangeType.BOTH_CHANGED
                        stateChanged -> ChangeType.STATE_CHANGED
                        shelfChanged -> ChangeType.SHELF_CHANGED
                        else -> null // No changes
                    }

                    // Only add entries for actual changes
                    changeType?.let { type ->
                        changelog.add(
                            OrderChangelogEntry(
                                timestamp = event.timestamp,
                                state = event.state,
                                shelf = event.shelf,
                                changeType = type,
                            )
                        )
                    }
                }
            }
        }

        return changelog.reversed() // Most recent first
    }

    private fun errorHandler() = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = OrderEventsDetailUiState.NotFound
    }
}