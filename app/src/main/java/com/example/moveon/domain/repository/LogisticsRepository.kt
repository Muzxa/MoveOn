package com.example.moveon.domain.repository

import com.google.type.LatLng
import com.moveon.app.data.remote.dto.BookingDto
import com.moveon.app.data.remote.dto.ProviderDto
import kotlinx.coroutines.flow.Flow

interface LogisticsRepository {
    suspend fun getMarketplaceProviders(): List<ProviderDto>
    suspend fun confirmBooking(booking: BookingDto)
    suspend fun verifyMoveOTP(bookingId: String, enteredOtp: String): Boolean
    //fun trackVehicleLocation(vehicleId: String): Flow<LatLng>
}