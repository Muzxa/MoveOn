package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun addNewBox(box: Box)
    fun getBoxesForMove(bookingId: String): Flow<List<Box>>
    fun getItemsInBox(boxId: String): Flow<List<Item>>
    suspend fun addItemToInventory(item: Item)
}