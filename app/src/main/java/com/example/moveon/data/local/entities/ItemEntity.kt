package com.example.moveon.data.local.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
        tableName = "items",
        foreignKeys = [
            ForeignKey(
                entity = BoxEntity::class,
                parentColumns = ["box_id"],
                childColumns = ["box_id"],
                onDelete = ForeignKey.CASCADE
            )
        ]
)

data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val item_id: Int = 0,
    val box_id: String,
    val name: String,
    val description: String?,
    val image_url: String?,
    val is_fragile: Boolean
)