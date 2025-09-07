package com.example.csschallenge.ui.orderEvents

import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventShelf
import com.example.csschallenge.domain.model.OrderEventState
import com.example.csschallenge.ui.orderEvents.utils.formatCentsToDollars
import java.text.DecimalFormat

data class OrderEventsUiModel(
    val shelfItems: Map<OrderEventShelf, List<DomainOrderEvent>>,
    val statistics: OrderStatistics
) {
    companion object {
        fun preview(): OrderEventsUiModel {
            val sampleShelfItems = mapOf(
                OrderEventShelf.HOT to listOf(
                    DomainOrderEvent(
                        id = "1",
                        state = OrderEventState.COOKING,
                        price = 1250,
                        item = "Pizza Margherita",
                        customer = "John Doe",
                        shelf = OrderEventShelf.HOT,
                        timestamp = System.currentTimeMillis(),
                        destination = "123 Main St"
                    ),
                    DomainOrderEvent(
                        id = "2",
                        state = OrderEventState.WAITING,
                        price = 850,
                        item = "Cheeseburger",
                        customer = "Jane Smith",
                        shelf = OrderEventShelf.HOT,
                        timestamp = System.currentTimeMillis(),
                        destination = "456 Oak Ave"
                    )
                ),
                OrderEventShelf.COLD to listOf(
                    DomainOrderEvent(
                        id = "3",
                        state = OrderEventState.COOKING,
                        price = 950,
                        item = "Caesar Salad",
                        customer = "Bob Johnson",
                        shelf = OrderEventShelf.COLD,
                        timestamp = System.currentTimeMillis(),
                        destination = "789 Pine St"
                    )
                ),
                OrderEventShelf.FROZEN to listOf(
                    DomainOrderEvent(
                        id = "4",
                        state = OrderEventState.WAITING,
                        price = 650,
                        item = "Ice Cream Sundae",
                        customer = "Alice Brown",
                        shelf = OrderEventShelf.FROZEN,
                        timestamp = System.currentTimeMillis(),
                        destination = "321 Elm St"
                    )
                )
            )

            return OrderEventsUiModel(
                shelfItems = sampleShelfItems,
                statistics = OrderStatistics.preview(),
            )
        }
    }
}

data class OrderStatistics(
    val ordersTrashed: Int,
    val ordersDelivered: Int,
    val totalSales: Int,
    val totalWaste: Int,
    val totalRevenue: Int,
) {
    val formattedTotalSales: String
        get() = totalSales.formatCentsToDollars()
    val formattedTotalWaste: String
        get() = totalWaste.formatCentsToDollars()
    val formattedTotalRevenue: String
        get() = totalRevenue.formatCentsToDollars()

    companion object {
        fun preview(): OrderStatistics {
            return OrderStatistics(
                ordersDelivered = 12,
                ordersTrashed = 2,
                totalSales = 12345,
                totalWaste = 2367,
                totalRevenue = 9978
            )
        }
    }
}


