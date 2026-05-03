package com.example.moveon.ui.features.provider

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderDriversViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(ProviderDriversUiState())
    val uiState: State<ProviderDriversUiState> = _uiState

    init {
        onEvent(DriversEvent.LoadData)
    }

    fun onEvent(event: DriversEvent) {
        when (event) {
            is DriversEvent.LoadData -> loadData()
            
            // Add Form
            is DriversEvent.OpenAddForm -> {
                _uiState.value = _uiState.value.copy(
                    isAddFormVisible = true,
                    form = DriverFormState()
                )
            }
            is DriversEvent.CloseAddForm -> {
                _uiState.value = _uiState.value.copy(isAddFormVisible = false)
            }
            is DriversEvent.NameChanged -> {
                _uiState.value = _uiState.value.copy(
                    form = _uiState.value.form.copy(name = event.name)
                )
            }
            is DriversEvent.PhoneChanged -> {
                _uiState.value = _uiState.value.copy(
                    form = _uiState.value.form.copy(phone = event.phone)
                )
            }
            is DriversEvent.CnicChanged -> {
                _uiState.value = _uiState.value.copy(
                    form = _uiState.value.form.copy(cnic = event.cnic)
                )
            }
            is DriversEvent.LicenseChanged -> {
                _uiState.value = _uiState.value.copy(
                    form = _uiState.value.form.copy(licenseNo = event.license)
                )
            }
            is DriversEvent.SaveDriver -> saveDriver()

            // Assign Form
            is DriversEvent.OpenAssignForm -> {
                _uiState.value = _uiState.value.copy(
                    isAssignFormVisible = true,
                    assignFormState = AssignFormState(driver = event.driver, selectedVehicleId = event.driver.vehicleId.takeIf { it.isNotBlank() })
                )
            }
            is DriversEvent.CloseAssignForm -> {
                _uiState.value = _uiState.value.copy(isAssignFormVisible = false)
            }
            is DriversEvent.SelectVehicle -> {
                _uiState.value = _uiState.value.copy(
                    assignFormState = _uiState.value.assignFormState.copy(selectedVehicleId = event.vehicleId)
                )
            }
            is DriversEvent.ConfirmAssignment -> confirmAssignment()

            // Delete
            is DriversEvent.PromptDelete -> {
                _uiState.value = _uiState.value.copy(deleteCandidate = event.driver)
            }
            is DriversEvent.ConfirmDelete -> deleteDriver()
            is DriversEvent.DismissDelete -> {
                _uiState.value = _uiState.value.copy(deleteCandidate = null)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = authRepository.currentUser.firstOrNull()
            if (user == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "User not logged in")
                return@launch
            }

            val driversResult = logisticsRepository.getDriversForProvider(user.id)
            val vehiclesResult = logisticsRepository.getVehiclesForProvider(user.id)

            if (driversResult.isSuccess && vehiclesResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    drivers = driversResult.getOrDefault(emptyList()),
                    vehicles = vehiclesResult.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load drivers data."
                )
            }
        }
    }

    private fun saveDriver() {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull() ?: return@launch
            val form = _uiState.value.form

            if (form.name.isBlank() || form.phone.isBlank() || form.cnic.isBlank() || form.licenseNo.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Please fill all fields.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            val newDriver = Driver(
                id = "",
                providerId = user.id,
                vehicleId = "",
                licenseNo = form.licenseNo,
                name = form.name,
                phone = form.phone,
                cnic = form.cnic,
                status = "Available",
                rating = 5.0,
                tripsCount = 0,
                joinedDateMillis = System.currentTimeMillis()
            )

            val result = logisticsRepository.createDriver(newDriver)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isAddFormVisible = false)
                loadData()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to add driver."
                )
            }
        }
    }

    private fun confirmAssignment() {
        viewModelScope.launch {
            val assignForm = _uiState.value.assignFormState
            val driver = assignForm.driver ?: return@launch
            val selectedVehicleId = assignForm.selectedVehicleId ?: ""

            _uiState.value = _uiState.value.copy(isLoading = true, isAssignFormVisible = false)

            // If a vehicle is assigned to this driver, update driver
            val updatedDriver = driver.copy(vehicleId = selectedVehicleId)
            val result = logisticsRepository.updateDriver(updatedDriver)

            if (result.isSuccess) {
                // If the selected vehicle was assigned to another driver, we must clear it from them
                val allDrivers = _uiState.value.drivers
                val otherDriversWithSameVehicle = allDrivers.filter { 
                    it.id != updatedDriver.id && it.vehicleId == selectedVehicleId && selectedVehicleId.isNotBlank() 
                }

                otherDriversWithSameVehicle.forEach { oldDriver ->
                    logisticsRepository.updateDriver(oldDriver.copy(vehicleId = ""))
                }

                loadData()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to assign vehicle."
                )
            }
        }
    }

    private fun deleteDriver() {
        viewModelScope.launch {
            val candidate = _uiState.value.deleteCandidate ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, deleteCandidate = null)

            val result = logisticsRepository.deleteDriver(candidate.id)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to delete driver."
                )
            }
        }
    }
}
