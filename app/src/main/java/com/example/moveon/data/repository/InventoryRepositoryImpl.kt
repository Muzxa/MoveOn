package com.example.moveon.data.repository

import androidx.room.Entity
import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.ItemEntity
import com.example.moveon.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val boxDao: BoxDao,
    private val itemDao: ItemDao
): InventoryRepository {
    override suspend fun addNewBox(box: BoxEntity){
        boxDao.insertBox(box)
    }

    override fun getBoxesForMove(bookingId: Int): Flow<List<BoxEntity>> {
        return boxDao.getBoxesForBooking(bookingId)
    }

    override fun getItemsInBox(boxId: String): Flow<List<ItemEntity>> {
        return itemDao.getItemInBox(boxId)
    }

    //ML Add item
    override suspend fun addItemToInventory(item: ItemEntity){
        itemDao.insertItem(item)
    }
}