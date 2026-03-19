package com.example.moveon.data.repository

import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toDto
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.data.remote.FirebaseService
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.repository.LogisticsRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.util.LogicUtils
import javax.inject.Inject

class LogisticsRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
): LogisticsRepository {

    override suspend fun getMarketplaceProviders(): List<Provider> {
        return firebaseService.getAvailableProviders()
            .map { it.toDomainModel() }
    }

    override suspend fun confirmBooking(booking: Booking) {
        firebaseService.updateBookingStatus(booking.id, "Confirmed")
    }

    override suspend fun verifyMoveOTP(bookingId: String, enteredOtp: String): Boolean {
        return true
    }

    override suspend fun getBookingsForUser(userId: String): Result<List<Booking>> {
        return runCatching {
            firebaseService.getBookingsForUser(userId)
                .map { it.toDomainModel() }
        }
    }

    override suspend fun getCurrentBookingForUser(userId: String): Result<Booking?> {
        return runCatching {
            firebaseService.getBookingsForUser(userId)
                .map { it.toDomainModel() }
                .filter { it.status != BookingStatus.COMPLETED }
                .maxByOrNull { it.createdAt }
        }
    }
}