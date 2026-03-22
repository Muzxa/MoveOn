package com.example.moveon.data.remote

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.moveon.app.data.remote.dto.BookingDto
import com.moveon.app.data.remote.dto.DriverDto
import com.moveon.app.data.remote.dto.ProviderDto
import com.moveon.app.data.remote.dto.VehicleDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FirebaseService @Inject constructor(
    private val realTimeDB: FirebaseDatabase,
    private val firestore: FirebaseFirestore
){
    //fetch providers
    suspend fun getAvailableProviders(): List<ProviderDto>{
        return firestore.collection("providers")
            .whereEqualTo("is_verified", true)
            .get()
            .await()
            .toObjects(ProviderDto::class.java)
    }

    //listens to live updates for real time tracking
    fun trackVehicle(vehicleId: String){
        realTimeDB.getReference("vehicles/$vehicleId/location")
    }

    //update booking status
    suspend fun updateBookingStatus(bookingId: String, status: String){
        firestore.collection("bookings").document(bookingId)
            .update("status", status)
            .await()
    }

    suspend fun getBookingsForUser(userId: String): List<BookingDto> {
        return firestore.collection("bookings")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
            .toObjects(BookingDto::class.java)
    }

    suspend fun getProviderById(providerId: String): ProviderDto? {
        return firestore.collection("providers")
            .document(providerId)
            .get()
            .await()
            .toObject(ProviderDto::class.java)
    }

    suspend fun getVehiclesForProvider(providerId: String): List<VehicleDto> {
        return firestore.collection("vehicles")
            .whereEqualTo("provider_id", providerId)
            .get()
            .await()
            .toObjects(VehicleDto::class.java)
    }

    suspend fun getDriversForProvider(providerId: String): List<DriverDto> {
        return firestore.collection("drivers")
            .whereEqualTo("provider_id", providerId)
            .get()
            .await()
            .toObjects(DriverDto::class.java)
    }

    suspend fun getBookingsForProvider(providerId: String): List<BookingDto> {
        return firestore.collection("bookings")
            .whereEqualTo("provider_id", providerId)
            .get()
            .await()
            .toObjects(BookingDto::class.java)
    }
}