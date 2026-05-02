package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.TripActorType
import com.example.moveon.domain.model.TripLocation
import com.example.moveon.domain.model.Vehicle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface LogisticsRepository {
    suspend fun getMarketplaceProviders(): List<Provider>
    suspend fun getProviderById(providerId: String): Result<Provider?>
    suspend fun getVehiclesForProvider(providerId: String): Result<List<Vehicle>>
    suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle>
    suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle>
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
    suspend fun getDriversForProvider(providerId: String): Result<List<Driver>>
    suspend fun getBookingsForProvider(providerId: String): Result<List<Booking>>
    suspend fun createBooking(booking: Booking): Result<Booking>
    suspend fun confirmBooking(booking: Booking)
    // Confirm booking using only booking id (server-side update)
    suspend fun confirmBookingById(bookingId: String, providerId: String)
    suspend fun verifyMoveOTP(bookingId: String, enteredOtp: String): Boolean
    suspend fun getBookingsForUser(userId: String): Result<List<Booking>>
    suspend fun getCurrentBookingForUser(userId: String): Result<Booking?>
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
    )
    fun observeTripLocation(bookingId: String): Flow<TripLocation>
    fun trackVehicleLocation(vehicleId: String): Flow<LatLng>
    fun observeBookingStatus(bookingId: String): Flow<BookingStatus>
    fun observeBookingsForProvider(providerId: String): Flow<List<Booking>>
}