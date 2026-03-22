package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun addNewBox(box: Box)
    suspend fun addNewBoxToCloud(box: Box, userId: String, colorHex: String): Result<Unit>
    fun getBoxesForMove(bookingId: String): Flow<List<Box>>
    fun getItemsInBox(boxId: String): Flow<List<Item>>
    suspend fun addItemToInventory(item: Item)
    suspend fun getTotalBoxesCount(): Int
}