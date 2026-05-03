package com.example.moveon.ui.features.provider

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.BookingVehicle
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.TripActorType
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import com.example.moveon.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class ProviderDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

    private var bookingsListenerJob: Job? = null
    private var tripLocationJob: Job? = null
    private var lastPublishedLocation: LatLng? = null
    private var lastPublishedAt: Long = 0L

    fun acceptBooking(bookingId: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val request = _state.value.newRequests.firstOrNull { it.bookingId == bookingId }
        val fallbackAssignment = request?.assignmentOptions?.firstOrNull { it.isAvailable }
            ?: request?.assignmentOptions?.firstOrNull()

        if (fallbackAssignment == null) {
            onComplete(false, "No vehicle/driver assignment is available")
            return
        }

        assignAndDispatchBooking(
            bookingId = bookingId,
            vehicleId = fallbackAssignment.vehicleId,
            driverId = fallbackAssignment.driverId,
            onComplete = onComplete
        )
    }

    fun assignAndDispatchBooking(
        bookingId: String,
        vehicleId: String,
        driverId: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                val providerId = currentUser.value?.id
                    ?: throw IllegalStateException("Provider session not available")
                logisticsRepository.assignVehicleAndDriverToBooking(
                    bookingId = bookingId,
                    providerId = providerId,
                    assignment = BookingVehicle(
                        bookingId = bookingId,
                        vehicleId = vehicleId,
                        driverId = driverId
                    )
                )
                onComplete(true, null)
                refresh()
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun markArrivedAtPickup(bookingId: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                logisticsRepository.markBookingActive(bookingId)
                onComplete(true, null)
                refresh()
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun markTripCompleted(bookingId: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                logisticsRepository.markBookingCompleted(bookingId)
                onComplete(true, null)
                refresh()
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun dismissNewRequestNotification() {
        _state.value = _state.value.copy(
            showNewRequestNotification = false,
            newRequestNotificationMessage = null
        )
    }

    fun publishForegroundLocationSnapshot(lat: Double, lng: Double) {
        Log.d("PublishLocation", "[START] Called with Lat=$lat, Lng=$lng")
        val snapshot = _state.value
        val bookingId = snapshot.activeTrackingBookingId ?: run {
            Log.w("PublishLocation", "[NO_BOOKING_ID] activeTrackingBookingId is null")
            return
        }
        val providerId = currentUser.value?.id ?: run {
            Log.w("PublishLocation", "[NO_PROVIDER_ID] currentUser.id is null")
            return
        }
        val userId = snapshot.activeTrackingUserId ?: run {
            Log.w("PublishLocation", "[NO_USER_ID] activeTrackingUserId is null")
            return
        }
        val actorId = providerId
        val nowMs = System.currentTimeMillis()
        Log.d("PublishLocation", "[IDS] BookingId=$bookingId, ProviderId=$providerId, UserId=$userId")

        val newPoint = LatLng(lat, lng)
        val lastPoint = lastPublishedLocation
        val movedKm = if (lastPoint != null) {
            LocationUtils.calculateDistanceKm(lastPoint, newPoint)
        } else {
            Double.MAX_VALUE
        }

        val elapsedMs = nowMs - lastPublishedAt
        val shouldPublish = lastPoint == null || elapsedMs >= 10_000L || movedKm >= 0.02
        Log.d("PublishLocation", "[THROTTLE] LastPoint=$lastPoint, ElapsedMs=$elapsedMs, MovedKm=$movedKm, ShouldPublish=$shouldPublish")
        if (!shouldPublish) {
            Log.d("PublishLocation", "[THROTTLED] Skipping publish due to throttle")
            return
        }

        viewModelScope.launch {
            Log.d("PublishLocation", "[ASYNC_START] Launching repository call for booking $bookingId")
            runCatching {
                logisticsRepository.publishTripLocation(
                    bookingId = bookingId,
                    providerId = providerId,
                    userId = userId,
                    actorId = actorId,
                    actorType = TripActorType.PROVIDER,
                    lat = lat,
                    lng = lng,
                    vehicleId = snapshot.activeTrackingVehicleId,
                    timestamp = nowMs
                )
                Log.d("PublishLocation", "[PUBLISHED] Successfully published location for booking $bookingId")
            }.onFailure { error ->
                Log.e("PublishLocation", "[PUBLISH_ERROR] Failed to publish location: ${error.message}", error)
            }
            lastPublishedLocation = newPoint
            lastPublishedAt = nowMs
        }
    }

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _state = mutableStateOf(ProviderDashboardUiState(isLoading = true))
    val state: State<ProviderDashboardUiState> = _state

    init {
        observeAndLoad()
    }

    fun refresh() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                _state.value = ProviderDashboardUiState(
                    isLoading = false,
                    errorMessage = "Could not load provider profile."
                )
                return@launch
            }
            loadForProvider(user)
        }
    }

    private fun observeAndLoad() {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _state.value = ProviderDashboardUiState(
                        isLoading = false,
                        errorMessage = "Could not load provider profile."
                    )
                } else {
                    loadForProvider(user)
                }
            }
        }
    }

    private suspend fun loadForProvider(user: User) {
        if (user.role != UserRole.PROVIDER) {
            _state.value = ProviderDashboardUiState(
                isLoading = false,
                providerDisplayName = "Provider",
                errorMessage = "Current user is not a provider account."
            )
            return
        }

        _state.value = _state.value.copy(
            isLoading = true,
            errorMessage = null,
            providerDisplayName = "${user.firstName} ${user.lastName}".trim().ifBlank { "Provider" }
        )

        val providerDeferred = viewModelScope.async { logisticsRepository.getProviderById(user.id) }
        val vehiclesDeferred = viewModelScope.async { logisticsRepository.getVehiclesForProvider(user.id) }
        val driversDeferred = viewModelScope.async { logisticsRepository.getDriversForProvider(user.id) }
        val bookingsDeferred = viewModelScope.async { logisticsRepository.getBookingsForProvider(user.id) }

        val providerResult = providerDeferred.await()
        val vehiclesResult = vehiclesDeferred.await()
        val driversResult = driversDeferred.await()
        val bookingsResult = bookingsDeferred.await()

        val provider = providerResult.getOrNull()
        val vehicles = vehiclesResult.getOrDefault(emptyList())
        val drivers = driversResult.getOrDefault(emptyList())
        val bookings = bookingsResult.getOrDefault(emptyList())

        val usersById = bookings
            .map { it.userId }
            .filter { it.isNotBlank() }
            .distinct()
            .associateWith { userId ->
                logisticsRepository.getUserById(userId).getOrNull()
            }

        val userBookingCounts = bookings
            .map { it.userId }
            .filter { it.isNotBlank() }
            .distinct()
            .associateWith { userId ->
                logisticsRepository.getBookingsForUser(userId)
                    .getOrDefault(emptyList())
                    .size
            }

        val displayName = provider?.establishmentName
            ?.takeIf { it.isNotBlank() }
            ?: "${user.firstName} ${user.lastName}".trim().ifBlank { "Provider" }

        val activeJobs = bookings
            .filter { it.status == BookingStatus.ACTIVE || it.status == BookingStatus.CONFIRMED }
            .sortedByDescending { it.createdAt }

        val newRequests = bookings
            .filter { it.status == BookingStatus.SEARCHING }
            .sortedByDescending { it.createdAt }

        val todayStart = now().toLocalDate().atStartOfDay(now().zone)
        val weekStart = now().toLocalDate().minusDays(now().dayOfWeek.value.toLong() - 1L).atStartOfDay(now().zone)
        val monthStart = now().withDayOfMonth(1).toLocalDate().atStartOfDay(now().zone)

        val earningsToday = sumEarningsSince(bookings, todayStart)
        val earningsWeek = sumEarningsSince(bookings, weekStart)
        val earningsMonth = sumEarningsSince(bookings, monthStart)

        val completed = bookings.filter { it.status == BookingStatus.COMPLETED }
        val completedVerified = completed.count { it.isOtpVerified }
        val onTimePct = if (completed.isNotEmpty()) {
            (completedVerified * 100) / completed.size
        } else {
            0
        }

        publishDashboardState(
            provider = provider,
            vehicles = vehicles,
            drivers = drivers,
            bookings = bookings,
            usersById = usersById,
            userBookingCounts = userBookingCounts,
            displayName = displayName,
            earningsToday = earningsToday,
            earningsWeek = earningsWeek,
            earningsMonth = earningsMonth,
            onTimePct = onTimePct,
            hasCriticalError = providerResult.isFailure && vehiclesResult.isFailure && driversResult.isFailure && bookingsResult.isFailure,
            shouldShowNotification = false
        )

        bookingsListenerJob?.cancel()
        bookingsListenerJob = viewModelScope.launch {
            try {
                Log.d("ProviderDashboard", "[LISTENER_SETUP] Starting live bookings listener for provider: ${user.id}")
                logisticsRepository.observeBookingsForProvider(user.id)
                    .catch { error ->
                        Log.e("ProviderDashboard", "[LISTENER_ERROR] Failed to observe bookings for provider ${user.id}: ${error.message}", error)
                    }
                    .collect { liveBookings ->
                        Log.d("ProviderDashboard", "[LISTENER_UPDATE] Received ${liveBookings.size} live bookings for provider ${user.id}")
                        val newRequests = liveBookings.filter { it.status == BookingStatus.SEARCHING }
                        Log.d("ProviderDashboard", "[NEW_REQUESTS] New requests count: ${newRequests.size} - ${newRequests.map { it.id }.joinToString(", ")}")
                        
                        publishDashboardState(
                            provider = provider,
                            vehicles = vehicles,
                            drivers = drivers,
                            bookings = liveBookings,
                            usersById = usersById,
                            userBookingCounts = userBookingCounts,
                            displayName = displayName,
                            earningsToday = sumEarningsSince(liveBookings, todayStart),
                            earningsWeek = sumEarningsSince(liveBookings, weekStart),
                            earningsMonth = sumEarningsSince(liveBookings, monthStart),
                            onTimePct = computeOnTimePercent(liveBookings),
                            hasCriticalError = false,
                            shouldShowNotification = true
                        )
                    }
            } catch (e: Exception) {
                Log.e("ProviderDashboard", "[LISTENER_SETUP] Exception setting up listener: ${e.message}", e)
            }
        }
    }

    private fun publishDashboardState(
        provider: Provider?,
        vehicles: List<Vehicle>,
        drivers: List<Driver>,
        bookings: List<Booking>,
        usersById: Map<String, User?>,
        userBookingCounts: Map<String, Int>,
        displayName: String,
        earningsToday: Double,
        earningsWeek: Double,
        earningsMonth: Double,
        onTimePct: Int,
        hasCriticalError: Boolean,
        shouldShowNotification: Boolean
    ) {
        val activeJobs = bookings
            .filter { it.status == BookingStatus.ACTIVE || it.status == BookingStatus.CONFIRMED }
            .sortedByDescending { it.createdAt }

        val newRequests = bookings
            .filter { it.status == BookingStatus.SEARCHING }
            .sortedByDescending { it.createdAt }

        val requestItems = newRequests.map { booking ->
            val user = usersById[booking.userId]
            val allOptions = buildAssignmentOptions(
                bookingId = booking.id,
                vehicles = vehicles,
                drivers = drivers
            )
            val requestedService = planForFare(booking.totalFare)
            val matchingOptions = allOptions.filter { it.serviceTag == requestedService }
            val otherOptions = allOptions.filter { it.serviceTag != requestedService }

            val distanceKm = routeDistanceKm(booking)
            val startTime = if (booking.scheduledTime > 0L) booking.scheduledTime else booking.createdAt
            
            ProviderNewRequestUi(
                bookingId = booking.id,
                bookingCode = buildBookingCode(booking.id),
                userId = booking.userId,
                service = requestedService,
                ageLabel = ageLabel(booking.createdAt),
                pickup = booking.pickupAddress,
                destination = booking.dropOffAddress,
                pickupScheduleLabel = timeLabel(startTime),
                dropOffEstimateLabel = estimateDropoffLabel(startTime, distanceKm),
                distanceKm = distanceKm,
                estimatedHours = estimateTripHours(distanceKm),
                totalFare = booking.totalFare,
                providerEarnings = booking.totalFare * 0.92,
                instructions = "Please call 15 minutes before arrival. Elevator available at pickup.", // Following the screenshot
                customerName = listOfNotNull(user?.firstName, user?.lastName)
                    .joinToString(" ")
                    .ifBlank { "Customer" },
                customerPhone = user?.phoneNumber,
                customerRating = if (booking.rating > 0f) booking.rating.toDouble() else 4.7,
                customerBookingCount = userBookingCounts[booking.userId] ?: 12,
                matchingOptions = matchingOptions,
                otherOptions = otherOptions
            )
        }

        val activeItems = activeJobs.map { booking ->
            val assignment = booking.vehicles.firstOrNull()
            val vehicle = vehicles.firstOrNull { it.id == assignment?.vehicleId }
            val driver = drivers.firstOrNull { it.id == assignment?.driverId }
            val user = usersById[booking.userId]
            val distanceKm = routeDistanceKm(booking)
            val startTime = if (booking.scheduledTime > 0L) booking.scheduledTime else booking.createdAt
            
            ProviderActiveJobUi(
                bookingId = booking.id,
                userId = booking.userId,
                service = planForFare(booking.totalFare),
                status = when (booking.status) {
                    BookingStatus.ACTIVE -> "In Transit"
                    BookingStatus.CONFIRMED -> "Loading"
                    else -> booking.status.name.lowercase().replaceFirstChar { it.uppercaseChar() }
                },
                code = buildBookingCode(booking.id).substringAfter("-20"), // Shorter code for active jobs
                pickup = booking.pickupAddress,
                destination = booking.dropOffAddress,
                pickupLat = booking.pickupLat,
                pickupLng = booking.pickupLng,
                dropOffLat = booking.dropOffLat,
                dropOffLng = booking.dropOffLng,
                driver = driver?.name?.ifBlank { null } ?: "Unassigned",
                eta = if (booking.status == BookingStatus.ACTIVE) {
                    etaLabel(System.currentTimeMillis() + (estimateTripHours(distanceKm) * 60 * 60_000L).toLong())
                } else {
                    etaLabel(startTime)
                },
                vehicle = formatVehicle(vehicle),
                customerName = listOfNotNull(user?.firstName, user?.lastName)
                    .joinToString(" ")
                    .ifBlank { "Customer" },
                customerPhone = user?.phoneNumber,
                boxesCount = booking.vehicles.size,
                distanceKm = distanceKm,
                totalFare = booking.totalFare,
                assignedVehicleId = assignment?.vehicleId,
                otp = booking.otp
            )
        }

        val trackingBooking = activeJobs.firstOrNull()
        val trackingVehicleId = trackingBooking?.vehicles?.firstOrNull()?.vehicleId

        val previousNewRequestsCount = _state.value.newRequests.size
        val currentNewRequestsCount = requestItems.size
        val shouldEmitNotification = shouldShowNotification && previousNewRequestsCount > 0 && currentNewRequestsCount > previousNewRequestsCount
        val notificationMessage = if (shouldEmitNotification) {
            "New booking request received"
        } else {
            null
        }

        _state.value = ProviderDashboardUiState(
            isLoading = false,
            providerDisplayName = displayName,
            vehiclesCount = vehicles.size,
            driversCount = drivers.size,
            activeJobsCount = activeJobs.size,
            earningsToday = earningsToday,
            earningsThisWeek = earningsWeek,
            earningsThisMonth = earningsMonth,
            rating = provider?.rating ?: 0.0,
            trips = bookings.size,
            onTimePercent = onTimePct,
            newRequests = requestItems,
            activeJobs = activeItems,
            activeTrackingBookingId = trackingBooking?.id,
            activeTrackingUserId = trackingBooking?.userId,
            activeTrackingVehicleId = trackingVehicleId,
            liveVehicleLat = _state.value.liveVehicleLat,
            liveVehicleLng = _state.value.liveVehicleLng,
            showNewRequestNotification = shouldEmitNotification,
            newRequestNotificationMessage = notificationMessage,
            errorMessage = if (hasCriticalError) "Unable to load provider dashboard." else null
        )

        startTripLocationListener(trackingBooking?.id)
    }

    private fun startTripLocationListener(bookingId: String?) {
        tripLocationJob?.cancel()
        if (bookingId.isNullOrBlank()) {
            _state.value = _state.value.copy(liveVehicleLat = null, liveVehicleLng = null)
            return
        }

        tripLocationJob = viewModelScope.launch {
            logisticsRepository.observeTripLocation(bookingId)
                .catch { error ->
                    Log.e("ProviderDashboard", "[TRIP_LOCATION] Failed to observe live trip location: ${error.message}", error)
                }
                .collect { tripLocation ->
                    _state.value = _state.value.copy(
                        liveVehicleLat = tripLocation.lat,
                        liveVehicleLng = tripLocation.lng
                    )
                }
        }
    }

    private fun computeOnTimePercent(bookings: List<Booking>): Int {
        val completed = bookings.filter { it.status == BookingStatus.COMPLETED }
        val completedVerified = completed.count { it.isOtpVerified }
        return if (completed.isNotEmpty()) {
            (completedVerified * 100) / completed.size
        } else {
            0
        }
    }

    private fun sumEarningsSince(bookings: List<Booking>, since: ZonedDateTime): Double {
        return bookings
            .filter { it.createdAt > 0L }
            .filter { Instant.ofEpochMilli(it.createdAt).atZone(now().zone).isAfter(since) }
            .sumOf { it.totalFare }
    }

    private fun ageLabel(createdAt: Long): String {
        if (createdAt <= 0L) return "now"
        val minutes = ((System.currentTimeMillis() - createdAt) / 60000L).coerceAtLeast(0L)
        return when {
            minutes < 1L -> "just now"
            minutes < 60L -> "$minutes mins ago"
            else -> "${minutes / 60L}h ago"
        }
    }

    private fun etaLabel(scheduledAt: Long): String {
        if (scheduledAt <= 0L) return "ETA unavailable"
        val minutes = ((scheduledAt - System.currentTimeMillis()) / 60000L)
        return when {
            minutes <= 0L -> "Arriving soon"
            minutes < 60L -> "${minutes}m"
            else -> "${minutes / 60L}h ${minutes % 60L}m"
        }
    }

    private fun timeLabel(epochMs: Long): String {
        if (epochMs <= 0L) return "TBD"
        val dateTime = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
        val hour = dateTime.hour
        val minute = dateTime.minute.toString().padStart(2, '0')
        val amPm = if (hour >= 12) "PM" else "AM"
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "Today, $h12:$minute $amPm"
    }

    private fun estimateDropoffLabel(scheduledAt: Long, distanceKm: Double): String {
        val start = if (scheduledAt > 0L) scheduledAt else System.currentTimeMillis()
        val minutesToAdd = (estimateTripHours(distanceKm) * 60).toLong()
        return "Est. ${timeLabel(start + minutesToAdd * 60_000L).removePrefix("Today, ")}"
    }

    private fun estimateTripHours(distanceKm: Double): Double {
        if (distanceKm <= 0.0) return 0.0
        val assumedSpeedKmh = 68.0
        return distanceKm / assumedSpeedKmh
    }

    private fun routeDistanceKm(booking: Booking): Double {
        if (booking.pickupLat == 0.0 || booking.dropOffLat == 0.0) return 0.0
        return LocationUtils.calculateDistanceKm(
            LatLng(booking.pickupLat, booking.pickupLng),
            LatLng(booking.dropOffLat, booking.dropOffLng)
        )
    }

    private fun buildBookingCode(bookingId: String): String {
        val suffix = bookingId.takeLast(4).ifBlank { "0000" }
        return "#MV-${ZonedDateTime.now().year}-$suffix"
    }

    private fun planForFare(fare: Double): String {
        return when {
            fare >= 100000 -> "MoveMax"
            fare >= 50000 -> "MoveBig"
            else -> "MoveLite"
        }
    }

    private fun buildAssignmentOptions(
        bookingId: String,
        vehicles: List<Vehicle>,
        drivers: List<Driver>
    ): List<ProviderAssignmentOptionUi> {
        val driversByVehicleId = drivers
            .filter { it.vehicleId.isNotBlank() }
            .groupBy { it.vehicleId }

        return vehicles.mapNotNull { vehicle ->
            val driver = driversByVehicleId[vehicle.id]?.firstOrNull() ?: return@mapNotNull null
            ProviderAssignmentOptionUi(
                bookingId = bookingId,
                vehicleId = vehicle.id,
                driverId = driver.id,
                vehicleLabel = formatVehicle(vehicle),
                driverLabel = driver.name.ifBlank { "Driver " + driver.licenseNo.ifBlank { "#" + driver.id.takeLast(4) } },
                serviceTag = vehicle.type.ifBlank { "MoveLite" }, // Based on vehicle type instead of requested service
                isAvailable = driver.status == "Available",
                rating = driver.rating,
                trips = driver.tripsCount,
                statusLabel = driver.status
            )
        }.sortedByDescending { it.isAvailable }
    }

    private fun formatVehicle(vehicle: Vehicle?): String {
        if (vehicle == null) return "Vehicle unassigned"
        val model = listOf(vehicle.make, vehicle.model)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { vehicle.type.ifBlank { "Vehicle" } }
        return "$model - ${vehicle.plateNumber.ifBlank { "N/A" }}"
    }

    private fun now(): ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
}

