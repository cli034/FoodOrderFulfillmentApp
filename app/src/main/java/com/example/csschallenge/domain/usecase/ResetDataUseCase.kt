package com.example.csschallenge.domain.usecase

import com.example.csschallenge.domain.repositories.OrderRepo
import javax.inject.Inject

class ResetDataUseCase @Inject constructor(
    private val orderRepo: OrderRepo
) {
    suspend operator fun invoke() {
        orderRepo.clearAllData()
    }
}