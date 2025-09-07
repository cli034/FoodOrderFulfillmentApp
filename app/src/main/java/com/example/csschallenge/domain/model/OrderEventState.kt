package com.example.csschallenge.domain.model

enum class OrderEventState {
    CREATED,
    COOKING,
    WAITING,
    DELIVERED,
    TRASHED,
    CANCELLED,
}