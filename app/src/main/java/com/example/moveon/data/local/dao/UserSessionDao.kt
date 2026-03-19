package com.example.moveon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moveon.data.local.entities.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: UserSessionEntity)

    @Query("SELECT * FROM user_sessions WHERE user_id = :userId LIMIT 1")
    suspend fun getSessionById(userId: String): UserSessionEntity?

    @Query("SELECT * FROM user_sessions WHERE user_id = :userId LIMIT 1")
    fun observeSessionById(userId: String): Flow<UserSessionEntity?>

    @Query("DELETE FROM user_sessions")
    suspend fun clearAllSessions()
}