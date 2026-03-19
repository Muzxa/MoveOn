package com.example.moveon.ui.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.components.MoveOnProfileActionRow
import com.example.moveon.ui.components.MoveOnProfileHeaderCard
import com.example.moveon.ui.components.MoveOnStatCard

@Composable
fun ProfileScreen(
    onTabSelected: (DashboardTab) -> Unit,
    onNavigateToLogin: () -> Unit,
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
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            MoveOnBottomBar(
                selectedTab = DashboardTab.Profile,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1C1B1F)
            )
            Text(
                text = "Manage your account and moving activity",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(14.dp))

            MoveOnProfileHeaderCard(
                name = state.displayName,
                email = state.email.ifBlank { "No email available" },
                photoUrl = state.profilePhotoUrl,
                initials = state.initials
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    valueColor = Color(0xFF2E7D32)
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

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoveOnProfileActionRow(
                title = "Account Settings",
                subtitle = "Update password and account details",
                leadingIcon = Icons.Outlined.Settings,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoveOnProfileActionRow(
                title = "Notifications",
                subtitle = "Manage push and email notifications",
                leadingIcon = Icons.Outlined.NotificationsNone,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoveOnProfileActionRow(
                title = "Payment Methods",
                subtitle = "Add and manage your cards",
                leadingIcon = Icons.Outlined.CreditCard,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoveOnProfileActionRow(
                title = "Help & Support",
                subtitle = "Get help or contact support",
                leadingIcon = Icons.AutoMirrored.Outlined.HelpOutline,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            MoveOnPillButton(
                text = if (state.isLoading) "Loading..." else "Logout",
                onClick = { if (!state.isLoading) viewModel.onEvent(ProfileEvent.Logout) },
                modifier = Modifier.fillMaxWidth(),
                background = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
