package com.example.moveon.data.repository

import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toEntity
import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import com.example.moveon.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val boxDao: BoxDao,
    private val itemDao: ItemDao
): InventoryRepository {
    
    override suspend fun addNewBox(box: Box) {
        boxDao.insertBox(box.toEntity())
    }

    override fun getBoxesForMove(bookingId: String): Flow<List<Box>> {
        val bookingIdInt = bookingId.toIntOrNull() ?: 0
        return boxDao.getBoxesForBooking(bookingIdInt)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getItemsInBox(boxId: String): Flow<List<Item>> {
        return itemDao.getItemInBox(boxId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun addItemToInventory(item: Item) {
        itemDao.insertItem(item.toEntity())
    }

    override suspend fun getTotalBoxesCount(): Int {
        return boxDao.getTotalBoxesCount()
    }
}