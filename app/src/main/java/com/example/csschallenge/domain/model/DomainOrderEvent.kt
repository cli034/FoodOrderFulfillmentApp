package com.example.csschallenge.domain.model

import com.example.csschallenge.data.model.NetworkOrderEvent
import com.example.csschallenge.ui.orderEvents.utils.formatCentsToDollars
import com.example.csschallenge.ui.orderEvents.utils.formatTimestamp
import java.text.DecimalFormat

data class DomainOrderEvent(
    val id: String,
    val state: OrderEventState,
    val price: Int,
    val item: String,
    val customer: String,
    val shelf: OrderEventShelf,
    val timestamp: Long,
    val destination: String,
) {
    val formattedPrice: String
        get() = price.formatCentsToDollars()
    val formattedTimestamp: String
        get() = timestamp.formatTimestamp()
}

fun NetworkOrderEvent.toDomainOrderEvent(): DomainOrderEvent {
    return DomainOrderEvent(
        id = id,
        state = OrderEventState.valueOf(state ?: OrderEventState.CANCELLED.name), // if state is null, assume the order is cancelled
        price = price ?: 0,
        item = item.orEmpty(),
        customer = customer.orEmpty(),
        shelf = OrderEventShelf.valueOf(shelf ?: OrderEventShelf.NONE.name),
        timestamp = timestamp ?: -1L,
        destination = destination.orEmpty()
    )
}

