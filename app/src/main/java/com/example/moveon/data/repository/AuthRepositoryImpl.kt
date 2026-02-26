package com.example.moveon.data.repository

import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toDto
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.moveon.app.data.remote.dto.DriverDto
import com.moveon.app.data.remote.dto.ProviderDto
import com.moveon.app.data.remote.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
): AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            firestoreListener?.remove()
            
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestoreListener = firebaseFirestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        val user = snapshot?.toObject(UserDto::class.java)?.toDomainModel()
                        trySend(user)
                    }
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
            firestoreListener?.remove()
        }
    }

    override suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Login failed")

            val userDoc = firebaseFirestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val userDto = userDoc.toObject(UserDto::class.java)
                ?: throw Exception("User data not found")

            Result.success(userDto.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun registerUser(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed")

            val userDto = UserDto(
                user_id = firebaseUser.uid,
                first_name = fName,
                last_name = lName,
                email = email,
                phone_number = pNumber,
                role = "User",
                created_at = System.currentTimeMillis()
            )

            firebaseFirestore.collection("users")
                .document(firebaseUser.uid)
                .set(userDto)
                .await()

            Result.success(userDto.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerProvider(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String,
        establishmentName: String,
        baseRate: Double,
        ratePerKm: Double
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Provider registration failed")

            val userDto = UserDto(
                user_id = firebaseUser.uid,
                first_name = fName,
                last_name = lName,
                email = email,
                phone_number = pNumber,
                role = "Provider",
                created_at = System.currentTimeMillis()
            )

            val providerDto = ProviderDto(
                provider_id = firebaseUser.uid,
                establishment_name = establishmentName,
                base_rate = baseRate,
                rate_per_km = ratePerKm,
                is_verified = false,
                rating = 0.0
            )

            // Batch write to ensure both documents are created
            firebaseFirestore.runBatch { batch ->
                batch.set(firebaseFirestore.collection("users").document(firebaseUser.uid), userDto)
                batch.set(firebaseFirestore.collection("providers").document(firebaseUser.uid), providerDto)
            }.await()

            Result.success(userDto.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerDriver(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String,
        providerId: String,
        vehicleId: String,
        licenseNo: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Driver registration failed")

            val userDto = UserDto(
                user_id = firebaseUser.uid,
                first_name = fName,
                last_name = lName,
                email = email,
                phone_number = pNumber,
                role = "Driver",
                created_at = System.currentTimeMillis()
            )

            val driverDto = DriverDto(
                driver_id = firebaseUser.uid,
                provider_id = providerId,
                vehicle_id = vehicleId,
                license_no = licenseNo
            )

            firebaseFirestore.runBatch { batch ->
                batch.set(firebaseFirestore.collection("users").document(firebaseUser.uid), userDto)
                batch.set(firebaseFirestore.collection("drivers").document(firebaseUser.uid), driverDto)
            }.await()

            Result.success(userDto.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
