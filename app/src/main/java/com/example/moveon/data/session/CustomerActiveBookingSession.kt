package com.example.moveon.data.session

import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CustomerActiveBookingState {
    data object Loading : CustomerActiveBookingState

    data class Ready(
        val booking: Booking?,
        val providerName: String?
    ) : CustomerActiveBookingState

    data class Error(val message: String) : CustomerActiveBookingState
}

@Singleton
class CustomerActiveBookingSession @Inject constructor(
    private val logisticsRepository: LogisticsRepository,
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow<CustomerActiveBookingState>(CustomerActiveBookingState.Loading)
    val state: StateFlow<CustomerActiveBookingState> = _state.asStateFlow()

    private var lastUserId: String? = null

    init {
        scope.launch {
            authRepository.currentUser.collect { user ->
                if (user == null) {
                    lastUserId = null
                    _state.value = CustomerActiveBookingState.Ready(null, null)
                } else {
                    lastUserId = user.id
                    _state.value = CustomerActiveBookingState.Loading
                    refreshInternal(user.id)
                }
            }
        }
    }

    suspend fun refresh() {
        val userId = lastUserId ?: return
        _state.value = CustomerActiveBookingState.Loading
        refreshInternal(userId)
    }

    private suspend fun refreshInternal(userId: String) {
        logisticsRepository.getCurrentBookingForUser(userId)
            .onSuccess { booking ->
                if (booking == null) {
                    _state.value = CustomerActiveBookingState.Ready(null, null)
                } else {
                    val providerName = logisticsRepository.getProviderById(booking.providerId)
                        .getOrNull()
                        ?.establishmentName
                        ?.takeIf { it.isNotBlank() }
                        ?: "Assigned Provider"
                    _state.value = CustomerActiveBookingState.Ready(booking, providerName)
                }
            }
            .onFailure { e ->
                _state.value = CustomerActiveBookingState.Error(
                    e.message ?: "Unable to load your move details right now."
                )
            }
    }

    fun onBookingUpdated(booking: Booking?) {
        scope.launch {
            when {
                booking == null -> {
                    _state.value = CustomerActiveBookingState.Ready(null, null)
                }
                booking.status == BookingStatus.COMPLETED -> {
                    _state.value = CustomerActiveBookingState.Ready(null, null)
                }
                else -> {
                    val providerName = logisticsRepository.getProviderById(booking.providerId)
                        .getOrNull()
                        ?.establishmentName
                        ?.takeIf { it.isNotBlank() }
                        ?: "Assigned Provider"
                    _state.value = CustomerActiveBookingState.Ready(booking, providerName)
                }
            }
        }
    }
}
