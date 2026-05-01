package com.example.moveon.domain.model

data class Vehicle(
    val id: String,
    val providerId: String,
    val type: String,
    val make: String,
    val model: String,
    val plateNumber: String,
    val maxCapacityKg: Double,
    val maxVolumeKg: Double,
    val currentLat: Double,
    val currentLng: Double,
    val isAvailable: Boolean
)