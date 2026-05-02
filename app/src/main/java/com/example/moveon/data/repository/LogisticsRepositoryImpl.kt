package com.example.moveon.data.repository

import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toDto
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.data.remote.FirebaseService
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.domain.repository.LogisticsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogisticsRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
): LogisticsRepository {

    override suspend fun getMarketplaceProviders(): List<Provider> {
        return firebaseService.getAvailableProviders()
            .map { it.toDomainModel() }
    }

    override suspend fun getProviderById(providerId: String): Result<Provider?> {
        return runCatching {
            firebaseService.getProviderById(providerId)?.toDomainModel()
        }
    }

    override suspend fun getVehiclesForProvider(providerId: String): Result<List<Vehicle>> {
        return runCatching {
            firebaseService.getVehiclesForProvider(providerId)
                .map { it.toDomainModel() }
        }
    }

    override suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle> {
        return runCatching {
            firebaseService.createVehicle(vehicle.toDto())
                .toDomainModel()
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle> {
        return runCatching {
            firebaseService.updateVehicle(vehicle.toDto())
                .toDomainModel()
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> {
        return runCatching {
            firebaseService.deleteVehicle(vehicleId)
        }
    }

    override suspend fun getDriversForProvider(providerId: String): Result<List<Driver>> {
        return runCatching {
            firebaseService.getDriversForProvider(providerId)
                .map { it.toDomainModel() }
        }
    }

    override suspend fun getBookingsForProvider(providerId: String): Result<List<Booking>> {
        return runCatching {
            firebaseService.getBookingsForProvider(providerId)
                .map { it.toDomainModel() }
        }
    }

    override suspend fun createBooking(booking: Booking): Result<Booking> {
        return runCatching {
            firebaseService.createBooking(booking.toDto())
                .toDomainModel()
        }
    }

    override suspend fun confirmBooking(booking: Booking) {
        firebaseService.updateBookingStatus(booking.id, "Confirmed")
    }

    override suspend fun confirmBookingById(bookingId: String) {
        firebaseService.updateBookingStatus(bookingId, "Confirmed")
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

    override fun trackVehicleLocation(vehicleId: String): Flow<LatLng> {
        return firebaseService.trackVehicleLocation(vehicleId)
    }
}