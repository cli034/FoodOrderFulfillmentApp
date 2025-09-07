package com.example.csschallenge.domain.usecase

import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.repositories.OrderRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RetrieveOrderHistoryUseCase @Inject constructor(
    private val orderRepo: OrderRepo
) {
    operator fun invoke(orderId: String): Flow<List<DomainOrderEvent>> {
        return orderRepo.getOrderHistoryFlow(orderId)
    }
}