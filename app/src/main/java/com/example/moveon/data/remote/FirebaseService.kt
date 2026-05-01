package com.example.moveon.data.remote

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.moveon.app.data.remote.dto.BookingDto
import com.moveon.app.data.remote.dto.DriverDto
import com.moveon.app.data.remote.dto.ProviderDto
import com.moveon.app.data.remote.dto.VehicleDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FirebaseService @Inject constructor(
    private val realTimeDB: FirebaseDatabase,
    private val firestore: FirebaseFirestore
){
    //fetch providers
    suspend fun getAvailableProviders(): List<ProviderDto>{
        return firestore.collection("providers")
            .get()
            .await()
            .toObjects(ProviderDto::class.java)
    }

    //listens to live updates for real time tracking
    fun trackVehicleLocation(vehicleId: String): Flow<LatLng> = callbackFlow {
        val ref = realTimeDB.getReference("vehicles/$vehicleId/location")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java) ?: return
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: return
                trySend(LatLng(lat, lng))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    //update booking status
    suspend fun updateBookingStatus(bookingId: String, status: String){
        firestore.collection("bookings").document(bookingId)
            .update("status", status)
            .await()
    }

    suspend fun createBooking(booking: BookingDto): BookingDto {
        val bookingsCollection = firestore.collection("bookings")
        val documentRef = if (booking.booking_id.isBlank()) {
            bookingsCollection.document()
        } else {
            bookingsCollection.document(booking.booking_id)
        }

        val bookingToPersist = booking.copy(booking_id = documentRef.id)
        documentRef.set(bookingToPersist).await()
        return bookingToPersist
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