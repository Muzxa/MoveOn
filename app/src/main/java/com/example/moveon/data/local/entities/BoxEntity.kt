package com.example.moveon.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "boxes",
    indices = [Index(value = ["box_id"], unique = true)]
)
data class BoxEntity (
    @PrimaryKey
    val box_uuid: String,
    val box_id: String,
    val booking_id: Int,
    val vehicle_id: Int?,
    val category: String,
    val label: String,
    val volume: Double,
    val packed: Boolean
)