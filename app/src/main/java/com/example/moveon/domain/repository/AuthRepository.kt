package com.example.moveon.domain.repository

import com.example.moveon.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(email: String, pass: String, fName: String, lName: String, pNumber: String): Result<User>
    suspend fun logout()
}