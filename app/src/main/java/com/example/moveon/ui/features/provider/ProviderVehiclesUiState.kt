package com.example.moveon.ui.features.provider

import com.example.moveon.domain.model.Vehicle

data class VehicleFormState(
    val id: String? = null,
    val type: String = "",
    val isMultiple: Boolean = false,
    val plateNumbers: List<String> = listOf(""),
    val capacityKg: String = "",
    val volumeKg: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val color: String = "",
    val baseRate: String = "",
    val ratePerKm: String = "",
    val isAvailable: Boolean = true
)

data class VehiclesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val vehicles: List<Vehicle> = emptyList(),
    val errorMessage: String? = null,
    val isFormVisible: Boolean = false,
    val form: VehicleFormState = VehicleFormState(),
    val deleteCandidate: Vehicle? = null,
    val selectedTab: String = "All"
)

sealed class VehiclesEvent {
    data object OpenAddForm : VehiclesEvent()
    data class OpenEditForm(val vehicle: Vehicle) : VehiclesEvent()
    data object CloseForm : VehiclesEvent()
    data class TypeChanged(val value: String) : VehiclesEvent()
    data class IsMultipleChanged(val value: Boolean) : VehiclesEvent()
    data class PlateNumberChanged(val index: Int, val value: String) : VehiclesEvent()
    data object AddPlateNumber : VehiclesEvent()
    data class RemovePlateNumber(val index: Int) : VehiclesEvent()
    data class CapacityChanged(val value: String) : VehiclesEvent()
    data class VolumeChanged(val value: String) : VehiclesEvent()
    data class MakeChanged(val value: String) : VehiclesEvent()
    data class ModelChanged(val value: String) : VehiclesEvent()
    data class YearChanged(val value: String) : VehiclesEvent()
    data class ColorChanged(val value: String) : VehiclesEvent()
    data class BaseRateChanged(val value: String) : VehiclesEvent()
    data class RatePerKmChanged(val value: String) : VehiclesEvent()
    data class AvailabilityChanged(val value: Boolean) : VehiclesEvent()
    data class TabChanged(val tab: String) : VehiclesEvent()
    data object SaveVehicle : VehiclesEvent()
    data class PromptDelete(val vehicle: Vehicle) : VehiclesEvent()
    data object DismissDelete : VehiclesEvent()
    data object ConfirmDelete : VehiclesEvent()
    data class ToggleAvailability(val vehicle: Vehicle, val isAvailable: Boolean) : VehiclesEvent()
}

