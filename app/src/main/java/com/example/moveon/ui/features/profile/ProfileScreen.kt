package com.example.moveon.ui.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.ProviderBottomBar
import com.example.moveon.ui.components.ProviderDashboardTab
import com.example.moveon.ui.components.MoveOnProfileActionRowItem
import com.example.moveon.ui.components.MoveOnProfileHeaderCard
import com.example.moveon.ui.components.MoveOnStatCard
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Success

@Composable
fun ProfileScreen(
    onTabSelected: (DashboardTab) -> Unit,
    onNavigateToLogin: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSecurity: () -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.profileState.value

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateToLogin -> onNavigateToLogin()
            }
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Profile Header Card with Gradient
            MoveOnProfileHeaderCard(
                name = state.displayName,
                email = state.email.ifBlank { "No email available" },
                photoUrl = state.profilePhotoUrl,
                initials = state.initials,
                memberSinceDate = "Jan 2024"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MoveOnStatCard(
                    value = state.totalMoves.toString(),
                    label = "Moves",
                    modifier = Modifier.weight(1f)
                )
                MoveOnStatCard(
                    value = state.totalBoxes.toString(),
                    label = "Boxes",
                    modifier = Modifier.weight(1f)
                )
                MoveOnStatCard(
                    value = if (state.averageRating <= 0f) "--" else String.format("%.1f", state.averageRating),
                    label = "Rating",
                    modifier = Modifier.weight(1f),
                    valueColor = Success
                )
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Section Header
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LightTextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Account Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    MoveOnProfileActionRowItem(
                        title = "Edit Profile",
                        leadingIcon = Icons.Outlined.Edit,
                        onClick = {},
                        showDivider = true
                    )
                    MoveOnProfileActionRowItem(
                        title = "Saved Addresses",
                        leadingIcon = Icons.Outlined.LocationOn,
                        onClick = {},
                        showDivider = true
                    )
                    MoveOnProfileActionRowItem(
                        title = "Move History",
                        leadingIcon = Icons.Outlined.History,
                        onClick = {},
                        showDivider = true
                    )
                    MoveOnProfileActionRowItem(
                        title = "Payment Methods",
                        leadingIcon = Icons.Outlined.Payment,
                        onClick = {},
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences Section Header
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LightTextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preferences Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    MoveOnProfileActionRowItem(
                        title = "App Settings",
                        leadingIcon = Icons.Outlined.Settings,
                        onClick = onOpenSettings,
                        showDivider = true
                    )
                    MoveOnProfileActionRowItem(
                        title = "Privacy & Security",
                        leadingIcon = Icons.Outlined.Lock,
                        onClick = onOpenSecurity,
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button (Red Outlined)
            OutlinedButton(
                onClick = { if (!state.isLoading) viewModel.onEvent(ProfileEvent.Logout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = BorderStroke(2.dp, ErrorDeep),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = ErrorDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = if (state.isLoading) "Loading..." else "Logout",
                        color = ErrorDeep,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
