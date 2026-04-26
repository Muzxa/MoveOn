package com.example.moveon.ui.features.book

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.User
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _state = mutableStateOf(BookUiState(isLoadingProviders = true))
    val state: State<BookUiState> = _state

    init {
        refreshProviders()
    }

    fun refreshProviders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingProviders = true,
                providersError = null
            )

            runCatching { logisticsRepository.getMarketplaceProviders() }
                .onSuccess { providers ->
                    _state.value = _state.value.copy(
                        isLoadingProviders = false,
                        providers = providers,
                        providersError = null
                    )
                }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(
                        isLoadingProviders = false,
                        providers = emptyList(),
                        providersError = throwable.message ?: "Unable to load providers right now."
                    )
                }
        }
    }

    fun onServiceSelected(serviceId: String) {
        _state.value = _state.value.copy(
            selectedServiceId = serviceId,
            selectedProviderId = "",
            formError = null,
            bookingError = null
        )
    }

    fun onProviderSelected(providerId: String) {
        _state.value = _state.value.copy(
            selectedProviderId = providerId,
            formError = null,
            bookingError = null
        )
    }

    fun onPickupAddressChanged(value: String) {
        _state.value = _state.value.copy(
            pickupAddress = value,
            formError = null,
            bookingError = null
        )
    }

    fun onDropOffAddressChanged(value: String) {
        _state.value = _state.value.copy(
            dropOffAddress = value,
            formError = null,
            bookingError = null
        )
    }

    fun onDistanceKmChanged(value: String) {
        val normalized = normalizeDistanceInput(value)
        _state.value = _state.value.copy(
            distanceKmText = normalized,
            formError = null,
            bookingError = null
        )
    }

    fun onDatePicked(dateMillis: Long) {
        val localDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        _state.value = _state.value.copy(
            selectedDateMillis = dateMillis,
            scheduledDateText = localDate.format(dateFormatter),
            formError = null,
            bookingError = null
        )
    }

    fun onTimePicked(hour: Int, minute: Int) {
        val localTime = LocalTime.of(hour, minute)
        _state.value = _state.value.copy(
            selectedHour = hour,
            selectedMinute = minute,
            scheduledTimeText = localTime.format(timeFormatter),
            formError = null,
            bookingError = null
        )
    }

    fun scheduledDateTimeMillis(): Long? {
        val stateSnapshot = _state.value
        val dateMillis = stateSnapshot.selectedDateMillis ?: return null
        val hour = stateSnapshot.selectedHour ?: return null
        val minute = stateSnapshot.selectedMinute ?: return null

        val selectedDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return selectedDate
            .atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun onStepAdvance() {
        val stateSnapshot = _state.value
        if (stateSnapshot.currentStep >= TOTAL_STEPS) return

        val validationMessage = validateStep(stateSnapshot.currentStep)
        if (validationMessage != null) {
            _state.value = stateSnapshot.copy(formError = validationMessage)
            return
        }

        _state.value = stateSnapshot.copy(
            currentStep = stateSnapshot.currentStep + 1,
            formError = null
        )
    }

    fun onPrimaryAction() {
        val snapshot = _state.value
        if (snapshot.currentStep < TOTAL_STEPS) {
            onStepAdvance()
            return
        }

        if (snapshot.createdBooking != null) {
            resetForNewBooking()
            return
        }

        submitBooking()
    }

    fun onStepBack() {
        if (_state.value.currentStep > 1) {
            _state.value = _state.value.copy(
                currentStep = _state.value.currentStep - 1,
                formError = null,
                bookingError = null
            )
        }
    }

    fun canAdvance(): Boolean {
        val stateSnapshot = _state.value
        return when (stateSnapshot.currentStep) {
            1 -> stateSnapshot.selectedServiceId.isNotBlank()
            2 -> stateSnapshot.selectedProviderId.isNotBlank()
            3 -> {
                val distanceValue = stateSnapshot.distanceKmText.toDoubleOrNull()
                val scheduledMillis = scheduledDateTimeMillis()
                stateSnapshot.pickupAddress.isNotBlank() &&
                    stateSnapshot.dropOffAddress.isNotBlank() &&
                    distanceValue != null &&
                    distanceValue > 0.0 &&
                    scheduledMillis != null &&
                    scheduledMillis > System.currentTimeMillis()
            }

            else -> !stateSnapshot.isSubmittingBooking
        }
    }

    fun dismissOtpDialog() {
        _state.value = _state.value.copy(showOtpDialog = false)
    }

    fun openOtpDialog() {
        if (_state.value.createdBooking != null) {
            _state.value = _state.value.copy(showOtpDialog = true)
        }
    }

    private fun submitBooking() {
        val snapshot = _state.value
        if (snapshot.isSubmittingBooking) return

        val userId = currentUser.value?.id
        if (userId.isNullOrBlank()) {
            _state.value = snapshot.copy(bookingError = "Please login again to continue booking.")
            return
        }

        val selectedProvider = snapshot.providers.firstOrNull { it.id == snapshot.selectedProviderId }
        if (selectedProvider == null) {
            _state.value = snapshot.copy(bookingError = "Please select a provider before confirming.")
            return
        }

        val distanceKm = snapshot.distanceKmText.toDoubleOrNull()
        if (distanceKm == null || distanceKm <= 0.0) {
            _state.value = snapshot.copy(bookingError = "Please enter a valid distance in kilometers.")
            return
        }

        val scheduledAt = scheduledDateTimeMillis()
        if (scheduledAt == null) {
            _state.value = snapshot.copy(bookingError = "Please pick both date and time.")
            return
        }

        if (scheduledAt <= System.currentTimeMillis()) {
            _state.value = snapshot.copy(bookingError = "Booking time must be in the future.")
            return
        }

        val fare = selectedProvider.baseRate + (selectedProvider.ratePerKm * distanceKm)
        val otp = generateOtpCode()
        val bookingToCreate = Booking(
            id = "",
            userId = userId,
            providerId = selectedProvider.id,
            status = BookingStatus.SEARCHING,
            pickupAddress = snapshot.pickupAddress.trim(),
            dropOffAddress = snapshot.dropOffAddress.trim(),
            totalFare = fare,
            otp = otp,
            isOtpVerified = false,
            createdAt = System.currentTimeMillis(),
            scheduledTime = scheduledAt,
            rating = 0f
        )

        _state.value = snapshot.copy(
            isSubmittingBooking = true,
            bookingError = null
        )

        viewModelScope.launch {
            logisticsRepository.createBooking(bookingToCreate)
                .onSuccess { createdBooking ->
                    _state.value = _state.value.copy(
                        isSubmittingBooking = false,
                        bookingError = null,
                        createdBooking = createdBooking,
                        showOtpDialog = true
                    )
                }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(
                        isSubmittingBooking = false,
                        bookingError = throwable.message ?: "Could not confirm booking right now."
                    )
                }
        }
    }

    private fun resetForNewBooking() {
        val providersSnapshot = _state.value.providers
        val providersLoading = _state.value.isLoadingProviders

        _state.value = BookUiState(
            providers = providersSnapshot,
            isLoadingProviders = providersLoading,
            providersError = null
        )
    }

    private fun validateStep(step: Int): String? {
        val stateSnapshot = _state.value
        return when (step) {
            1 -> if (stateSnapshot.selectedServiceId.isBlank()) {
                "Select a service type to continue."
            } else {
                null
            }

            2 -> if (stateSnapshot.selectedProviderId.isBlank()) {
                "Select a provider to continue."
            } else {
                null
            }

            3 -> {
                when {
                    stateSnapshot.pickupAddress.isBlank() -> "Pickup address is required."
                    stateSnapshot.dropOffAddress.isBlank() -> "Drop-off address is required."
                    stateSnapshot.distanceKmText.toDoubleOrNull() == null -> "Distance must be a number."
                    stateSnapshot.distanceKmText.toDoubleOrNull()?.let { it <= 0.0 } == true -> {
                        "Distance must be greater than zero."
                    }

                    scheduledDateTimeMillis() == null -> "Please pick both date and time."
                    scheduledDateTimeMillis()?.let { it <= System.currentTimeMillis() } == true -> {
                        "Schedule must be set in the future."
                    }

                    else -> null
                }
            }

            else -> null
        }
    }

    private fun normalizeDistanceInput(value: String): String {
        val builder = StringBuilder()
        var dotSeen = false

        value.forEach { ch ->
            if (ch.isDigit()) {
                builder.append(ch)
            } else if (ch == '.' && !dotSeen) {
                builder.append(ch)
                dotSeen = true
            }
        }

        return builder.toString()
    }

    private fun generateOtpCode(): String {
        val code = Random.nextInt(from = 1000, until = 10000)
        return code.toString()
    }

    companion object {
        const val TOTAL_STEPS = 4
    }
}

data class BookUiState(
    val currentStep: Int = 1,
    val selectedServiceId: String = "",
    val selectedProviderId: String = "",
    val pickupAddress: String = "",
    val dropOffAddress: String = "",
    val distanceKmText: String = "",
    val selectedDateMillis: Long? = null,
    val selectedHour: Int? = null,
    val selectedMinute: Int? = null,
    val scheduledDateText: String = "",
    val scheduledTimeText: String = "",
    val providers: List<Provider> = emptyList(),
    val isLoadingProviders: Boolean = false,
    val providersError: String? = null,
    val formError: String? = null,
    val isSubmittingBooking: Boolean = false,
    val bookingError: String? = null,
    val createdBooking: Booking? = null,
    val showOtpDialog: Boolean = false
)
