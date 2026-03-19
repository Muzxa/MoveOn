package com.example.moveon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey
    val user_id: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String,
    val role: String,
    val created_at: Long,
    val last_login_time: Long?,
    val last_synced_at: Long
)