package com.example.moveon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.local.dao.UserSessionDao
import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.ItemEntity
import com.example.moveon.data.local.entities.UserSessionEntity

@Database(
    entities = [BoxEntity::class, ItemEntity::class, UserSessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MoveOnDatabase : RoomDatabase() {
    abstract fun boxDao(): BoxDao
    abstract fun itemDao(): ItemDao
    abstract fun userSessionDao(): UserSessionDao

    companion object{
        const val DATABASE_NAME = "moveon_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_sessions` (
                        `user_id` TEXT NOT NULL,
                        `first_name` TEXT NOT NULL,
                        `last_name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `phone_number` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `last_login_time` INTEGER,
                        `last_synced_at` INTEGER NOT NULL,
                        PRIMARY KEY(`user_id`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}