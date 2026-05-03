package com.example.moveon.ui.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.ProviderBottomBar
import com.example.moveon.ui.components.ProviderDashboardTab
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import android.widget.Toast

@Composable
fun VerifyIdentityScreen(
    onBack: () -> Unit,
    onSelectMethod: (String) -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    prefilledEmail: String? = null,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser.collectAsState().value
    val emailLabel = maskEmail(prefilledEmail ?: user?.email.orEmpty())
    val phoneLabel = maskPhone(user?.phoneNumber.orEmpty())

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            if (isProviderMode) {
                ProviderBottomBar(
                    selectedTab = ProviderDashboardTab.Profile,
                    onTabSelected = onProviderTabSelected
                )
            } else {
                MoveOnBottomBar(
                    selectedTab = DashboardTab.Profile,
                    onTabSelected = onTabSelected
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopHeader(
                title = "Verify Identity",
                subtitle = "Choose how to receive your OTP",
                onBack = onBack
            )

            VerifyIdentityCard(
                title = "Email",
                subtitle = emailLabel,
                icon = Icons.Outlined.Email,
                iconBackground = Primary.copy(alpha = 0.10f),
                iconTint = Primary,
                onClick = { onSelectMethod("email") }
            )

            VerifyIdentityCard(
                title = "SMS",
                subtitle = phoneLabel,
                icon = Icons.Outlined.LocationOn,
                iconBackground = Color(0x1AFF6F00),
                iconTint = Color(0xFFFF6F00),
                onClick = { onSelectMethod("sms") }
            )
        }
    }
}

@Composable
private fun VerifyIdentityCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = LightTextSecondary)
            }

            Icon(Icons.Outlined.ChevronRight, contentDescription = "Select $title", tint = LightTextSecondary)
        }
    }
}

@Composable
fun NewPasswordScreen(
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    forgotPasswordEmail: String? = null,
    viewModel: PasswordUpdateViewModel = hiltViewModel()
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }

    val canSubmit = newPassword.length >= 8 && newPassword == confirmPassword
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            if (isProviderMode) {
                ProviderBottomBar(
                    selectedTab = ProviderDashboardTab.Profile,
                    onTabSelected = onProviderTabSelected
                )
            } else {
                MoveOnBottomBar(
                    selectedTab = DashboardTab.Profile,
                    onTabSelected = onTabSelected
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopHeader(
                title = "New Password",
                subtitle = "Create a strong password",
                onBack = onBack
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.20f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = LightTextPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Password must be at least 8 characters long",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextPrimary
                    )
                }
            }

            PasswordField(
                label = "New Password",
                value = newPassword,
                placeholder = "Enter new password",
                isVisible = showNewPassword,
                onToggleVisible = { showNewPassword = !showNewPassword },
                onValueChange = { newPassword = it }
            )

            PasswordField(
                label = "Confirm Password",
                value = confirmPassword,
                placeholder = "Re-enter new password",
                isVisible = showConfirmPassword,
                onToggleVisible = { showConfirmPassword = !showConfirmPassword },
                onValueChange = { confirmPassword = it }
            )

            if (viewModel.requiresReauth.value && forgotPasswordEmail.isNullOrBlank()) {
                PasswordField(
                    label = "Current Password",
                    value = currentPassword,
                    placeholder = "Enter current password",
                    isVisible = showCurrentPassword,
                    onToggleVisible = { showCurrentPassword = !showCurrentPassword },
                    onValueChange = { currentPassword = it }
                )
            }

            Button(
                onClick = {
                    val email = forgotPasswordEmail?.trim().orEmpty()
                    if (email.isNotBlank()) {
                        viewModel.sendPasswordResetEmail(email) { onPasswordChanged() }
                    } else {
                        if (viewModel.requiresReauth.value) {
                            viewModel.reauthenticateAndUpdatePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword
                            ) { onPasswordChanged() }
                        } else {
                            viewModel.updatePassword(newPassword) { onPasswordChanged() }
                        }
                    }
                },
                enabled = canSubmit && (!viewModel.requiresReauth.value || currentPassword.isNotBlank() || !forgotPasswordEmail.isNullOrBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Change Password", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisible: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedTextColor = LightTextPrimary,
                unfocusedTextColor = LightTextPrimary,
                disabledTextColor = LightTextPrimary.copy(alpha = 0.6f),
                focusedPlaceholderColor = LightTextSecondary,
                unfocusedPlaceholderColor = LightTextSecondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                IconButton(onClick = onToggleVisible) {
                    Icon(
                        imageVector = if (isVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (isVisible) "Hide password" else "Show password",
                        tint = LightTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
    }
}
