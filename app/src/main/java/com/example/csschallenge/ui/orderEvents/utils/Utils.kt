package com.example.csschallenge.ui.orderEvents.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatTimestamp(): String {
    return if (this > 0) {
        val formatter = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        formatter.format(Date(this))
    } else {
        "Unknown time"
    }
}

fun Int.formatCentsToDollars(): String {
    val decimalFormat = DecimalFormat("#,##0.00")
    return decimalFormat.format(this / 100.0)
}