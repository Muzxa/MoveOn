package com.example.moveon.data.repository

import com.example.moveon.data.remote.FirebaseService
import com.example.moveon.domain.repository.LogisticsRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.util.LogicUtils
import com.moveon.app.data.remote.dto.BookingDto
import com.moveon.app.data.remote.dto.ProviderDto
import javax.inject.Inject

class LogisticsRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
): LogisticsRepository {

    override suspend fun getMarketplaceProviders(): List<ProviderDto>{
        return firebaseService.getAvailableProviders()
    }

    override suspend fun confirmBooking(booking: BookingDto) {
        firebaseService.updateBookingStatus(booking.booking_id, "Confirmed")
    }

    override suspend fun verifyMoveOTP(booingId: String, enteredOtp: String): Boolean{
        return true
    }
}