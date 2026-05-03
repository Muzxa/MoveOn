package com.moveon.app.data.remote.dto

data class VehicleDto(
    val vehicle_id: String = "",
    val provider_id: String = "",  
    val type: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val color: String = "",
    val plate_number: String = "",
    val current_lat: Double = 0.0,
    val current_lng: Double = 0.0,
    val max_capacity: Double = 0.0,  
    val max_volume: Double = 0.0,
    val base_rate: Double = 0.0,
    val rate_per_km: Double = 0.0,
    val is_available: Boolean = true
)