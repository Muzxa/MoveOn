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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MoveOnOutlinedPillButton
import com.example.moveon.ui.components.ProviderBottomBar
import com.example.moveon.ui.components.ProviderDashboardTab
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Success
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenAppSettings: () -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.value
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
                title = "Settings",
                subtitle = "Manage your app preferences",
                onBack = onBack
            )

            SectionTitle(title = "Notifications", icon = Icons.Outlined.NotificationsNone)
            SectionCard {
                SettingToggleRow(
                    title = "Push Notifications",
                    subtitle = "Receive notifications about your moves",
                    icon = Icons.Outlined.NotificationsNone,
                    checked = state.pushNotificationsEnabled,
                    onCheckedChange = viewModel::setPushNotificationsEnabled
                )
                SectionDivider()
                SettingToggleRow(
                    title = "Email Notifications",
                    subtitle = "Get updates via email",
                    icon = Icons.Outlined.Email,
                    checked = state.emailNotificationsEnabled,
                    onCheckedChange = viewModel::setEmailNotificationsEnabled
                )
            }

            SectionTitle(title = "Privacy & Security", icon = Icons.Outlined.Lock)
            SectionCard {
                SettingToggleRow(
                    title = "Share Live Location",
                    subtitle = "Allow drivers to see your location",
                    icon = Icons.Outlined.LocationOn,
                    checked = state.shareLiveLocationEnabled,
                    onCheckedChange = viewModel::setShareLiveLocationEnabled
                )
            }

            SectionTitle(title = "App Preferences", icon = Icons.Outlined.DarkMode)
            SectionCard {
                SettingToggleRow(
                    title = "Dark Mode",
                    subtitle = "Switch to dark theme",
                    icon = Icons.Outlined.DarkMode,
                    checked = state.darkModeEnabled,
                    onCheckedChange = viewModel::setDarkModeEnabled
                )
                SectionDivider()
                SettingToggleRow(
                    title = "Auto Sync",
                    subtitle = "Sync data when online",
                    icon = Icons.Outlined.Sync,
                    checked = state.autoSyncEnabled,
                    onCheckedChange = viewModel::setAutoSyncEnabled
                )
                SectionDivider()
                // App Settings action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onOpenAppSettings
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAppSettings() }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(LightSurfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Settings",
                                style = MaterialTheme.typography.titleMedium,
                                color = LightTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Detailed app preferences",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = LightTextSecondary
                        )
                    }
                }
            }

            SectionTitle(title = "More Settings", icon = Icons.Outlined.Security)
            SecurityActionCard(onClick = onOpenSecurity)

            if (!state.autoSyncEnabled) {
                SectionTitle(title = "Data Management", icon = Icons.Outlined.Sync)
                SectionCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Offline Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LightTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${state.offlineBoxesCount} boxes cached",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "${state.offlineItemsCount} items stored",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                state.lastSyncLabel?.let {
                                    Text(
                                        text = "Last synced $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightTextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(LightSurfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = state.cacheSizeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LightTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = viewModel::syncNow,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Sync Now", color = Color.White)
                            }

                            OutlinedButton(
                                onClick = viewModel::clearCache,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, LightBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = LightTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Clear Cache", color = LightTextPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onOpenOtp: (String) -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    viewModel: SecurityViewModel = hiltViewModel()
) {
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
                title = "Security",
                subtitle = "Manage your account security",
                onBack = onBack
            )

            SectionTitle(title = "Security Options", icon = Icons.Outlined.Security)
            SectionCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Primary.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Password",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LightTextPrimary
                    )

                    Text(
                        text = "Change",
                        modifier = Modifier
                            .clickable { onOpenOtp("password") }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityOtpScreen(
    verificationMethod: String,
    onBack: () -> Unit,
    onVerify: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser.collectAsState().value
    val destinationLabel = if (verificationMethod == "sms") {
        maskPhone(user?.phoneNumber.orEmpty())
    } else {
        maskEmail(user?.email.orEmpty())
    }
    var code by rememberSaveable { mutableStateOf("") }

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
                title = "Enter Code",
                subtitle = "We sent a code to $destinationLabel",
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(88.dp))

            OtpCodeInput(
                code = code,
                onCodeChange = { code = it }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Didn't receive the code?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary
                )
                TextButton(onClick = { }) {
                    Text(text = "Resend Code", color = Primary)
                }
            }

            Button(
                onClick = onVerify,
                enabled = code.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(text = "Verify", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PasswordUpdatedScreen(
    onVerifyAgain: () -> Unit,
    onGoToSettings: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {}
) {
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(160.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Success.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Password Updated",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LightTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your password has been successfully changed. You can now use your new password to log in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onVerifyAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(text = "Back to Home", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                MoveOnOutlinedPillButton(
                    text = "Go to Settings",
                    onClick = onGoToSettings,
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = LightBorder,
                    background = LightSurface,
                    textColor = LightTextPrimary
                )
            }
        }
    }
}

@Composable
fun TopHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = LightTextPrimary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LightTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = LightTextSecondary
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LightBorder)
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(LightSurfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SecurityOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(LightSurfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = LightTextSecondary
        )
    }
}

@Composable
private fun SecurityActionCard(onClick: () -> Unit) {
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LightSurfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Password & biometrics",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = LightTextSecondary
            )
        }
    }
}

@Composable
private fun OtpCodeInput(
    code: String,
    onCodeChange: (String) -> Unit
) {
    BasicTextField(
        value = code,
        onValueChange = { newValue ->
            val filtered = newValue.filter(Char::isDigit).take(6)
            onCodeChange(filtered)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(6) { index ->
                    val digit = code.getOrNull(index)?.toString().orEmpty()
                    val isActive = index == code.length && code.length < 6
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                color = if (isActive) LightSurfaceVariant else LightSurface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(Color.Transparent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Box(modifier = Modifier.alpha(0f)) {
                it()
            }
        }
    )
}

fun maskEmail(email: String): String {
    if (email.isBlank() || !email.contains("@")) return "a***@email.com"
    val parts = email.split("@")
    val local = parts.first()
    val domain = parts.getOrNull(1).orEmpty()
    val prefix = local.firstOrNull()?.toString().orEmpty()
    return "$prefix***@$domain"
}

fun maskPhone(phone: String): String {
    val digits = phone.filter(Char::isDigit)
    if (digits.length < 4) return "+92 *** ****"
    return "+${digits.take(3)} ${digits.drop(3).take(3)} *** ${digits.takeLast(4)}"
}