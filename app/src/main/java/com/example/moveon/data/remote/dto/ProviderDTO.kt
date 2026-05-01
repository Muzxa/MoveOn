package com.moveon.app.data.remote.dto

data class ProviderDto(
    val provider_id: String = "",
    val is_verified: Boolean = false,
    val establishment_name: String = "",
    val rating: Double = 0.0, // 1 to 5 stars
    val base_rate: Double = 0.0, // Fixed starting price
    val rate_per_km: Double = 0.0, // Fixed distance price
    val business_lat: Double = 0.0,
    val business_lng: Double = 0.0
)