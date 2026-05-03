package com.example.moveon.domain.model

data class Driver(
    val id: String,
    val providerId: String,
    val vehicleId: String,
    val licenseNo: String,
    val name: String = "",
    val phone: String = "",
    val cnic: String = "",
    val status: String = "Available", // "Available", "On Trip", "Off Duty"
    val rating: Double = 5.0,
    val tripsCount: Int = 0,
    val joinedDateMillis: Long = System.currentTimeMillis()
)
