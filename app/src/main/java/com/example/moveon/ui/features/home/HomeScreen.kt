package com.example.moveon.ui.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.components.MoveOnProfileAvatar
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.BlueTint
import com.example.moveon.ui.theme.Error
import com.example.moveon.ui.theme.GreenTint
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Success

@Composable
fun HomeScreen(
    onTabSelected: (DashboardTab) -> Unit = {},
    onManageInventoryClick: () -> Unit = {},
    onScanBoxClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.homeState.value

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            MoveOnBottomBar(
                selectedTab = DashboardTab.Home,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                    Text(
                        text = state.profileName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = LightTextPrimary
                    )
                }

                MoveOnProfileAvatar(
                    photoUrl = state.profilePhotoUrl,
                    initials = state.profileInitials
                )
            }

            Spacer(Modifier.height(14.dp))

            when {
                state.isLoading -> {
                    LoadingMoveCard()
                }

                state.errorMessage != null -> {
                    ErrorMoveCard(
                        message = state.errorMessage,
                        onRetry = viewModel::refreshDashboard
                    )
                }

                state.activeMove != null -> {
                    ActiveMoveCard(
                        move = state.activeMove,
                        onManageInventoryClick = onManageInventoryClick
                    )
                }

                else -> {
                    EmptyMoveCard(onManageInventoryClick = onManageInventoryClick)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineMedium,
                color = LightTextPrimary
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "Scan Box",
                    icon = Icons.Outlined.QrCode2,
                    tint = Accent,
                    bg = BlueTint,
                    onClick = onScanBoxClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "New Move",
                    icon = Icons.Outlined.LocalShipping,
                    tint = Primary,
                    bg = BlueTint,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "My Moves",
                    icon = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
                    tint = Primary,
                    bg = BlueTint,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Room Scan",
                    icon = Icons.Outlined.AutoAwesome,
                    tint = Success,
                    bg = GreenTint,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LoadingMoveCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Primary
            )
            Text(
                text = "Loading your active move...",
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary
            )
        }
    }
}

@Composable
private fun ErrorMoveCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, Error.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Could not load move details",
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
            MoveOnPillButton(text = "Retry", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EmptyMoveCard(
    onManageInventoryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No active move right now",
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary
            )
            Text(
                text = "You currently have nothing to move. Start by organizing your inventory.",
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
            MoveOnPillButton(
                text = "Manage Inventory",
                onClick = onManageInventoryClick,
                modifier = Modifier.fillMaxWidth(),
                background = Primary
            )
        }
    }
}

@Composable
private fun ActiveMoveCard(
    move: ActiveMoveUi,
    onManageInventoryClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Primary, Primary.copy(alpha = 0.7f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Move #${move.moveId}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = move.providerLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Estimated Arrival",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextSecondary
                        )
                        Text(
                            text = move.etaLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightTextPrimary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(BlueTint, RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = move.statusLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary
                        )
                    }
                }

                AddressTimeline(
                    pickup = move.pickupAddress,
                    dropOff = move.dropOffAddress
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MoveOnPillButton(
                        text = "Track Driver",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    MoveOnPillButton(
                        text = "Manage Inventory",
                        onClick = onManageInventoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressTimeline(
    pickup: String,
    dropOff: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Primary, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(LightBorder)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Pickup", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                Text(pickup, style = MaterialTheme.typography.labelLarge, color = LightTextPrimary)
            }
        }
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(LightSurface, CircleShape)
                    .border(1.dp, Primary, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Drop-off", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                Text(dropOff, style = MaterialTheme.typography.labelLarge, color = LightTextPrimary)
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = LightBackground),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = LightTextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
