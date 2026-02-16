package com.example.moveon.domain.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: UserRole,
    val createdAt: Long,
    val lastLoginTime: Long? = null  
)