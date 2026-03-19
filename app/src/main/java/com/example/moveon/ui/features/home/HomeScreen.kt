package com.example.moveon.ui.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun HomeScreen(
    onTabSelected: (DashboardTab) -> Unit = {},
    onManageInventoryClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.homeState.value

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
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
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = state.profileName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF1C1B1F)
                    )
                }

                ProfileAvatar(
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
                color = Color(0xFF1C1B1F)
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "Scan Box",
                    icon = Icons.Outlined.QrCode2,
                    tint = Color(0xFFFF6F00),
                    bg = Color(0x1AFF6F00),
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "New Move",
                    icon = Icons.Outlined.LocalShipping,
                    tint = Color(0xFF1565C0),
                    bg = Color(0x1A1565C0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "My Moves",
                    icon = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
                    tint = Color(0xFF1565C0),
                    bg = Color(0x1A1565C0),
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Room Scan",
                    icon = Icons.Outlined.AutoAwesome,
                    tint = Color(0xFF2E7D32),
                    bg = Color(0x1A2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    initials: String
) {
    val shape = CircleShape
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(48.dp)
                .clip(shape)
                .border(1.dp, Color(0x331565C0), shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0x1A1565C0), shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1565C0)
            )
        }
    }
}

@Composable
private fun LoadingMoveCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x331565C0))
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
                color = Color(0xFF1565C0)
            )
            Text(
                text = "Loading your active move...",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1C1B1F)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x33EF4444))
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
                color = Color(0xFF1C1B1F)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x331565C0))
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
                color = Color(0xFF1C1B1F)
            )
            Text(
                text = "You currently have nothing to move. Start by organizing your inventory.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
            MoveOnPillButton(
                text = "Manage Inventory",
                onClick = onManageInventoryClick,
                modifier = Modifier.fillMaxWidth(),
                background = Color(0xFF1565C0)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x331565C0), RoundedCornerShape(16.dp))
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF1976D2))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Move #${move.moveId}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Text(
                    text = move.providerLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
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
                            .background(Color(0xFFE8F4FD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Estimated Arrival",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = move.etaLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1C1B1F)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color(0x141565C0), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = move.statusLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1565C0)
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
                        .background(Color(0xFF1565C0), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(Color(0xFFE0E0E0))
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Pickup", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                Text(pickup, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1C1B1F))
            }
        }
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFF1565C0), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Drop-off", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                Text(dropOff, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1C1B1F))
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
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
                color = Color(0xFF1C1B1F),
                textAlign = TextAlign.Center
            )
        }
    }
}
