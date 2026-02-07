package com.moveon.app.data.remote.dto

data class UserDto(
    val user_id: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val email: String = "",
    val phone_number: String = "",
    val role: String = "User", // Role can be "User", "Provider", or "Driver"
    val created_at: Long = 0L
)