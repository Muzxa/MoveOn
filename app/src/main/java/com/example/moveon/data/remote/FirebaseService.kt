package com.example.moveon.data.remote

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.moveon.app.data.remote.dto.BookingDto
import com.moveon.app.data.remote.dto.DriverDto
import com.moveon.app.data.remote.dto.ProviderDto
import com.moveon.app.data.remote.dto.VehicleDto
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.TripActorType
import com.example.moveon.domain.model.TripLocation
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

    // Writes latest actor location for a booking in Realtime Database.
    suspend fun publishTripLocation(
        bookingId: String,
        providerId: String,
        userId: String,
        actorId: String,
        actorType: TripActorType,
        lat: Double,
        lng: Double,
        vehicleId: String? = null,
        speedMps: Double? = null,
        headingDeg: Double? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val latestRef = realTimeDB.getReference("trip_locations/$bookingId/latest")
        val payload = mutableMapOf<String, Any>(
            "booking_id" to bookingId,
            "provider_id" to providerId,
            "user_id" to userId,
            "actor_id" to actorId,
            "actor_type" to actorType.name,
            "lat" to lat,
            "lng" to lng,
            "timestamp" to timestamp
        )
        if (!vehicleId.isNullOrBlank()) payload["vehicle_id"] = vehicleId
        if (speedMps != null) payload["speed_mps"] = speedMps
        if (headingDeg != null) payload["heading_deg"] = headingDeg

        latestRef.setValue(payload).await()
    }

    fun observeTripLocation(bookingId: String): Flow<TripLocation> = callbackFlow {
        val ref = realTimeDB.getReference("trip_locations/$bookingId/latest")
        Log.d("TripLocationListener", "[SUBSCRIBE] Starting trip location listener for booking: $bookingId at path: trip_locations/$bookingId/latest")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TripLocationListener", "[DATA_RECEIVED] Snapshot exists: ${snapshot.exists()}, children: ${snapshot.childrenCount}")
                val lat = snapshot.child("lat").getValue(Double::class.java) ?: run {
                    Log.e("TripLocationListener", "[PARSE_ERROR] Missing or invalid lat: ${snapshot.child("lat").value}")
                    return
                }
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: run {
                    Log.e("TripLocationListener", "[PARSE_ERROR] Missing or invalid lng: ${snapshot.child("lng").value}")
                    return
                }
                Log.d("TripLocationListener", "[LOCATION_UPDATE] Booking: $bookingId, Lat: $lat, Lng: $lng")
                val actorId = snapshot.child("actor_id").getValue(String::class.java)
                    ?: snapshot.child("actorId").getValue(String::class.java)
                    .orEmpty()
                val actorTypeRaw = snapshot.child("actor_type").getValue(String::class.java)
                    ?: snapshot.child("actorType").getValue(String::class.java)
                    .orEmpty()
                val actorType = runCatching { TripActorType.valueOf(actorTypeRaw.uppercase()) }
                    .getOrDefault(TripActorType.PROVIDER)
                val vehicleId = snapshot.child("vehicle_id").getValue(String::class.java)
                    ?: snapshot.child("vehicleId").getValue(String::class.java)
                val speedMps = snapshot.child("speed_mps").getValue(Double::class.java)
                    ?: snapshot.child("speedMps").getValue(Double::class.java)
                val headingDeg = snapshot.child("heading_deg").getValue(Double::class.java)
                    ?: snapshot.child("headingDeg").getValue(Double::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                    ?: System.currentTimeMillis()

                trySend(
                    TripLocation(
                        bookingId = bookingId,
                        actorId = actorId,
                        actorType = actorType,
                        vehicleId = vehicleId,
                        lat = lat,
                        lng = lng,
                        speedMps = speedMps,
                        headingDeg = headingDeg,
                        timestamp = timestamp
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TripLocationListener", "[LISTENER_ERROR] Failed to observe trip location for $bookingId: ${error.message}", error.toException())
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Legacy vehicle-level location stream kept for compatibility.
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

    // Updates booking status and, when provided, the assigned provider id.
    suspend fun updateBookingStatus(bookingId: String, status: String, providerId: String? = null) {
        val updates = mutableMapOf<String, Any>(
            "status" to status
        )
        if (!providerId.isNullOrBlank()) {
            updates["provider_id"] = providerId
        }
        firestore.collection("bookings").document(bookingId)
            .update(updates)
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
        Log.d("FirebaseService", "[BOOKING_CREATE] Persisting booking - ID: ${bookingToPersist.booking_id}, Provider: ${bookingToPersist.provider_id}, User: ${bookingToPersist.user_id}, Status: ${bookingToPersist.status}")
        documentRef.set(bookingToPersist).await()
        Log.d("FirebaseService", "[BOOKING_CREATE] Successfully persisted booking ${bookingToPersist.booking_id} to provider ${bookingToPersist.provider_id}")
        return bookingToPersist
    }

    suspend fun createVehicle(vehicle: VehicleDto): VehicleDto {
        val vehiclesCollection = firestore.collection("vehicles")
        val documentRef = if (vehicle.vehicle_id.isBlank()) {
            vehiclesCollection.document()
        } else {
            vehiclesCollection.document(vehicle.vehicle_id)
        }

        val vehicleToPersist = vehicle.copy(vehicle_id = documentRef.id)
        documentRef.set(vehicleToPersist).await()
        return vehicleToPersist
    }

    suspend fun updateVehicle(vehicle: VehicleDto): VehicleDto {
        require(vehicle.vehicle_id.isNotBlank()) { "Vehicle id is required." }
        val documentRef = firestore.collection("vehicles").document(vehicle.vehicle_id)
        documentRef.set(vehicle).await()
        return vehicle
    }

    suspend fun deleteVehicle(vehicleId: String) {
        firestore.collection("vehicles")
            .document(vehicleId)
            .delete()
            .await()
    }

    suspend fun getBookingsForUser(userId: String): List<BookingDto> {
        return firestore.collection("bookings")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
            .toObjects(BookingDto::class.java)
    }

    suspend fun getBookingById(bookingId: String): BookingDto? {
        return firestore.collection("bookings")
            .document(bookingId)
            .get()
            .await()
            .toObject(BookingDto::class.java)
            ?.copy(booking_id = bookingId)
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

    fun observeBookingsForProvider(providerId: String): Flow<List<BookingDto>> = callbackFlow {
        Log.d("FirebaseService", "[PROVIDER_LISTENER] Setting up listener for provider: $providerId")
        val query = firestore.collection("bookings")
            .whereEqualTo("provider_id", providerId)

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseService", "[PROVIDER_LISTENER] ERROR for provider $providerId: ${error.message}", error)
                close(error)
                return@addSnapshotListener
            }

            val bookings = snapshot?.toObjects(BookingDto::class.java).orEmpty()
            Log.d("FirebaseService", "[PROVIDER_LISTENER] Received ${bookings.size} bookings for provider $providerId: ${bookings.map { it.booking_id to it.status }.joinToString(", ")}")
            trySend(bookings)
        }

        awaitClose { 
            Log.d("FirebaseService", "[PROVIDER_LISTENER] Removing listener for provider: $providerId")
            registration.remove() 
        }
    }

    // Observes real-time booking status changes
    fun observeBookingStatus(bookingId: String): Flow<BookingStatus> = callbackFlow {
        val ref = firestore.collection("bookings").document(bookingId)
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val bookingDto = snapshot.toObject(BookingDto::class.java)
                if (bookingDto != null) {
                    val status = try {
                        BookingStatus.valueOf(bookingDto.status.uppercase())
                    } catch (e: Exception) {
                        BookingStatus.SEARCHING
                    }
                    trySend(status)
                }
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun getUserById(userId: String): com.moveon.app.data.remote.dto.UserDto? {
        return firestore.collection("users").document(userId).get().await().toObject(com.moveon.app.data.remote.dto.UserDto::class.java)
    }
}