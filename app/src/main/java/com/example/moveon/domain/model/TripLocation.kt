package com.example.moveon.domain.model

data class TripLocation(
    val bookingId: String,
    val actorId: String,
    val actorType: TripActorType,
    val vehicleId: String? = null,
    val lat: Double,
    val lng: Double,
    val speedMps: Double? = null,
    val headingDeg: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
