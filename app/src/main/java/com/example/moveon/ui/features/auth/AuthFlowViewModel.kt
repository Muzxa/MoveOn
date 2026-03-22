package com.example.moveon.ui.features.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.moveon.domain.model.UserRole

class AuthFlowViewModel : ViewModel() {
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var selectedRole by mutableStateOf<UserRole?>(null)

    var businessName by mutableStateOf("")
    var businessAddress by mutableStateOf("")
    var city by mutableStateOf("")
    var businessDescription by mutableStateOf("")
    var yearsOfExperience by mutableStateOf("")

    var vehicleMake by mutableStateOf("")
    var vehicleModel by mutableStateOf("")
    var vehicleYear by mutableStateOf("")
    var vehicleColor by mutableStateOf("")
    var plateNumber by mutableStateOf("")
    var maxCapacityKg by mutableStateOf("")
    var maxVolumeM3 by mutableStateOf("")
    var baseRate by mutableStateOf("")
    var ratePerKm by mutableStateOf("")

    var cnicUploaded by mutableStateOf(false)

    fun splitFirstAndLastName(): Pair<String, String> {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) return "" to ""
        val firstName = parts.first()
        val lastName = parts.drop(1).joinToString(" ")
        return firstName to lastName
    }

    fun reset() {
        fullName = ""
        email = ""
        phoneNumber = ""
        password = ""
        confirmPassword = ""
        selectedRole = null

        businessName = ""
        businessAddress = ""
        city = ""
        businessDescription = ""
        yearsOfExperience = ""

        vehicleMake = ""
        vehicleModel = ""
        vehicleYear = ""
        vehicleColor = ""
        plateNumber = ""
        maxCapacityKg = ""
        maxVolumeM3 = ""
        baseRate = ""
        ratePerKm = ""

        cnicUploaded = false
    }
}

