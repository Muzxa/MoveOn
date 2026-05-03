package com.example.moveon.ui.features.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.moveon.ui.theme.ErrorDeep

@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onOpenSecurity: () -> Unit = {},
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
            if (isProviderMode) ProviderBottomBar(selectedTab = ProviderDashboardTab.Profile, onTabSelected = onProviderTabSelected)
            else MoveOnBottomBar(selectedTab = DashboardTab.Profile, onTabSelected = onTabSelected)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = LightTextPrimary
                    )
                    Text(
                        text = "Manage your app preferences",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextSecondary
                    )
                }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close app settings",
                        tint = Primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            AppSettingSectionTitle("Notifications", Icons.Outlined.NotificationsNone)
            AppSettingCard {
                AppSettingToggleRow("Push Notifications", "Receive notifications about your moves", Icons.Outlined.NotificationsNone, state.pushNotificationsEnabled, viewModel::setPushNotificationsEnabled)
                AppSettingDivider()
                AppSettingToggleRow("Email Notifications", "Get updates via email", Icons.Outlined.Email, state.emailNotificationsEnabled, viewModel::setEmailNotificationsEnabled)
            }

            AppSettingSectionTitle("Privacy & Security", Icons.Outlined.Security)
            AppSettingCard {
                AppSettingToggleRow("Share Live Location", "Allow drivers to see your location", Icons.Outlined.LocationOn, state.shareLiveLocationEnabled, viewModel::setShareLiveLocationEnabled)
            }

            AppSettingSectionTitle("App Preferences", Icons.Outlined.DarkMode)
            AppSettingCard {
                AppSettingToggleRow("Dark Mode", "Switch to dark theme", Icons.Outlined.DarkMode, state.darkModeEnabled, viewModel::setDarkModeEnabled)
                AppSettingDivider()
                AppSettingToggleRow("Auto Sync", "Sync data when online", Icons.Outlined.Sync, state.autoSyncEnabled, viewModel::setAutoSyncEnabled)
            }

            AppSettingSectionTitle("More Settings", Icons.Outlined.Security)
            AppSettingCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenSecurity)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    AppSettingRowContent("Security", "Password & biometrics", Icons.Outlined.Security, showChevron = true)
                }
            }

            if (!state.autoSyncEnabled) {
                AppSettingSectionTitle("Data Management", Icons.Outlined.Download)
                AppSettingCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Offline Data", style = MaterialTheme.typography.titleMedium, color = LightTextPrimary)
                        Text("${state.offlineBoxesCount} boxes cached", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = viewModel::syncNow,
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                                border = BorderStroke(1.dp, LightBorder)
                            ) {
                                Icon(Icons.Outlined.Sync, contentDescription = null, tint = LightTextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", color = LightTextPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = viewModel::clearCache,
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightSurface),
                                border = BorderStroke(1.dp, LightBorder)
                            ) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = ErrorDeep, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Cache", color = ErrorDeep)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSettingSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(16.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = LightTextSecondary)
    }
}

@Composable
private fun AppSettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) { content() }
}

@Composable
private fun AppSettingDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LightBorder))
}

@Composable
private fun AppSettingToggleRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(LightSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AppSettingRowContent(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, showChevron: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(LightSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }
        if (showChevron) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Open $title", tint = LightTextSecondary)
        }
    }
}
