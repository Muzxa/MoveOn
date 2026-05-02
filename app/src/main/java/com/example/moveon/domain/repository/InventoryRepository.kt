package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun addNewBox(box: Box)
    suspend fun addNewBoxToCloud(box: Box, userId: String, colorHex: String): Result<Unit>
    suspend fun updateBoxPackedStatus(boxUuid: String, isPacked: Boolean)
    suspend fun updateBoxPackedStatusInCloud(
        boxUuid: String,
        userId: String,
        isPacked: Boolean
    ): Result<Unit>
    suspend fun updateBoxInfo(
        boxUuid: String,
        boxId: String,
        category: String,
        label: String
    )
    suspend fun updateBoxInfoInCloud(
        boxUuid: String,
        userId: String,
        boxId: String,
        category: String,
        label: String,
        colorHex: String
    ): Result<Unit>
    suspend fun deleteBox(boxUuid: String)
    suspend fun deleteBoxFromCloud(boxUuid: String, userId: String): Result<Unit>
    fun getBoxesForMove(bookingId: Int): Flow<List<Box>>
    fun getItemsInBox(boxId: String): Flow<List<Item>>
    fun getTotalItemsCount(): Flow<Int>
    fun getTotalFragileItemsCount(): Flow<Int>
    fun getItemCountsByBox(): Flow<Map<String, Int>>
    suspend fun addItemToInventory(item: Item)
    suspend fun updateItemInInventory(item: Item)
    suspend fun deleteItemFromInventory(item: Item)
    suspend fun getTotalBoxesCount(): Int
}