package com.example.moveon.domain.repository

import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.Provider
import com.google.type.LatLng
import kotlinx.coroutines.flow.Flow

interface LogisticsRepository {
    suspend fun getMarketplaceProviders(): List<Provider>
    suspend fun confirmBooking(booking: Booking)
    suspend fun verifyMoveOTP(bookingId: String, enteredOtp: String): Boolean
    suspend fun getCurrentBookingForUser(userId: String): Result<Booking?>
    //fun trackVehicleLocation(vehicleId: String): Flow<LatLng>
}