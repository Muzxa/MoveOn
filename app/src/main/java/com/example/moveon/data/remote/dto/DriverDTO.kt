package com.moveon.app.data.remote.dto

data class DriverDto(
    val driver_id: String = "",
    val provider_id: String = "",
    val vehicle_id: String = "",
    val license_no: String = "",
    val name: String = "",
    val phone: String = "",
    val cnic: String = "",
    val status: String = "Available",
    val rating: Double = 5.0,
    val trips_count: Int = 0,
    val joined_date_millis: Long = 0L
)