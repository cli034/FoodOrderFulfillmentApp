package com.example.csschallenge.ui.orderEvents.details

import androidx.annotation.StringRes
import com.example.csschallenge.R
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventShelf
import com.example.csschallenge.domain.model.OrderEventState
import com.example.csschallenge.ui.orderEvents.utils.formatTimestamp

data class OrderEventsDetailUiModel(
    val currentOrder: DomainOrderEvent,
    val orderHistory: List<DomainOrderEvent>,
    val changelog: List<OrderChangelogEntry>,
) {
    companion object {
        fun preview(): OrderEventsDetailUiModel {
            val sampleOrder = DomainOrderEvent(
                id = "order-123",
                state = OrderEventState.COOKING,
                price = 1250,
                item = "Pizza Margherita",
                customer = "John Doe",
                shelf = OrderEventShelf.HOT,
                timestamp = System.currentTimeMillis(),
                destination = "123 Main St"
            )

            val sampleChangelog = listOf(
                OrderChangelogEntry(
                    timestamp = System.currentTimeMillis(),
                    state = OrderEventState.CREATED,
                    shelf = OrderEventShelf.NONE,
                    changeType = ChangeType.ORDER_CREATED,
                ),
                OrderChangelogEntry(
                    timestamp = System.currentTimeMillis(),
                    state = OrderEventState.COOKING,
                    shelf = OrderEventShelf.HOT,
                    changeType = ChangeType.BOTH_CHANGED,
                )
            )
            return OrderEventsDetailUiModel(
                currentOrder = sampleOrder,
                orderHistory = listOf(sampleOrder),
                changelog = sampleChangelog
            )
        }
    }
}

data class OrderChangelogEntry(
    val timestamp: Long,
    val state: OrderEventState,
    val shelf: OrderEventShelf,
    val changeType: ChangeType,
) {
    @get:StringRes
    val descriptionResId: Int
        get() = when (changeType) {
            ChangeType.ORDER_CREATED -> R.string.changelog_order_created
            ChangeType.STATE_CHANGED -> R.string.changelog_state_changed
            ChangeType.SHELF_CHANGED -> R.string.changelog_shelf_changed
            ChangeType.BOTH_CHANGED -> R.string.changelog_both_changed
        }

    val formattedTimestamp: String
        get() = timestamp.formatTimestamp()
}

enum class ChangeType {
    ORDER_CREATED,
    STATE_CHANGED,
    SHELF_CHANGED,
    BOTH_CHANGED,
}