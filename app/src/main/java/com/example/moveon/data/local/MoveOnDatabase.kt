package com.example.moveon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.ItemEntity

@Database(
    entities = [BoxEntity::class, ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MoveOnDatabase : RoomDatabase() {
    abstract fun boxDao(): BoxDao
    abstract fun itemDao(): ItemDao

    companion object{
        const val DATABASE_NAME = "moveon_db"
    }
}