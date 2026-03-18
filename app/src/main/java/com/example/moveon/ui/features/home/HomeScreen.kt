package com.example.moveon.ui.features.home

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
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MoveOnPillButton

@Composable
fun HomeScreen(
    onTabSelected: (DashboardTab) -> Unit = {},
    onTrackDriverClick: () -> Unit = {},
    onManageInventoryClick: () -> Unit = {}
) {
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
                Text(
                    text = "MoveOn",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1C1B1F)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x1A1565C0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AK",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1565C0)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

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
                                text = "Move #MV-2024-0142",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Shahzore · LEA 3394",
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
                                    text = "45 mins",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1C1B1F)
                                )
                            }
                        }

                        AddressTimeline(
                            pickup = "123 Main Street, Karachi",
                            dropOff = "456 Park Avenue, Lahore"
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MoveOnPillButton(
                                text = "Track Driver",
                                onClick = onTrackDriverClick,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
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
                color = Color(0xFF1C1B1F)
            )
        }
    }
}
