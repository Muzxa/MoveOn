package com.moveon.app.data.remote.dto

data class BookingDto(
    val booking_id: String = "",
    val user_id: String = "",
    val provider_id: String = "",
    val status: String = "Searching", // Confirmed, Active, or Completed
    val pickup_address: String = "",
    val dropoff_address: String = "",
    val pickup_lat: Double = 0.0,
    val pickup_lng: Double = 0.0,
    val dropoff_lat: Double = 0.0,
    val dropoff_lng: Double = 0.0,
    val total_fare: Double = 0.0,
    val otp: String = "",
    val otp_verified: Boolean = false,
    val created_at: Long = 0L,
    val scheduled_time: Long = 0L,
    val rating: Float = 0F,
    val vehicles: List<BookingVehicleDto> = emptyList()
)