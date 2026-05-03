package com.example.moveon.data.repository

import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toSessionEntity
import com.example.moveon.data.local.dao.UserSessionDao
import com.example.moveon.domain.model.User
import com.example.moveon.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.moveon.app.data.remote.dto.DriverDto
import com.moveon.app.data.remote.dto.ProviderDto
import com.moveon.app.data.remote.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val userSessionDao: UserSessionDao
): AuthRepository {

    private fun mapAuthException(e: Exception): Exception {
        return if (e is FirebaseAuthUserCollisionException) {
            Exception("This email is already registered. Please sign in or use another email.")
        } else {
            e
        }
    }

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override suspend fun reserveAccount(email: String, pass: String): Result<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            result.user ?: throw Exception("Registration failed")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    private suspend fun getOrCreateAuthUser(
        email: String,
        pass: String,
        failureMessage: String
    ): FirebaseUser {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.email.equals(email, ignoreCase = true)) {
            return currentUser
        }

        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        return result.user ?: throw Exception(failureMessage)
    }

    override val currentUser: Flow<User?> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            firestoreListener?.remove()
            
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                launch { userSessionDao.clearAllSessions() }
                trySend(null)
            } else {
                firestoreListener = firebaseFirestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            launch {
                                val cachedUser = userSessionDao.getSessionById(firebaseUser.uid)
                                if (cachedUser != null) {
                                    trySend(cachedUser.toDomainModel())
                                }
                            }
                            return@addSnapshotListener
                        }
                        val user = snapshot?.toObject(UserDto::class.java)?.toDomainModel()
                        if (user != null) {
                            launch { userSessionDao.upsertSession(user.toSessionEntity()) }
                        }
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

            val user = userDto.toDomainModel()
            userSessionDao.upsertSession(user.toSessionEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun logout() {
        userSessionDao.clearAllSessions()
        firebaseAuth.signOut()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Google sign-in failed")

            val userDocRef = firebaseFirestore.collection("users").document(firebaseUser.uid)
            val userDoc = userDocRef.get().await()

            if (!userDoc.exists()) {
                // New Google user — create a Firestore profile
                val nameParts = firebaseUser.displayName?.split(" ") ?: emptyList()
                val userDto = UserDto(
                    user_id = firebaseUser.uid,
                    first_name = nameParts.firstOrNull() ?: "",
                    last_name = nameParts.drop(1).joinToString(" "),
                    email = firebaseUser.email ?: "",
                    phone_number = "",
                    role = "User",
                    created_at = System.currentTimeMillis()
                )
                userDocRef.set(userDto).await()
                val user = userDto.toDomainModel()
                userSessionDao.upsertSession(user.toSessionEntity())
                Result.success(user)
            } else {
                val userDto = userDoc.toObject(UserDto::class.java)
                    ?: throw Exception("User data not found")
                val user = userDto.toDomainModel()
                userSessionDao.upsertSession(user.toSessionEntity())
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun registerUser(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String
    ): Result<User> {
        return try {
            val firebaseUser = getOrCreateAuthUser(email, pass, "Registration failed")

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

            val user = userDto.toDomainModel()
            userSessionDao.upsertSession(user.toSessionEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun registerProvider(
        email: String,
        pass: String,
        fName: String,
        lName: String,
        pNumber: String,
        establishmentName: String,
        businessLat: Double,
        businessLng: Double,
        vehicleMake: String,
        vehicleModel: String,
        vehicleYear: String,
        vehicleColor: String,
        plateNumber: String,
        maxCapacityKg: Double,
        maxVolumeM3: Double,
        baseRate: Double,
        ratePerKm: Double
    ): Result<User> {
        return try {
            val firebaseUser = getOrCreateAuthUser(email, pass, "Provider registration failed")

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
                is_verified = false,
                rating = 0.0,
                business_lat = businessLat,
                business_lng = businessLng
            )

            val vehicleId = java.util.UUID.randomUUID().toString()
            val vehicleType = com.example.moveon.util.VehicleCategoryHelper.determineCategory(maxVolumeM3, maxCapacityKg)

            val vehicleDto = com.moveon.app.data.remote.dto.VehicleDto(
                vehicle_id = vehicleId,
                provider_id = firebaseUser.uid,
                type = vehicleType,
                make = vehicleMake,
                model = vehicleModel,
                year = vehicleYear,
                color = vehicleColor,
                plate_number = plateNumber,
                max_capacity = maxCapacityKg,
                max_volume = maxVolumeM3,
                base_rate = baseRate,
                rate_per_km = ratePerKm,
                current_lat = businessLat,
                current_lng = businessLng,
                is_available = true
            )

            // Batch write to ensure all documents are created
            firebaseFirestore.runBatch { batch ->
                batch.set(firebaseFirestore.collection("users").document(firebaseUser.uid), userDto)
                batch.set(firebaseFirestore.collection("providers").document(firebaseUser.uid), providerDto)
                batch.set(firebaseFirestore.collection("vehicles").document(vehicleId), vehicleDto)
            }.await()

            val user = userDto.toDomainModel()
            userSessionDao.upsertSession(user.toSessionEntity())
            Result.success(user)
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
            val firebaseUser = getOrCreateAuthUser(email, pass, "Driver registration failed")

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

            val user = userDto.toDomainModel()
            userSessionDao.upsertSession(user.toSessionEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }
}
