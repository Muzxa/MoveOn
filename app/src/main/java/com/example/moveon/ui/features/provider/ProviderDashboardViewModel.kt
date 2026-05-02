package com.example.moveon.ui.features.provider

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
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
    private var lastPublishedLocation: LatLng? = null
    private var lastPublishedAt: Long = 0L

    fun acceptBooking(bookingId: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val providerId = currentUser.value?.id
                    ?: throw IllegalStateException("Provider session not available")
                logisticsRepository.confirmBookingById(bookingId, providerId)
                onComplete(true, null)
                // refresh list
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

        val requestItems = newRequests.take(2).map { booking ->
            ProviderNewRequestUi(
                bookingId = booking.id,
                service = planForFare(booking.totalFare),
                ageLabel = ageLabel(booking.createdAt),
                pickup = booking.pickupAddress,
                destination = booking.dropOffAddress
            )
        }

        val activeItems = activeJobs.take(2).mapIndexed { index, booking ->
            val driver = drivers.getOrNull(index)
            val vehicle = vehicles.getOrNull(index)
            ProviderActiveJobUi(
                service = planForFare(booking.totalFare),
                status = when (booking.status) {
                    BookingStatus.ACTIVE -> "In Transit"
                    BookingStatus.CONFIRMED -> "Loading"
                    else -> booking.status.name.lowercase().replaceFirstChar { it.uppercaseChar() }
                },
                code = "#${booking.id.takeLast(4)}",
                pickup = booking.pickupAddress,
                destination = booking.dropOffAddress,
                driver = driver?.id?.ifBlank { null } ?: "Unassigned",
                eta = etaLabel(booking.scheduledTime),
                vehicle = formatVehicle(vehicle)
            )
        }

        val trackingBooking = activeJobs.firstOrNull()
        val trackingVehicleId = vehicles.firstOrNull()?.id

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
            showNewRequestNotification = shouldEmitNotification,
            newRequestNotificationMessage = notificationMessage,
            errorMessage = if (hasCriticalError) "Unable to load provider dashboard." else null
        )
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

    private fun planForFare(fare: Double): String {
        return when {
            fare >= 100000 -> "MoveMax"
            fare >= 50000 -> "MoveBig"
            else -> "MoveLite"
        }
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
    val showNewRequestNotification: Boolean = false,
    val newRequestNotificationMessage: String? = null,
    val errorMessage: String? = null
)

data class ProviderNewRequestUi(
    val bookingId: String,
    val service: String,
    val ageLabel: String,
    val pickup: String,
    val destination: String
)

data class ProviderActiveJobUi(
    val service: String,
    val status: String,
    val code: String,
    val pickup: String,
    val destination: String,
    val driver: String,
    val eta: String,
    val vehicle: String
)
