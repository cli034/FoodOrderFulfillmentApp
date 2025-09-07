package com.example.csschallenge.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventShelf
import com.example.csschallenge.domain.model.OrderEventState

@Entity(tableName = "order_events")
data class OrderEventEntity(
    @PrimaryKey
    val uniqueKey: String, // Combination of orderId + timestamp for uniqueness
    val orderId: String,
    val state: String,
    val price: Int,
    val item: String,
    val customer: String,
    val shelf: String,
    val timestamp: Long,
    val destination: String
)

fun OrderEventEntity.toDomainOrderEvent(): DomainOrderEvent {
    return DomainOrderEvent(
        id = orderId,
        state = OrderEventState.valueOf(state),
        price = price,
        item = item,
        customer = customer,
        shelf = OrderEventShelf.valueOf(shelf),
        timestamp = timestamp,
        destination = destination
    )
}

fun DomainOrderEvent.toOrderEventEntity(): OrderEventEntity {
    return OrderEventEntity(
        uniqueKey = "${id}_${timestamp}", // Unique key combining order ID and timestamp
        orderId = id,
        state = state.name,
        price = price,
        item = item,
        customer = customer,
        shelf = shelf.name,
        timestamp = timestamp,
        destination = destination
    )
}