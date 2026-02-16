package com.example.moveon.data.repository

import android.util.Log.e
import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toDto
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moveon.app.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
): AuthRepository {
    
    override val currentUser: Flow<User?> = MutableStateFlow(null)
    
    override suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Login failed")
            
            val userDoc = firebaseFirestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val userDto = userDoc.toObject(UserDto::class.java) 
                ?: throw Exception("User not found")
            
            val user = userDto.toDomainModel()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun register(
        email: String, 
        pass: String, 
        fName: String, 
        lName: String, 
        pNumber: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed")

            val newUser = User(
                id = firebaseUser.uid,
                firstName = fName,
                lastName = lName,
                phoneNumber = pNumber,
                email = email,
                role = UserRole.USER,
                createdAt = System.currentTimeMillis(),
                lastLoginTime = System.currentTimeMillis()
            )
            
            firebaseFirestore.collection("users")
                .document(firebaseUser.uid)
                .set(newUser.toDto())
                .await()
            
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}