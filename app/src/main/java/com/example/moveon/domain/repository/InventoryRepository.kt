package com.example.moveon.domain.repository
import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun addNewBox(box: BoxEntity)
    fun getBoxesForMove(bookingId: Int): Flow<List<BoxEntity>>
    fun getItemsInBox(box_id: String): Flow<List<ItemEntity>>
    suspend fun addItemToInventory(item: ItemEntity)
}