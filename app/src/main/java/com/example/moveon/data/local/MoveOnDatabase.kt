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
    version = 4,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("PRAGMA foreign_keys=OFF")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `boxes_new` (
                        `box_uuid` TEXT NOT NULL,
                        `box_id` TEXT NOT NULL,
                        `booking_id` INTEGER NOT NULL,
                        `vehicle_id` INTEGER,
                        `category` TEXT NOT NULL,
                        `label` TEXT NOT NULL,
                        `volume` REAL NOT NULL,
                        `packed` INTEGER NOT NULL,
                        PRIMARY KEY(`box_uuid`)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO `boxes_new` (
                        `box_uuid`,
                        `box_id`,
                        `booking_id`,
                        `vehicle_id`,
                        `category`,
                        `label`,
                        `volume`,
                        `packed`
                    )
                    SELECT
                        `box_id` AS `box_uuid`,
                        `box_id`,
                        `booking_id`,
                        `vehicle_id`,
                        `category`,
                        `label`,
                        `volume`,
                        0 AS `packed`
                    FROM `boxes`
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE `boxes`")
                database.execSQL("ALTER TABLE `boxes_new` RENAME TO `boxes`")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_boxes_box_id` ON `boxes` (`box_id`)"
                )

                database.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `items` ADD COLUMN `quantity` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }
}