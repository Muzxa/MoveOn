package com.example.moveon.data.repository

import com.example.moveon.data.mapper.toDomain
import com.example.moveon.data.remote.dto.SavedAddressDto
import com.example.moveon.domain.model.SavedAddress
import com.example.moveon.domain.repository.SavedAddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SavedAddressRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : SavedAddressRepository {

    /** Explicit map so Firestore field names match [docs/firestore.rules] (Kotlin POJO mapping can drift). */
    private fun SavedAddress.toFirestorePayload(documentId: String, updatedAtMillis: Long, createdAtMillis: Long) =
        mapOf(
            "address_id" to documentId,
            "label" to label,
            "address_line_1" to addressLine1,
            "address_line_2" to addressLine2,
            "city" to city,
            "lat" to lat,
            "lng" to lng,
            "is_default" to isDefault,
            "updated_at" to updatedAtMillis,
            "created_at" to createdAtMillis
        )

    private fun userIdOrNull(): String? = auth.currentUser?.uid

    private fun addressesCollectionFor(uid: String) =
        firestore.collection("users").document(uid).collection("saved_addresses")

    private fun addressesCollectionOrNull() =
        userIdOrNull()?.let { uid -> addressesCollectionFor(uid) }

    override fun observeSavedAddresses(): Flow<List<SavedAddress>> = callbackFlow {
        var firestoreReg: ListenerRegistration? = null

        fun clearFirestoreListener() {
            firestoreReg?.remove()
            firestoreReg = null
        }

        fun mapSnapshotToItems(snapshot: QuerySnapshot?): List<SavedAddress> {
            return runCatching {
                snapshot?.documents.orEmpty()
                    .mapNotNull { doc ->
                        runCatching {
                            doc.toObject(SavedAddressDto::class.java)?.copy(address_id = doc.id)
                        }.getOrNull()
                    }
                    .map { it.toDomain() }
                    .sortedWith(
                        compareByDescending<SavedAddress> { it.isDefault }
                            .thenByDescending { it.updatedAt }
                    )
            }.getOrElse { emptyList() }
        }

        fun attachListener(uid: String) {
            clearFirestoreListener()
            firestoreReg = addressesCollectionFor(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(mapSnapshotToItems(snapshot))
            }
        }

        val authListener = FirebaseAuth.AuthStateListener { fa ->
            val uid = fa.currentUser?.uid
            if (uid == null) {
                clearFirestoreListener()
                trySend(emptyList())
            } else {
                attachListener(uid)
            }
        }
        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            clearFirestoreListener()
        }
    }

    override suspend fun addSavedAddress(address: SavedAddress): Result<Unit> {
        return runCatching {
            require(userIdOrNull() != null) { "User not logged in." }
            val now = System.currentTimeMillis()
            val col = addressesCollectionOrNull() ?: error("User not logged in.")
            val docRef = if (address.id.isBlank()) col.document() else col.document(address.id)

            val createdAt = if (address.createdAt == 0L) now else address.createdAt
            val toStore = address.copy(
                id = docRef.id,
                createdAt = createdAt,
                updatedAt = now
            )

            docRef.set(toStore.toFirestorePayload(docRef.id, now, createdAt), SetOptions.merge()).await()

            if (toStore.isDefault) {
                setDefaultAddressInternal(docRef.id)
            }
        }
    }

    override suspend fun updateSavedAddress(address: SavedAddress): Result<Unit> {
        return runCatching {
            require(userIdOrNull() != null) { "User not logged in." }
            require(address.id.isNotBlank()) { "address id is required" }
            val now = System.currentTimeMillis()
            val col = addressesCollectionOrNull() ?: error("User not logged in.")
            val docRef = col.document(address.id)

            val toStore = address.copy(updatedAt = now)

            docRef.set(
                toStore.toFirestorePayload(address.id, now, toStore.createdAt),
                SetOptions.merge()
            ).await()

            if (toStore.isDefault) {
                setDefaultAddressInternal(address.id)
            }
        }
    }

    override suspend fun deleteSavedAddress(addressId: String): Result<Unit> {
        return runCatching {
            require(userIdOrNull() != null) { "User not logged in." }
            require(addressId.isNotBlank()) { "address id is required" }
            val col = addressesCollectionOrNull() ?: error("User not logged in.")
            col.document(addressId).delete().await()
        }
    }

    override suspend fun setDefaultAddress(addressId: String): Result<Unit> {
        return runCatching { setDefaultAddressInternal(addressId) }
    }

    private suspend fun setDefaultAddressInternal(addressId: String) {
        val col = addressesCollectionOrNull() ?: return
        val snapshot = col.get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            val isDefault = doc.id == addressId
            batch.update(col.document(doc.id), "is_default", isDefault)
            batch.update(col.document(doc.id), "updated_at", System.currentTimeMillis())
        }
        // commit() with no writes can be error-prone across SDK versions; skip if empty
        if (snapshot.documents.isNotEmpty()) {
            batch.commit().await()
        }
    }
}

