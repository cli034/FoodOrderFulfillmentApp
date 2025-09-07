package com.example.csschallenge.ui.orderEvents.details.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.csschallenge.ui.orderEvents.details.OrderEventsDetailScreen

internal const val OrderEventsDetailRoute = "order_events_detail_route"
internal const val OrderIdArg = "order_Id_arg"
internal const val OrderEventsDetailRouteWithArgs = "$OrderEventsDetailRoute/{$OrderIdArg}"

fun NavGraphBuilder.orderEventsDetailScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = OrderEventsDetailRouteWithArgs,
        arguments = listOf(
            navArgument(OrderIdArg) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString(OrderIdArg) ?: ""
        OrderEventsDetailScreen(
            orderId = orderId,
            onBackClick = onBackClick
        )
    }
}

fun navigateToOrderEventsDetail(orderId: String): String {
    return "$OrderEventsDetailRoute/$orderId"
}