package com.example.csschallenge.testing.respositories

import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.repositories.OrderRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class TestOrderRepoImpl: OrderRepo {
    private val ordersFlow = MutableStateFlow<List<DomainOrderEvent>?>(null)

    fun setOrderEvents(events: List<DomainOrderEvent>) {
        ordersFlow.value = events
    }

    override fun getOrderEventsFlow(): Flow<List<DomainOrderEvent>> {
        return ordersFlow.filterNotNull()
    }

    override fun getOrderHistoryFlow(orderId: String): Flow<List<DomainOrderEvent>> {
        return ordersFlow.filterNotNull().map { events ->
            events.filter { event -> event.id == orderId }
        }
    }

    override suspend fun clearAllData() {
        ordersFlow.value = emptyList()
    }
}