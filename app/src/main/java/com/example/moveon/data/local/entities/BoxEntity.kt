package com.example.moveon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boxes")
data class BoxEntity (
    @PrimaryKey
    val box_id: String,
    val booking_id: Int,
    val vehicle_id: Int?,
    val category: String,
    val label: String,
    val volume: Double,
    val qr_image_path: String?
)