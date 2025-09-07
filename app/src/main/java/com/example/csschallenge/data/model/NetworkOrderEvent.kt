package com.example.csschallenge.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkOrderEvent(
    val id: String,
    val state: String? = null,
    val price: Int? = null,
    val item: String? = null,
    val customer: String? = null,
    val shelf: String? = null,
    val timestamp: Long? = null,
    val destination: String? = null,
)
