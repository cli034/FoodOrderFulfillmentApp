package com.example.csschallenge.ui.orderEvents

sealed interface OrderEventsUiState {

    data object Loading: OrderEventsUiState

    data object Empty: OrderEventsUiState

    data object Error: OrderEventsUiState

    data class Success(
        val uiModel: OrderEventsUiModel
    ): OrderEventsUiState
}