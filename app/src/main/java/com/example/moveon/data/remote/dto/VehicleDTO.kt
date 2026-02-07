package com.moveon.app.data.remote.dto

data class VehicleDto(
    val vehicle_id: String = "",
    val type: String = "",
    val plate_number: String = "",
    val current_lat: Double = 0.0, // Used for live GPS monitoring
    val current_lng: Double = 0.0,
    val max_capacity: Double = 0.0
)