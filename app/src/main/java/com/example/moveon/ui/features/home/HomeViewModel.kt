package com.example.moveon.ui.features.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.data.session.CustomerActiveBookingSession
import com.example.moveon.data.session.CustomerActiveBookingState
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.User
import com.example.moveon.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerActiveBookingSession: CustomerActiveBookingSession,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _homeState = mutableStateOf(HomeUiState(isLoading = true))
    val homeState: State<HomeUiState> = _homeState

    init {
        viewModelScope.launch {
            combine(currentUser, customerActiveBookingSession.state) { user, bookingState ->
                Pair(user, bookingState)
            }.collect { (user, bookingState) ->
                if (user == null) {
                    _homeState.value = HomeUiState(
                        isLoading = false,
                        errorMessage = "Could not load your profile."
                    )
                    return@collect
                }

                val base = HomeUiState(
                    profileName = "${user.firstName} ${user.lastName}".trim().ifBlank { "MoveOn User" },
                    profileInitials = initialsOf(user.firstName, user.lastName),
                    profilePhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString()
                )

                _homeState.value = when (bookingState) {
                    is CustomerActiveBookingState.Loading -> {
                        base.copy(
                            isLoading = true,
                            activeMove = null,
                            errorMessage = null
                        )
                    }
                    is CustomerActiveBookingState.Error -> {
                        base.copy(
                            isLoading = false,
                            activeMove = null,
                            errorMessage = bookingState.message
                        )
                    }
                    is CustomerActiveBookingState.Ready -> {
                        val move = bookingState.booking?.toActiveMoveUi(
                            bookingState.providerName?.takeIf { it.isNotBlank() } ?: "Assigned Provider"
                        )
                        base.copy(
                            isLoading = false,
                            activeMove = move,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            customerActiveBookingSession.refresh()
        }
    }

    private fun initialsOf(firstName: String, lastName: String): String {
        val first = firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val second = lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return (first + second).ifBlank { "MO" }
    }

    private fun Booking.toActiveMoveUi(providerName: String = "Assigned Provider"): ActiveMoveUi {
        return ActiveMoveUi(
            moveId = id,
            statusLabel = status.toUiLabel(),
            pickupAddress = pickupAddress,
            dropOffAddress = dropOffAddress,
            providerLabel = if (providerName.isBlank()) "Provider assigned soon" else providerName,
            etaLabel = estimateEta(scheduledTime)
        )
    }

    private fun BookingStatus.toUiLabel(): String {
        return when (this) {
            BookingStatus.SEARCHING -> "Searching"
            BookingStatus.CONFIRMED -> "Confirmed"
            BookingStatus.ACTIVE -> "Active"
            BookingStatus.COMPLETED -> "Completed"
        }
    }

    private fun estimateEta(scheduledTime: Long): String {
        if (scheduledTime <= 0L) return "ETA unavailable"

        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(scheduledTime - System.currentTimeMillis())
        return when {
            diffMinutes <= 0L -> "Arriving soon"
            diffMinutes < 60L -> "$diffMinutes mins"
            else -> "${diffMinutes / 60}h ${diffMinutes % 60}m"
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val profileName: String = "MoveOn User",
    val profileInitials: String = "MO",
    val profilePhotoUrl: String? = null,
    val activeMove: ActiveMoveUi? = null,
    val errorMessage: String? = null
)

data class ActiveMoveUi(
    val moveId: String,
    val statusLabel: String,
    val pickupAddress: String,
    val dropOffAddress: String,
    val providerLabel: String,
    val etaLabel: String
)
