package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.Vehicle
import com.google.type.LatLng
import kotlinx.coroutines.flow.Flow

interface LogisticsRepository {
    suspend fun getMarketplaceProviders(): List<Provider>
    suspend fun getProviderById(providerId: String): Result<Provider?>
    suspend fun getVehiclesForProvider(providerId: String): Result<List<Vehicle>>
    suspend fun getDriversForProvider(providerId: String): Result<List<Driver>>
    suspend fun getBookingsForProvider(providerId: String): Result<List<Booking>>
    suspend fun createBooking(booking: Booking): Result<Booking>
    suspend fun confirmBooking(booking: Booking)
    suspend fun verifyMoveOTP(bookingId: String, enteredOtp: String): Boolean
    suspend fun getBookingsForUser(userId: String): Result<List<Booking>>
    suspend fun getCurrentBookingForUser(userId: String): Result<Booking?>
    //fun trackVehicleLocation(vehicleId: String): Flow<LatLng>
}