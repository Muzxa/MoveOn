package com.example.moveon.domain.model

data class Provider(
    val id: String,
    val establishmentName: String,
    val isVerified: Boolean,
    val rating: Double,
    val businessLat: Double = 0.0,
    val businessLng: Double = 0.0,
    val phoneNumber: String? = null
)