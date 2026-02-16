package com.example.moveon.domain.model

data class Box(
    val id: String,
    val bookingId: String,
    val vehicleId: String? = null,  
    val category: String,
    val label: String,
    val volume: Double,  
    val qrImagePath: String,
    val items: List<Item> = emptyList()
)