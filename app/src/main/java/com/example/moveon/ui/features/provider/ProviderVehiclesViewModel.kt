package com.example.moveon.ui.features.provider

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.User
import com.example.moveon.domain.model.UserRole
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderVehiclesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = mutableStateOf(VehiclesUiState(isLoading = true))
    val uiState: State<VehiclesUiState> = _uiState

    init {
        observeUserAndLoad()
    }

    fun onEvent(event: VehiclesEvent) {
        when (event) {
            VehiclesEvent.OpenAddForm -> openAddForm()
            is VehiclesEvent.OpenEditForm -> openEditForm(event.vehicle)
            VehiclesEvent.CloseForm -> closeForm()
            is VehiclesEvent.TypeChanged -> updateForm { copy(type = event.value) }
            is VehiclesEvent.PlateChanged -> updateForm { copy(plateNumber = event.value) }
            is VehiclesEvent.CapacityChanged -> updateForm { copy(capacityKg = event.value) }
            is VehiclesEvent.VolumeChanged -> updateForm { copy(volumeKg = event.value) }
            is VehiclesEvent.MakeChanged -> updateForm { copy(make = event.value) }
            is VehiclesEvent.ModelChanged -> updateForm { copy(model = event.value) }
            is VehiclesEvent.AvailabilityChanged -> updateForm { copy(isAvailable = event.value) }
            VehiclesEvent.SaveVehicle -> saveVehicle()
            is VehiclesEvent.PromptDelete -> _uiState.value = _uiState.value.copy(deleteCandidate = event.vehicle)
            VehiclesEvent.DismissDelete -> _uiState.value = _uiState.value.copy(deleteCandidate = null)
            VehiclesEvent.ConfirmDelete -> confirmDelete()
            is VehiclesEvent.ToggleAvailability -> toggleAvailability(event.vehicle, event.isAvailable)
        }
    }

    private fun observeUserAndLoad() {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _uiState.value = VehiclesUiState(
                        isLoading = false,
                        errorMessage = "Could not load provider account."
                    )
                } else if (user.role != UserRole.PROVIDER) {
                    _uiState.value = VehiclesUiState(
                        isLoading = false,
                        errorMessage = "Current user is not a provider account."
                    )
                } else {
                    loadVehicles(user.id)
                }
            }
        }
    }

    private fun loadVehicles(providerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = logisticsRepository.getVehiclesForProvider(providerId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                vehicles = result.getOrDefault(emptyList()),
                errorMessage = if (result.isFailure) "Unable to load vehicles." else null
            )
        }
    }

    private fun openAddForm() {
        _uiState.value = _uiState.value.copy(
            isFormVisible = true,
            form = VehicleFormState(),
            errorMessage = null
        )
    }

    private fun openEditForm(vehicle: Vehicle) {
        _uiState.value = _uiState.value.copy(
            isFormVisible = true,
            form = VehicleFormState(
                id = vehicle.id,
                type = vehicle.type,
                plateNumber = vehicle.plateNumber,
                capacityKg = vehicle.maxCapacityKg.takeIf { it > 0.0 }?.toString() ?: "",
                volumeKg = vehicle.maxVolumeKg.takeIf { it > 0.0 }?.toString() ?: "",
                make = vehicle.make,
                model = vehicle.model,
                isAvailable = vehicle.isAvailable
            ),
            errorMessage = null
        )
    }

    private fun closeForm() {
        if (_uiState.value.isSaving) return
        _uiState.value = _uiState.value.copy(isFormVisible = false)
    }

    private fun updateForm(update: VehicleFormState.() -> VehicleFormState) {
        _uiState.value = _uiState.value.copy(form = _uiState.value.form.update(), errorMessage = null)
    }

    private fun saveVehicle() {
        val user = currentUser.value
        if (user == null || user.role != UserRole.PROVIDER) {
            _uiState.value = _uiState.value.copy(errorMessage = "Provider account is required.")
            return
        }

        val form = _uiState.value.form
        val type = form.type.trim()
        val plate = form.plateNumber.trim()
        val capacity = form.capacityKg.trim().toDoubleOrNull()
        val volume = form.volumeKg.trim().toDoubleOrNull() ?: 0.0

        if (type.isBlank() || plate.isBlank() || capacity == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Type, plate number, and capacity are required.")
            return
        }

        val vehicle = Vehicle(
            id = form.id ?: "",
            providerId = user.id,
            type = type,
            make = form.make.trim(),
            model = form.model.trim(),
            plateNumber = plate,
            maxCapacityKg = capacity,
            maxVolumeKg = volume,
            currentLat = 0.0,
            currentLng = 0.0,
            isAvailable = form.isAvailable
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val result = if (form.id == null) {
                logisticsRepository.createVehicle(vehicle)
            } else {
                logisticsRepository.updateVehicle(vehicle)
            }

            if (result.isSuccess) {
                val saved = result.getOrNull() ?: vehicle
                val updatedList = if (form.id == null) {
                    _uiState.value.vehicles + saved
                } else {
                    _uiState.value.vehicles.map { if (it.id == saved.id) saved else it }
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isFormVisible = false,
                    vehicles = updatedList
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Unable to save vehicle."
                )
            }
        }
    }

    private fun confirmDelete() {
        val vehicle = _uiState.value.deleteCandidate ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val result = logisticsRepository.deleteVehicle(vehicle.id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    deleteCandidate = null,
                    vehicles = _uiState.value.vehicles.filterNot { it.id == vehicle.id }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Unable to delete vehicle.",
                    deleteCandidate = null
                )
            }
        }
    }

    private fun toggleAvailability(vehicle: Vehicle, isAvailable: Boolean) {
        val updated = vehicle.copy(isAvailable = isAvailable)
        viewModelScope.launch {
            val result = logisticsRepository.updateVehicle(updated)
            if (result.isSuccess) {
                val saved = result.getOrNull() ?: updated
                _uiState.value = _uiState.value.copy(
                    vehicles = _uiState.value.vehicles.map { if (it.id == saved.id) saved else it }
                )
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Unable to update availability.")
            }
        }
    }
}

