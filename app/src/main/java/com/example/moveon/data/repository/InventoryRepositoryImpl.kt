package com.example.moveon.data.repository

import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toEntity
import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import com.example.moveon.domain.repository.InventoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val boxDao: BoxDao,
    private val itemDao: ItemDao,
    private val firestore: FirebaseFirestore
): InventoryRepository {
    
    override suspend fun addNewBox(box: Box) {
        boxDao.insertBox(box.toEntity())
    }

    override suspend fun addNewBoxToCloud(box: Box, userId: String, colorHex: String): Result<Unit> {
        return try {
            val payload = mapOf(
                "box_id" to box.id,
                "booking_id" to box.bookingId,
                "vehicle_id" to box.vehicleId,
                "category" to box.category,
                "label" to box.label,
                "volume" to box.volume,
                "qr_image_path" to box.qrImagePath,
                "color_hex" to colorHex,
                "created_at" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(box.id)
                .set(payload)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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