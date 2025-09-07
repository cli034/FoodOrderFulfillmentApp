package com.example.csschallenge.data.service

import com.example.csschallenge.data.model.NetworkOrderEvent
import com.example.csschallenge.data.model.OrderEventsResponse
import retrofit2.http.GET

interface OrderService {

    @GET("order_events")
    suspend fun retrieveOrderEvents(): List<NetworkOrderEvent>
}