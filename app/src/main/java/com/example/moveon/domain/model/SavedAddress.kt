package com.example.moveon.domain.model

data class SavedAddress(
    val id: String,
    val label: String,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val lat: Double,
    val lng: Double,
    val isDefault: Boolean,
    val updatedAt: Long,
    val createdAt: Long
)

