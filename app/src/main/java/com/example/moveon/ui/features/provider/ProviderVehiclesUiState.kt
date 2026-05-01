package com.example.moveon.ui.features.provider

import com.example.moveon.domain.model.Vehicle

data class VehicleFormState(
    val id: String? = null,
    val type: String = "",
    val plateNumber: String = "",
    val capacityKg: String = "",
    val volumeKg: String = "",
    val make: String = "",
    val model: String = "",
    val isAvailable: Boolean = true
)

data class VehiclesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val vehicles: List<Vehicle> = emptyList(),
    val errorMessage: String? = null,
    val isFormVisible: Boolean = false,
    val form: VehicleFormState = VehicleFormState(),
    val deleteCandidate: Vehicle? = null
)

sealed class VehiclesEvent {
    data object OpenAddForm : VehiclesEvent()
    data class OpenEditForm(val vehicle: Vehicle) : VehiclesEvent()
    data object CloseForm : VehiclesEvent()
    data class TypeChanged(val value: String) : VehiclesEvent()
    data class PlateChanged(val value: String) : VehiclesEvent()
    data class CapacityChanged(val value: String) : VehiclesEvent()
    data class VolumeChanged(val value: String) : VehiclesEvent()
    data class MakeChanged(val value: String) : VehiclesEvent()
    data class ModelChanged(val value: String) : VehiclesEvent()
    data class AvailabilityChanged(val value: Boolean) : VehiclesEvent()
    data object SaveVehicle : VehiclesEvent()
    data class PromptDelete(val vehicle: Vehicle) : VehiclesEvent()
    data object DismissDelete : VehiclesEvent()
    data object ConfirmDelete : VehiclesEvent()
    data class ToggleAvailability(val vehicle: Vehicle, val isAvailable: Boolean) : VehiclesEvent()
}

