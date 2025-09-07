package com.example.csschallenge.ui.orderEvents.details

sealed interface OrderEventsDetailUiState {

    data object Loading : OrderEventsDetailUiState

    data object NotFound : OrderEventsDetailUiState

    data class Success(
        val uiModel: OrderEventsDetailUiModel
    ) : OrderEventsDetailUiState
}