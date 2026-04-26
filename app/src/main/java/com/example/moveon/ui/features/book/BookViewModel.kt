package com.example.moveon.ui.features.book

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Provider
import com.example.moveon.domain.model.User
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

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
            selectedProviderId = ""
        )
    }

    fun onProviderSelected(providerId: String) {
        _state.value = _state.value.copy(selectedProviderId = providerId)
    }

    fun onPickupAddressChanged(value: String) {
        _state.value = _state.value.copy(pickupAddress = value)
    }

    fun onDropOffAddressChanged(value: String) {
        _state.value = _state.value.copy(dropOffAddress = value)
    }

    fun onDistanceKmChanged(value: String) {
        _state.value = _state.value.copy(distanceKmText = value)
    }

    fun onScheduledDateChanged(value: String) {
        _state.value = _state.value.copy(scheduledDateText = value)
    }

    fun onScheduledTimeChanged(value: String) {
        _state.value = _state.value.copy(scheduledTimeText = value)
    }

    fun onStepAdvance() {
        if (_state.value.currentStep < TOTAL_STEPS) {
            _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1)
        }
    }

    fun onStepBack() {
        if (_state.value.currentStep > 1) {
            _state.value = _state.value.copy(currentStep = _state.value.currentStep - 1)
        }
    }

    fun canAdvance(): Boolean {
        return when (_state.value.currentStep) {
            1 -> _state.value.selectedServiceId.isNotBlank()
            2 -> _state.value.selectedProviderId.isNotBlank()
            3 -> {
                _state.value.pickupAddress.isNotBlank() &&
                    _state.value.dropOffAddress.isNotBlank() &&
                    _state.value.distanceKmText.isNotBlank()
            }

            else -> true
        }
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
    val scheduledDateText: String = "",
    val scheduledTimeText: String = "",
    val providers: List<Provider> = emptyList(),
    val isLoadingProviders: Boolean = false,
    val providersError: String? = null
)
