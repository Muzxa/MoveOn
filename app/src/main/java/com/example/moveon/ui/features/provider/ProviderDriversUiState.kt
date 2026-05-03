package com.example.moveon.ui.features.provider

import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Vehicle

data class ProviderDriversUiState(
    val drivers: List<Driver> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val form: DriverFormState = DriverFormState(),
    val isAddFormVisible: Boolean = false,
    val assignFormState: AssignFormState = AssignFormState(),
    val isAssignFormVisible: Boolean = false,
    val deleteCandidate: Driver? = null
)

data class DriverFormState(
    val name: String = "",
    val phone: String = "",
    val cnic: String = "",
    val licenseNo: String = ""
)

data class AssignFormState(
    val driver: Driver? = null,
    val selectedVehicleId: String? = null
)

sealed class DriversEvent {
    object LoadData : DriversEvent()
    
    // Add Driver Form
    object OpenAddForm : DriversEvent()
    object CloseAddForm : DriversEvent()
    data class NameChanged(val name: String) : DriversEvent()
    data class PhoneChanged(val phone: String) : DriversEvent()
    data class CnicChanged(val cnic: String) : DriversEvent()
    data class LicenseChanged(val license: String) : DriversEvent()
    object SaveDriver : DriversEvent()
    
    // Assign Vehicle Form
    data class OpenAssignForm(val driver: Driver) : DriversEvent()
    object CloseAssignForm : DriversEvent()
    data class SelectVehicle(val vehicleId: String) : DriversEvent()
    object ConfirmAssignment : DriversEvent()
    
    // Delete
    data class PromptDelete(val driver: Driver) : DriversEvent()
    object ConfirmDelete : DriversEvent()
    object DismissDelete : DriversEvent()
}
