package com.example.csschallenge.ui.orderEvents.navigation

import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.csschallenge.ui.orderEvents.OrderEventsViewModel
import com.example.csschallenge.ui.orderEvents.details.navigation.navigateToOrderEventsDetail
import com.example.csschallenge.ui.orderEvents.details.navigation.orderEventsDetailScreen

@Composable
fun OrderEventsNavHost(
    viewModel: OrderEventsViewModel,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
    ) {
        NavHost(
            navController = navController,
            startDestination = OrderEventsScreenRoute,
        ) {
            orderEventsScreen(
                viewModel = viewModel,
                onOrderClick = { orderId ->
                    navController.navigate(navigateToOrderEventsDetail(orderId))
                }
            )

            orderEventsDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}