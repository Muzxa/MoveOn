package com.example.moveon.data.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moveon.data.local.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Query("SELECT * FROM items WHERE box_id = :boxId")
    fun getItemInBox(boxId: String): Flow<List<ItemEntity>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM items")
    fun getTotalItemsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM items WHERE is_fragile = 1")
    fun getTotalFragileItemsCount(): Flow<Int>

    @Query(
        """
        SELECT box_id AS boxId, COALESCE(SUM(quantity), 0) AS itemCount
        FROM items
        GROUP BY box_id
        """
    )
    fun getItemCountsByBox(): Flow<List<BoxItemCount>>

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)
}

data class BoxItemCount(
    val boxId: String,
    val itemCount: Int
)