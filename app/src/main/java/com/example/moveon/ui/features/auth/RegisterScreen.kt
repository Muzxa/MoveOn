package com.example.moveon.ui.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToHome: (UserRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Role selection
    var selectedRole by remember { mutableStateOf(UserRole.USER) }

    // Provider specific
    var establishmentName by remember { mutableStateOf("") }
    var baseRate by remember { mutableStateOf("") }
    var ratePerKm by remember { mutableStateOf("") }

    // Driver specific
    var providerId by remember { mutableStateOf("") }
    var vehicleId by remember { mutableStateOf("") }
    var licenseNo by remember { mutableStateOf("") }

    val authState by viewModel.authState

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AuthViewModel.UiEvent.NavigateToHome -> onNavigateToHome(event.role)
                is AuthViewModel.UiEvent.NavigateToLogin -> onNavigateToLogin()
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Join MoveOn",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Create an account to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Role Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserRole.values().forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text(role.name.lowercase().capitalize()) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Common Fields
        RegisterTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name")
        Spacer(modifier = Modifier.height(12.dp))
        RegisterTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name")
        Spacer(modifier = Modifier.height(12.dp))
        RegisterTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Phone Number", keyboardType = KeyboardType.Phone)
        Spacer(modifier = Modifier.height(12.dp))
        RegisterTextField(value = email, onValueChange = { email = it }, label = "Email Address", keyboardType = KeyboardType.Email)
        Spacer(modifier = Modifier.height(12.dp))
        RegisterTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

        // Conditional Fields
        when (selectedRole) {
            UserRole.PROVIDER -> {
                Spacer(modifier = Modifier.height(12.dp))
                RegisterTextField(value = establishmentName, onValueChange = { establishmentName = it }, label = "Establishment Name")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RegisterTextField(value = baseRate, onValueChange = { baseRate = it }, label = "Base Rate ($)", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    RegisterTextField(value = ratePerKm, onValueChange = { ratePerKm = it }, label = "Rate/Km ($)", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
            }
            UserRole.DRIVER -> {
                Spacer(modifier = Modifier.height(12.dp))
                RegisterTextField(value = providerId, onValueChange = { providerId = it }, label = "Provider ID")
                Spacer(modifier = Modifier.height(12.dp))
                RegisterTextField(value = vehicleId, onValueChange = { vehicleId = it }, label = "Vehicle ID")
                Spacer(modifier = Modifier.height(12.dp))
                RegisterTextField(value = licenseNo, onValueChange = { licenseNo = it }, label = "License Number")
            }
            UserRole.USER -> { /* No extra fields */ }
        }

        // Error Display
        if (authState is AuthViewModel.AuthState.Error) {
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Register Button
        Button(
            onClick = {
                when (selectedRole) {
                    UserRole.USER -> viewModel.onEvent(AuthEvent.RegisterUser(email, password, firstName, lastName, phoneNumber))
                    UserRole.PROVIDER -> viewModel.onEvent(AuthEvent.RegisterProvider(
                        email, password, firstName, lastName, phoneNumber,
                        establishmentName, baseRate.toDoubleOrNull() ?: 0.0, ratePerKm.toDoubleOrNull() ?: 0.0
                    ))
                    UserRole.DRIVER -> viewModel.onEvent(AuthEvent.RegisterDriver(
                        email, password, firstName, lastName, phoneNumber,
                        providerId, vehicleId, licenseNo
                    ))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = authState !is AuthViewModel.AuthState.Loading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("CREATE ACCOUNT", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "Already have an account? Log In",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black.copy(alpha = 0.5f),
            focusedPlaceholderColor = Color.Black.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.6f)
        )
    )
}

fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
