package com.example.moveon.domain.model

data class Booking(
    val id: String,
    val userId: String,
    val providerId: String,
    val status: BookingStatus,
    val pickupAddress: String,
    val dropOffAddress: String,
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val dropOffLat: Double = 0.0,
    val dropOffLng: Double = 0.0,
    val totalFare: Double,
    val otp: String,
    val isOtpVerified: Boolean,
    val createdAt: Long,
    val scheduledTime: Long,
    val rating: Float,
    val vehicles: List<BookingVehicle> = emptyList()
)