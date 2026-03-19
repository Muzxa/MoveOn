package com.example.moveon.domain.repository

import com.example.moveon.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    fun isUserLoggedIn(): Boolean
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun logout()
    
    suspend fun registerUser(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String
    ): Result<User>

    suspend fun registerProvider(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String,
        establishmentName: String,
        baseRate: Double,
        ratePerKm: Double
    ): Result<User>

    suspend fun registerDriver(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String,
        providerId: String,
        vehicleId: String,
        licenseNo: String
    ): Result<User>

    suspend fun signInWithGoogle(idToken: String): Result<User>
}
