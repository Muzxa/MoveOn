package com.example.moveon.ui.features.provider

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

        val requestItems = newRequests.take(2).map { booking ->
            ProviderNewRequestUi(
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

        val hasCriticalError = providerResult.isFailure && vehiclesResult.isFailure && driversResult.isFailure && bookingsResult.isFailure
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
            errorMessage = if (hasCriticalError) "Unable to load provider dashboard." else null
        )
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
    val errorMessage: String? = null
)

data class ProviderNewRequestUi(
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
