package com.example.moveon.domain.model

data class Provider(
    val id: String,
    val establishmentName: String,
    val isVerified: Boolean,
    val rating: Double,
    val baseRate: Double,
    val ratePerKm: Double
)