package com.example.moveon.ui.features.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.User
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.InventoryRepository
import com.example.moveon.domain.repository.LogisticsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository,
    private val inventoryRepository: InventoryRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _profileState = mutableStateOf(ProfileUiState(isLoading = true))
    val profileState: State<ProfileUiState> = _profileState

    private val _eventFlow = MutableSharedFlow<ProfileUiEvent>()
    val eventFlow: SharedFlow<ProfileUiEvent> = _eventFlow.asSharedFlow()

    init {
        observeUser()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Refresh -> refresh()
            ProfileEvent.Logout -> logout()
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _profileState.value = ProfileUiState(
                        isLoading = false,
                        errorMessage = "Could not load your profile."
                    )
                } else {
                    loadProfile(user)
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not load your profile."
                )
                return@launch
            }
            loadProfile(user)
        }
    }

    private suspend fun loadProfile(user: User) {
        _profileState.value = _profileState.value.copy(
            isLoading = true,
            displayName = "${user.firstName} ${user.lastName}".trim().ifBlank { "MoveOn User" },
            email = user.email,
            initials = initialsOf(user.firstName, user.lastName),
            profilePhotoUrl = firebaseAuth.currentUser?.photoUrl?.toString(),
            memberSinceDate = formatMemberSince(user.createdAt),
            errorMessage = null
        )

        val bookingsDeferred = viewModelScope.async {
            logisticsRepository.getBookingsForUser(user.id)
        }
        val boxesDeferred = viewModelScope.async {
            inventoryRepository.getTotalBoxesCount()
        }

        val bookingsResult = bookingsDeferred.await()
        val totalBoxes = boxesDeferred.await()

        bookingsResult
            .onSuccess { bookings ->
                val completedRated = bookings.filter {
                    it.status == BookingStatus.COMPLETED && it.rating > 0f
                }
                val avgRating = if (completedRated.isEmpty()) {
                    0f
                } else {
                    completedRated.map { it.rating }.average().toFloat()
                }

                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    totalMoves = bookings.size,
                    totalBoxes = totalBoxes,
                    averageRating = avgRating,
                    errorMessage = null
                )
            }
            .onFailure { throwable ->
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    totalBoxes = totalBoxes,
                    errorMessage = throwable.message ?: "Unable to load your profile stats right now."
                )
            }
    }

    private fun formatMemberSince(createdAt: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            sdf.format(Date(createdAt))
        } catch (t: Throwable) {
            ""
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _eventFlow.emit(ProfileUiEvent.NavigateToLogin)
        }
    }

    private fun initialsOf(firstName: String, lastName: String): String {
        val first = firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val second = lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return (first + second).ifBlank { "MO" }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val displayName: String = "MoveOn User",
    val email: String = "",
    val initials: String = "MO",
    val profilePhotoUrl: String? = null,
    val memberSinceDate: String = "",
    val totalMoves: Int = 0,
    val totalBoxes: Int = 0,
    val averageRating: Float = 0f,
    val errorMessage: String? = null
)

sealed class ProfileEvent {
    object Refresh : ProfileEvent()
    object Logout : ProfileEvent()
}

sealed class ProfileUiEvent {
    object NavigateToLogin : ProfileUiEvent()
}
