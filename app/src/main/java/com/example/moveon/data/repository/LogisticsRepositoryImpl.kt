package com.example.moveon.data.repository

import com.example.moveon.data.mapper.toDomainModel
import com.example.moveon.data.mapper.toDto
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.BookingVehicle
import com.example.moveon.data.remote.FirebaseService
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.TripActorType
import com.example.moveon.domain.model.TripLocation
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.domain.repository.LogisticsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            val provider = firebaseService.getProviderById(providerId)?.toDomainModel()
            if (provider != null) {
                // attempt to fetch provider phone from users collection
                val userDto = runCatching { firebaseService.getUserById(providerId) }.getOrNull()
                val phone = userDto?.phone_number ?: ""
                provider.copy(phoneNumber = phone)
            } else null
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return runCatching {
            firebaseService.getUserById(userId)?.toDomainModel()
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

    override suspend fun createDriver(driver: Driver): Result<Driver> {
        return runCatching {
            firebaseService.createDriver(driver.toDto())
                .toDomainModel()
        }
    }

    override suspend fun updateDriver(driver: Driver): Result<Driver> {
        return runCatching {
            firebaseService.updateDriver(driver.toDto())
                .toDomainModel()
        }
    }

    override suspend fun deleteDriver(driverId: String): Result<Unit> {
        return runCatching {
            firebaseService.deleteDriver(driverId)
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

    override suspend fun confirmBookingById(bookingId: String, providerId: String) {
        firebaseService.updateBookingStatus(bookingId, "Confirmed", providerId)
    }

    override suspend fun assignVehicleAndDriverToBooking(
        bookingId: String,
        providerId: String,
        assignment: BookingVehicle
    ) {
        firebaseService.assignVehicleAndDriverToBooking(
            bookingId = bookingId,
            providerId = providerId,
            assignment = assignment.toDto()
        )
    }

    override suspend fun markBookingActive(bookingId: String) {
        firebaseService.updateBookingStatus(bookingId, "Active")
    }

    override suspend fun markBookingCompleted(bookingId: String) {
        firebaseService.updateBookingStatus(bookingId, "Completed")
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

    override suspend fun getBookingById(bookingId: String): Result<Booking?> {
        return runCatching {
            firebaseService.getBookingById(bookingId)?.toDomainModel()
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

    override suspend fun publishTripLocation(
        bookingId: String,
        providerId: String,
        userId: String,
        actorId: String,
        actorType: TripActorType,
        lat: Double,
        lng: Double,
        vehicleId: String?,
        speedMps: Double?,
        headingDeg: Double?,
        timestamp: Long
    ) {
        firebaseService.publishTripLocation(
            bookingId = bookingId,
            providerId = providerId,
            userId = userId,
            actorId = actorId,
            actorType = actorType,
            lat = lat,
            lng = lng,
            vehicleId = vehicleId,
            speedMps = speedMps,
            headingDeg = headingDeg,
            timestamp = timestamp
        )
    }

    override fun observeTripLocation(bookingId: String): Flow<TripLocation> {
        return firebaseService.observeTripLocation(bookingId)
    }

    override fun trackVehicleLocation(vehicleId: String): Flow<LatLng> {
        return firebaseService.trackVehicleLocation(vehicleId)
    }

    override fun observeBookingStatus(bookingId: String): Flow<BookingStatus> {
        return firebaseService.observeBookingStatus(bookingId)
    }

    override fun observeBookingsForProvider(providerId: String): Flow<List<Booking>> {
        return firebaseService.observeBookingsForProvider(providerId)
            .map { bookings -> bookings.map { it.toDomainModel() } }
    }
}