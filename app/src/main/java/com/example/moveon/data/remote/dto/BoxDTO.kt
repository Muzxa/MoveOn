package com.moveon.app.data.remote.dto

data class BoxDto(
    val box_uuid: String = "",
    val box_id: String = "",
    val booking_id: Int = 0,
    val vehicle_id: Int? = null,
    val category: String = "",
    val label: String = "",
    val volume: Double = 0.0,
    val packed: Boolean = false
)
