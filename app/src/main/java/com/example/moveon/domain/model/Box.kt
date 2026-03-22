package com.example.moveon.domain.model

data class Box(
    val boxUuid: String,
    val boxId: String,
    val bookingId: Int,
    val vehicleId: Int? = null,
    val category: String,
    val label: String,
    val volume: Double,
    val packed: Boolean,
    val items: List<Item> = emptyList()
)