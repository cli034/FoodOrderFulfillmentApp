package com.example.csschallenge.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderEventsResponse(
    val orderEvents: List<NetworkOrderEvent>
)
