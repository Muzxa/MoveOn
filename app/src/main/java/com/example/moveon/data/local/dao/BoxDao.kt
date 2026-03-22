package com.example.moveon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moveon.data.local.entities.BoxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBox(box: BoxEntity)

    @Query("SELECT * FROM boxes WHERE box_id = :boxId")
    suspend fun getBoxById(boxId: String): BoxEntity?

    @Query("SELECT * FROM boxes WHERE booking_id = :bookingId")
    fun getBoxesForBooking(bookingId: Int): Flow<List<BoxEntity>>

    @Query("SELECT COUNT(*) FROM boxes")
    suspend fun getTotalBoxesCount(): Int

    @Query("UPDATE boxes SET packed = :isPacked WHERE box_uuid = :boxUuid")
    suspend fun updatePackedStatus(boxUuid: String, isPacked: Boolean)

    @Query(
        """
        UPDATE boxes
        SET box_id = :boxId,
            category = :category,
            label = :label
        WHERE box_uuid = :boxUuid
        """
    )
    suspend fun updateBoxInfo(
        boxUuid: String,
        boxId: String,
        category: String,
        label: String
    )

    @Query("DELETE FROM boxes WHERE box_uuid = :boxUuid")
    suspend fun deleteBoxByUuid(boxUuid: String)

    @Delete
    suspend fun deleteBox(box: BoxEntity)
}