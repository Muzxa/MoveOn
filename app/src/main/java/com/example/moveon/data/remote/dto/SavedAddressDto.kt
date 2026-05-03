package com.example.moveon.data.remote.dto

data class SavedAddressDto(
    val address_id: String = "",
    val label: String = "",
    val address_line_1: String = "",
    val address_line_2: String = "",
    val city: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val is_default: Boolean = false,
    val updated_at: Long = 0L,
    val created_at: Long = 0L
)

