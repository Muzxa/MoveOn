package com.example.moveon.domain.repository

import com.example.moveon.domain.model.SavedAddress
import kotlinx.coroutines.flow.Flow

interface SavedAddressRepository {
    fun observeSavedAddresses(): Flow<List<SavedAddress>>
    suspend fun addSavedAddress(address: SavedAddress): Result<Unit>
    suspend fun updateSavedAddress(address: SavedAddress): Result<Unit>
    suspend fun deleteSavedAddress(addressId: String): Result<Unit>
    suspend fun setDefaultAddress(addressId: String): Result<Unit>
}

