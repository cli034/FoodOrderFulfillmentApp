package com.example.csschallenge.domain.usecase

import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.repositories.OrderRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RetrieveOrderEventsUseCase @Inject constructor(
    private val orderRepo: OrderRepo,
) {

    operator fun invoke(): Flow<List<DomainOrderEvent>> {
        return orderRepo.getOrderEventsFlow()
    }
}