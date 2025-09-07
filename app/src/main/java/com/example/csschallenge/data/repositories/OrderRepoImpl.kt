package com.example.csschallenge.data.repositories

import android.util.Log
import com.example.csschallenge.data.database.OrderEventDao
import com.example.csschallenge.data.database.toOrderEventEntity
import com.example.csschallenge.data.database.toDomainOrderEvent
import com.example.csschallenge.data.service.OrderService
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.toDomainOrderEvent
import com.example.csschallenge.domain.repositories.OrderRepo
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class OrderRepoImpl @Inject constructor(
    private val orderService: OrderService,
    private val orderEventDao: OrderEventDao,
): OrderRepo {

    override fun getOrderEventsFlow(): Flow<List<DomainOrderEvent>> {
        // Start background polling and caching
        val serverPollingFlow = createServerPollingFlow()

        // Return latest orders from database
        return combine(
            serverPollingFlow,
            orderEventDao.getLatestOrderEventsFlow()
        ) { _, latestOrders ->
            latestOrders.map { it.toDomainOrderEvent() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getOrderHistoryFlow(orderId: String): Flow<List<DomainOrderEvent>> {
        return orderEventDao.getOrderHistoryFlow(orderId)
            .map { entities -> entities.map { it.toDomainOrderEvent() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun clearAllData() {
        orderEventDao.clearAllOrderEvents()
        Log.d("OrderRepoImpl", "All order data cleared from database")
    }

    private fun createServerPollingFlow(): Flow<Unit> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                val serverEvents = orderService.retrieveOrderEvents()
                    .map { it.toDomainOrderEvent() }

                Log.d("OrderRepoImpl", "Received ${serverEvents.size} events from server")

                // Cache all events in database
                if (serverEvents.isNotEmpty()) {
                    val entities = serverEvents.map { it.toOrderEventEntity() }
                    orderEventDao.insertOrderEvents(entities)
                    Log.d("OrderRepoImpl", "Cached ${entities.size} events in database")
                }

                emit(Unit)
                delay(2000) // Poll every 2 seconds
            } catch (e: Exception) {
                // Log error but continue polling - database will provide cached data
                Log.e("OrderRepoImpl", "Error fetching order events: ${e.message}", e)
                emit(Unit)
                delay(2000) // Still wait before next attempt
            }
        }
    }.flowOn(Dispatchers.IO)
}