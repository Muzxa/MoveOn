package com.example.moveon.ui.features.book

import android.util.Log
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
import com.example.moveon.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
    private val logisticsRepository: LogisticsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _state = mutableStateOf(BookUiState(isLoadingProviders = true))
    val state: State<BookUiState> = _state

    private var vehicleTrackingJob: Job? = null
    private var bookingStatusListenerJob: Job? = null

    init {
        // Try to restore form state from SavedStateHandle (in case ViewModel was recreated)
        restoreFormState()
        refreshProviders()
        loadCurrentBookingForUser()
        Log.d("BookViewModel", "[INIT] BookViewModel initialized")
    }

    private fun loadCurrentBookingForUser() {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) return@collect
                try {
                    val current = logisticsRepository.getCurrentBookingForUser(user.id)
                    current.onSuccess { booking ->
                        if (booking != null) {
                            Log.d("BookViewModel", "[INIT] Found active booking for user ${user.id}: ${booking.id}")
                            // fetch provider details for nicer UI labels
                            val provider = runCatching { logisticsRepository.getProviderById(booking.providerId) }
                                .getOrNull()?.getOrNull()

                            _state.value = _state.value.copy(
                                createdBooking = booking,
                                createdProvider = provider,
                                isWaitingForProviderResponse = booking.status == BookingStatus.SEARCHING
                            )
                            // start listeners for booking
                            startBookingStatusListener(booking.id)
                            startVehicleTracking(booking)
                        }
                    }.onFailure { err ->
                        Log.e("BookViewModel", "[INIT] Failed to load current booking for user ${user.id}: ${err.message}", err)
                    }
                } catch (e: Exception) {
                    Log.e("BookViewModel", "[INIT] Exception while loading current booking: ${e.message}", e)
                }
            }
        }
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
        saveFormState()
    }

    fun onProviderSelected(providerId: String) {
        _state.value = _state.value.copy(
            selectedProviderId = providerId,
            formError = null,
            bookingError = null
        )
        saveFormState()
    }

    fun onPickupAddressChanged(value: String) {
        _state.value = _state.value.copy(
            pickupAddress = value,
            formError = null,
            bookingError = null
        )
        saveFormState()
    }

    fun onDropOffAddressChanged(value: String) {
        _state.value = _state.value.copy(
            dropOffAddress = value,
            formError = null,
            bookingError = null
        )
        saveFormState()
    }

    fun onPickupLocationResolved(lat: Double, lng: Double, address: String) {
        _state.value = _state.value.copy(
            pickupAddress = address,
            pickupLat = lat,
            pickupLng = lng,
            formError = null,
            bookingError = null
        )
        recalculateDistance()
        saveFormState()
    }

    fun onDropOffLocationResolved(lat: Double, lng: Double, address: String) {
        _state.value = _state.value.copy(
            dropOffAddress = address,
            dropOffLat = lat,
            dropOffLng = lng,
            formError = null,
            bookingError = null
        )
        recalculateDistance()
        saveFormState()
    }

    private fun recalculateDistance() {
        val snap = _state.value
        val pLat = snap.pickupLat
        val pLng = snap.pickupLng
        val dLat = snap.dropOffLat
        val dLng = snap.dropOffLng
        if (pLat != null && pLng != null && dLat != null && dLng != null) {
            val distanceKm = LocationUtils.calculateDistanceKm(
                LatLng(pLat, pLng),
                LatLng(dLat, dLng)
            )
            _state.value = _state.value.copy(
                distanceKmText = String.format(Locale.US, "%.1f", distanceKm)
            )
        }
    }

    fun onDistanceKmChanged(value: String) {
        val normalized = normalizeDistanceInput(value)
        _state.value = _state.value.copy(
            distanceKmText = normalized,
            formError = null,
            bookingError = null
        )
        saveFormState()
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
        saveFormState()
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
        saveFormState()
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
        saveFormState()
    }

    fun onPrimaryAction() {
        val snapshot = _state.value
        val booking = snapshot.createdBooking
        // Tracking flow: primary button shows "View OTP" — never run step validation then.
        if (booking != null && booking.status != BookingStatus.COMPLETED) {
            openOtpDialog()
            return
        }

        if (snapshot.currentStep < TOTAL_STEPS) {
            onStepAdvance()
            return
        }

        submitBooking()
    }

    fun startNewBooking() {
        clearBookingTrackingJobs()
        resetForNewBooking()
    }

    fun dismissWaitingForProviderResponse() {
        clearBookingTrackingJobs()
        _state.value = _state.value.copy(
            createdBooking = null,
            showOtpDialog = false,
            isWaitingForProviderResponse = false,
            bookingStatusError = null,
            bookingError = null,
            isSubmittingBooking = false,
            vehicleLat = null,
            vehicleLng = null
        )
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
            Log.e("BookViewModel", "[BOOKING_SUBMIT] User ID is null or blank")
            _state.value = snapshot.copy(bookingError = "Unable to process booking. Please try again.")
            return
        }


        val selectedProvider = snapshot.providers.firstOrNull { it.id == snapshot.selectedProviderId }
        if (selectedProvider == null) {
            Log.e("BookViewModel", "[BOOKING_SUBMIT] Selected provider not found: ${snapshot.selectedProviderId}")
            _state.value = snapshot.copy(bookingError = "Please select a provider before confirming.")
            return
        }

        val distanceKm = snapshot.distanceKmText.toDoubleOrNull()
        if (distanceKm == null || distanceKm <= 0.0) {
            Log.e("BookViewModel", "[BOOKING_SUBMIT] Invalid distance: ${snapshot.distanceKmText}")
            _state.value = snapshot.copy(bookingError = "Please enter a valid distance in kilometers.")
            return
        }

        val scheduledAt = scheduledDateTimeMillis()
        if (scheduledAt == null) {
            Log.e("BookViewModel", "[BOOKING_SUBMIT] Scheduled time is null")
            _state.value = snapshot.copy(bookingError = "Please pick both date and time.")
            return
        }

        if (scheduledAt <= System.currentTimeMillis()) {
            Log.e("BookViewModel", "[BOOKING_SUBMIT] Booking time is in the past")
            _state.value = snapshot.copy(bookingError = "Booking time must be in the future.")
            return
        }

        val fare = 500.0 + (25.0 * distanceKm)
        val otp = generateOtpCode()
        val bookingToCreate = Booking(
            id = "",
            userId = userId,
            providerId = selectedProvider.id,
            status = BookingStatus.SEARCHING,
            pickupAddress = snapshot.pickupAddress.trim(),
            dropOffAddress = snapshot.dropOffAddress.trim(),
            pickupLat = snapshot.pickupLat ?: 0.0,
            pickupLng = snapshot.pickupLng ?: 0.0,
            dropOffLat = snapshot.dropOffLat ?: 0.0,
            dropOffLng = snapshot.dropOffLng ?: 0.0,
            totalFare = fare,
            otp = otp,
            isOtpVerified = false,
            createdAt = System.currentTimeMillis(),
            scheduledTime = scheduledAt,
            rating = 0f
        )

        Log.d("BookViewModel", "[BOOKING_SUBMIT] Creating booking - User: $userId, Provider: ${selectedProvider.id}, Distance: ${distanceKm}km, Fare: $fare")

        _state.value = snapshot.copy(
            isSubmittingBooking = true,
            bookingError = null
        )

        viewModelScope.launch {
            // Double-check server-side current booking for user to prevent duplicates
            try {
                val existingResult = logisticsRepository.getCurrentBookingForUser(userId)
                existingResult.onSuccess { existingBooking ->
                    if (existingBooking != null && existingBooking.status != BookingStatus.COMPLETED) {
                        Log.w("BookViewModel", "[BOOKING_SUBMIT] Aborting - user $userId already has active booking ${existingBooking.id}")
                        _state.value = _state.value.copy(
                            isSubmittingBooking = false,
                            bookingError = "You already have an active booking."
                        )
                        return@launch
                    }
                }.onFailure { err ->
                    Log.w("BookViewModel", "[BOOKING_SUBMIT] Could not verify existing booking: ${err.message}")
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "[BOOKING_SUBMIT] Exception checking existing booking: ${e.message}", e)
            }

            logisticsRepository.createBooking(bookingToCreate)
                .onSuccess { createdBooking ->
                    Log.d("BookViewModel", "[BOOKING_SUBMIT] Booking created successfully: ${createdBooking.id}, waiting for provider response")
                    // fetch provider details for display
                    val provider = runCatching { logisticsRepository.getProviderById(createdBooking.providerId) }
                        .getOrNull()?.getOrNull()

                    _state.value = _state.value.copy(
                        isSubmittingBooking = false,
                        bookingError = null,
                        createdBooking = createdBooking,
                        createdProvider = provider,
                        isWaitingForProviderResponse = true,
                        showOtpDialog = false
                    )
                    // Start listening to booking status changes
                    startBookingStatusListener(createdBooking.id)
                    // Start vehicle tracking if booking has vehicles
                    startVehicleTracking(createdBooking)
                }
                .onFailure { throwable ->
                    Log.e("BookViewModel", "[BOOKING_SUBMIT] Booking creation failed: ${throwable.message}", throwable)
                    _state.value = _state.value.copy(
                        isSubmittingBooking = false,
                        bookingError = throwable.message ?: "Could not confirm booking right now."
                    )
                }
        }
    }

    private fun startVehicleTracking(booking: Booking) {
        val vehicleId = booking.vehicles.firstOrNull()?.vehicleId
        Log.d("BookViewModel", "[TRACKING_START] Starting vehicle tracking for booking: ${booking.id}, vehicleId: $vehicleId")

        vehicleTrackingJob?.cancel()
        vehicleTrackingJob = viewModelScope.launch {
            try {
                Log.d("BookViewModel", "[TRIP_LOCATION_SUBSCRIBE] Subscribing to trip location updates for booking: ${booking.id}")
                logisticsRepository.observeTripLocation(booking.id)
                    .collect { tripLocation ->
                        Log.d("BookViewModel", "[TRIP_LOCATION_COLLECTED] Received location update - Lat: ${tripLocation.lat}, Lng: ${tripLocation.lng}, ActorId: ${tripLocation.actorId}")
                        _state.value = _state.value.copy(
                            vehicleLat = tripLocation.lat,
                            vehicleLng = tripLocation.lng
                        )
                    }
            } catch (ex: Exception) {
                Log.e("BookViewModel", "[TRIP_LOCATION_ERROR] Failed to observe trip location for ${booking.id}: ${ex.message}", ex)
                if (!vehicleId.isNullOrBlank()) {
                    Log.d("BookViewModel", "[FALLBACK_TRACKING] Falling back to legacy vehicle location tracking for vehicleId: $vehicleId")
                    logisticsRepository.trackVehicleLocation(vehicleId)
                        .catch { error ->
                            Log.e("BookViewModel", "[FALLBACK_ERROR] Vehicle tracking failed: ${error.message}", error as Throwable)
                        }
                        .collect { latLng ->
                            Log.d("BookViewModel", "[VEHICLE_LOCATION] Received fallback location - Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
                            _state.value = _state.value.copy(
                                vehicleLat = latLng.latitude,
                                vehicleLng = latLng.longitude
                            )
                        }
                } else {
                    Log.w("BookViewModel", "[NO_VEHICLE] Booking has no vehicles assigned")
                }
            }
        }
    }

    private fun startBookingStatusListener(bookingId: String) {
        bookingStatusListenerJob?.cancel()
        Log.d("BookViewModel", "[STATUS_LISTENER] Setting up status listener for booking: $bookingId")
        bookingStatusListenerJob = viewModelScope.launch {
            logisticsRepository.observeBookingStatus(bookingId)
                .catch { error ->
                    Log.e("BookViewModel", "[STATUS_LISTENER] Error observing booking status for $bookingId: ${error.message}", error)
                }
                .collect { status ->
                    Log.d("BookViewModel", "[STATUS_LISTENER] Booking $bookingId status changed to: $status")
                    _state.value = _state.value.copy(
                        bookingStatusError = null
                    )
                    when (status) {
                        BookingStatus.CONFIRMED -> {
                            // Provider has accepted the booking
                            Log.d("BookViewModel", "[STATUS_LISTENER] Provider confirmed booking $bookingId")
                            _state.value = _state.value.copy(
                                isWaitingForProviderResponse = false,
                                showOtpDialog = true
                            )
                        }
                        BookingStatus.ACTIVE -> {
                            // Trip has started
                            Log.d("BookViewModel", "[STATUS_LISTENER] Booking $bookingId is now ACTIVE")
                            _state.value = _state.value.copy(
                                isWaitingForProviderResponse = false
                            )
                        }
                        BookingStatus.COMPLETED -> {
                            Log.d("BookViewModel", "[STATUS_LISTENER] Booking $bookingId is now COMPLETED")
                            clearBookingTrackingJobs()
                            _state.value = _state.value.copy(
                                createdBooking = _state.value.createdBooking?.copy(status = BookingStatus.COMPLETED),
                                showOtpDialog = false,
                                isWaitingForProviderResponse = false
                            )
                        }
                        else -> {
                            // SEARCHING - keep current state
                            Log.d("BookViewModel", "[STATUS_LISTENER] Booking $bookingId still in ${status.name}")
                        }
                    }
                }
        }
    }

    private fun clearBookingTrackingJobs() {
        vehicleTrackingJob?.cancel()
        vehicleTrackingJob = null
        bookingStatusListenerJob?.cancel()
        bookingStatusListenerJob = null
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
        val code = Random.nextInt(from = 100000, until = 1000000)
        return code.toString()
    }

    private fun saveFormState() {
        val snapshot = _state.value
        try {
            savedStateHandle["currentStep"] = snapshot.currentStep
            savedStateHandle["selectedServiceId"] = snapshot.selectedServiceId
            savedStateHandle["selectedProviderId"] = snapshot.selectedProviderId
            savedStateHandle["pickupAddress"] = snapshot.pickupAddress
            savedStateHandle["dropOffAddress"] = snapshot.dropOffAddress
            savedStateHandle["pickupLat"] = snapshot.pickupLat
            savedStateHandle["pickupLng"] = snapshot.pickupLng
            savedStateHandle["dropOffLat"] = snapshot.dropOffLat
            savedStateHandle["dropOffLng"] = snapshot.dropOffLng
            savedStateHandle["distanceKmText"] = snapshot.distanceKmText
            savedStateHandle["selectedDateMillis"] = snapshot.selectedDateMillis
            savedStateHandle["selectedHour"] = snapshot.selectedHour
            savedStateHandle["selectedMinute"] = snapshot.selectedMinute
            savedStateHandle["scheduledDateText"] = snapshot.scheduledDateText
            savedStateHandle["scheduledTimeText"] = snapshot.scheduledTimeText
            Log.d("BookViewModel", "[SAVE_STATE] Form state saved to SavedStateHandle")
        } catch (e: Exception) {
            Log.e("BookViewModel", "[SAVE_STATE] Failed to save form state: ${e.message}", e)
        }
    }

    private fun restoreFormState() {
        try {
            val currentStep = savedStateHandle.get<Int>("currentStep") ?: 1
            val selectedServiceId = savedStateHandle.get<String>("selectedServiceId") ?: ""
            val selectedProviderId = savedStateHandle.get<String>("selectedProviderId") ?: ""
            val pickupAddress = savedStateHandle.get<String>("pickupAddress") ?: ""
            val dropOffAddress = savedStateHandle.get<String>("dropOffAddress") ?: ""
            val pickupLat = savedStateHandle.get<Double>("pickupLat")
            val pickupLng = savedStateHandle.get<Double>("pickupLng")
            val dropOffLat = savedStateHandle.get<Double>("dropOffLat")
            val dropOffLng = savedStateHandle.get<Double>("dropOffLng")
            val distanceKmText = savedStateHandle.get<String>("distanceKmText") ?: ""
            val selectedDateMillis = savedStateHandle.get<Long>("selectedDateMillis")
            val selectedHour = savedStateHandle.get<Int>("selectedHour")
            val selectedMinute = savedStateHandle.get<Int>("selectedMinute")
            val scheduledDateText = savedStateHandle.get<String>("scheduledDateText") ?: ""
            val scheduledTimeText = savedStateHandle.get<String>("scheduledTimeText") ?: ""

            if (selectedServiceId.isNotEmpty() || selectedProviderId.isNotEmpty() || pickupAddress.isNotEmpty()) {
                _state.value = _state.value.copy(
                    currentStep = currentStep,
                    selectedServiceId = selectedServiceId,
                    selectedProviderId = selectedProviderId,
                    pickupAddress = pickupAddress,
                    dropOffAddress = dropOffAddress,
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    dropOffLat = dropOffLat,
                    dropOffLng = dropOffLng,
                    distanceKmText = distanceKmText,
                    selectedDateMillis = selectedDateMillis,
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    scheduledDateText = scheduledDateText,
                    scheduledTimeText = scheduledTimeText,
                    isLoadingProviders = true
                )
                Log.d("BookViewModel", "[RESTORE_STATE] Form state restored from SavedStateHandle - Step: $currentStep, Service: $selectedServiceId, Provider: $selectedProviderId")
            }
        } catch (e: Exception) {
            Log.e("BookViewModel", "[RESTORE_STATE] Failed to restore form state: ${e.message}", e)
        }
    }

    companion object {
        const val TOTAL_STEPS = 3
    }
}

data class BookUiState(
    val currentStep: Int = 1,
    val selectedServiceId: String = "",
    val selectedProviderId: String = "",
    val pickupAddress: String = "",
    val dropOffAddress: String = "",
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val dropOffLat: Double? = null,
    val dropOffLng: Double? = null,
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
    val createdProvider: Provider? = null,
    val showOtpDialog: Boolean = false,
    val vehicleLat: Double? = null,
    val vehicleLng: Double? = null,
    val isWaitingForProviderResponse: Boolean = false,
    val bookingStatusError: String? = null
)
