package com.example.csschallenge.domain.repositories

import com.example.csschallenge.domain.model.DomainOrderEvent
import kotlinx.coroutines.flow.Flow

interface OrderRepo {

    fun getOrderEventsFlow(): Flow<List<DomainOrderEvent>>

    fun getOrderHistoryFlow(orderId: String): Flow<List<DomainOrderEvent>>

    suspend fun clearAllData()
}