data class ProviderDashboardUiState(
    val isLoading: Boolean = false,
    val providerDisplayName: String = "Provider",
    val vehiclesCount: Int = 0,
    val driversCount: Int = 0,
    val activeJobsCount: Int = 0,
    val earningsToday: Double = 0.0,
    val earningsThisWeek: Double = 0.0,
    val earningsThisMonth: Double = 0.0,
    val rating: Double = 0.0,
    val trips: Int = 0,
    val onTimePercent: Int = 0,
    val newRequests: List<ProviderNewRequestUi> = emptyList(),
    val activeJobs: List<ProviderActiveJobUi> = emptyList(),
    val activeTrackingBookingId: String? = null,
    val activeTrackingUserId: String? = null,
    val activeTrackingVehicleId: String? = null,
    val liveVehicleLat: Double? = null,
    val liveVehicleLng: Double? = null,
    val showNewRequestNotification: Boolean = false,
    val newRequestNotificationMessage: String? = null,
    val errorMessage: String? = null
)

data class ProviderNewRequestUi(
    val bookingId: String,
    val bookingCode: String,
    val userId: String,
    val service: String,
    val ageLabel: String,
    val pickup: String,
    val destination: String,
    val pickupScheduleLabel: String,
    val dropOffEstimateLabel: String,
    val distanceKm: Double,
    val estimatedHours: Double,
    val totalFare: Double,
    val providerEarnings: Double,
    val instructions: String,
    val customerName: String,
    val customerPhone: String?,
    val customerRating: Double,
    val customerBookingCount: Int,
    val matchingOptions: List<ProviderAssignmentOptionUi>,
    val otherOptions: List<ProviderAssignmentOptionUi>
)

data class ProviderAssignmentOptionUi(
    val bookingId: String,
    val vehicleId: String,
    val driverId: String,
    val vehicleLabel: String,
    val driverLabel: String,
    val serviceTag: String,
    val isAvailable: Boolean,
    val rating: Double,
    val trips: Int,
    val statusLabel: String
)

data class ProviderActiveJobUi(
    val bookingId: String,
    val userId: String,
    val service: String,
    val status: String,
    val code: String,
    val pickup: String,
    val destination: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropOffLat: Double,
    val dropOffLng: Double,
    val driver: String,
    val eta: String,
    val vehicle: String,
    val customerName: String,
    val customerPhone: String?,
    val boxesCount: Int,
    val distanceKm: Double,
    val totalFare: Double,
    val assignedVehicleId: String?,
    val otp: String
)
