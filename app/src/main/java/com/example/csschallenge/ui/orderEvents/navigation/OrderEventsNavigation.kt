package com.example.csschallenge.ui.orderEvents.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.csschallenge.ui.orderEvents.OrderEventsScreen
import com.example.csschallenge.ui.orderEvents.OrderEventsViewModel

internal const val OrderEventsScreenRoute = "order_events_screen_route"

fun NavGraphBuilder.orderEventsScreen(
    viewModel: OrderEventsViewModel,
    onOrderClick: (String) -> Unit,
) {
    composable(
        route = OrderEventsScreenRoute,
    ) {
        OrderEventsScreen(
            onOrderClick = onOrderClick,
            viewModel = viewModel,
        )
    }
}