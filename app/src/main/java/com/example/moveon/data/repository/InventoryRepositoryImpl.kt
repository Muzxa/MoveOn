package com.example.moveon.data.repository

import com.example.moveon.data.local.dao.BoxDao
import com.example.moveon.data.local.dao.ItemDao
import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toEntity
import com.example.moveon.domain.model.Box
import com.example.moveon.domain.model.Item
import com.example.moveon.domain.repository.InventoryRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
        val now = System.currentTimeMillis()
        val payload = mapOf(
            "box_uuid" to box.boxUuid,
            "box_id" to box.boxId,
            "user_id" to userId,
            "booking_id" to box.bookingId,
            "vehicle_id" to box.vehicleId,
            "category" to box.category,
            "label" to box.label,
            "volume" to box.volume,
            "packed" to box.packed,
            "color_hex" to colorHex,
            "created_at" to now
        )

        val failures = mutableListOf<String>()

        // Primary path: user-scoped boxes subcollection.
        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(box.boxUuid)
                .set(payload)
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes: ${it.message}"
        }

        // Secondary path: merge into the authenticated user's root document.
        // This matches existing successful write patterns in AuthRepositoryImpl.
        runCatching {
            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "inventory_updated_at" to now,
                        "inventory_boxes" to mapOf(box.boxUuid to payload)
                    ),
                    SetOptions.merge()
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} merge inventory_boxes: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to save box to Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override suspend fun updateBoxPackedStatus(boxUuid: String, isPacked: Boolean) {
        boxDao.updatePackedStatus(boxUuid, isPacked)
    }

    override suspend fun updateBoxPackedStatusInCloud(
        boxUuid: String,
        userId: String,
        isPacked: Boolean
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(boxUuid)
                .set(
                    mapOf(
                        "packed" to isPacked,
                        "updated_at" to now
                    ),
                    SetOptions.merge()
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes packed update: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "inventory_boxes.${boxUuid}.packed" to isPacked,
                        "inventory_boxes.${boxUuid}.updated_at" to now,
                        "inventory_updated_at" to now
                    )
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} inventory_boxes packed update: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to update packed status in Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override suspend fun updateBoxInfo(
        boxUuid: String,
        boxId: String,
        category: String,
        label: String
    ) {
        boxDao.updateBoxInfo(
            boxUuid = boxUuid,
            boxId = boxId,
            category = category,
            label = label
        )
    }

    override suspend fun updateBoxInfoInCloud(
        boxUuid: String,
        userId: String,
        boxId: String,
        category: String,
        label: String,
        colorHex: String
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val payload = mapOf(
            "box_id" to boxId,
            "category" to category,
            "label" to label,
            "color_hex" to colorHex,
            "updated_at" to now
        )
        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(boxUuid)
                .set(payload, SetOptions.merge())
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes info update: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "inventory_boxes.${boxUuid}.box_id" to boxId,
                        "inventory_boxes.${boxUuid}.category" to category,
                        "inventory_boxes.${boxUuid}.label" to label,
                        "inventory_boxes.${boxUuid}.color_hex" to colorHex,
                        "inventory_boxes.${boxUuid}.updated_at" to now,
                        "inventory_updated_at" to now
                    )
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} inventory_boxes info update: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to update box info in Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override suspend fun deleteBox(boxUuid: String) {
        boxDao.deleteBoxByUuid(boxUuid)
    }

    override suspend fun deleteBoxFromCloud(boxUuid: String, userId: String): Result<Unit> {
        val failures = mutableListOf<String>()

        runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("boxes")
                .document(boxUuid)
                .delete()
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid}/boxes delete: ${it.message}"
        }

        runCatching {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "inventory_boxes.${boxUuid}" to FieldValue.delete(),
                        "inventory_updated_at" to System.currentTimeMillis()
                    )
                )
                .await()
        }.onSuccess {
            return Result.success(Unit)
        }.onFailure {
            failures += "users/{uid} inventory_boxes delete: ${it.message}"
        }

        return Result.failure(
            IllegalStateException(
                "Failed to delete box from Firestore. Attempts: ${failures.joinToString(" | ")}"
            )
        )
    }

    override fun getBoxesForMove(bookingId: Int): Flow<List<Box>> {
        return boxDao.getBoxesForBooking(bookingId)
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