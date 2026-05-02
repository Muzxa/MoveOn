package com.example.moveon.ui.features.provider

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.ProviderBottomBar
import com.example.moveon.ui.components.ProviderDashboardTab
import com.example.moveon.ui.components.ProviderGlassStatCard
import com.example.moveon.ui.components.ProviderHeaderGradient
import com.example.moveon.ui.components.ProviderKpiCard
import com.example.moveon.ui.components.ProviderMetricCard
import com.example.moveon.ui.components.ProviderQuickActionCard
import com.example.moveon.ui.components.ProviderRoutePoint
import com.example.moveon.ui.components.ProviderSectionHeader
import com.example.moveon.ui.components.ProviderTag
import com.example.moveon.ui.components.ProviderToBeImplemented
import com.example.moveon.ui.components.ProviderTrailingChevron
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Secondary
import com.example.moveon.ui.theme.Success
import java.text.DecimalFormat

@Composable
fun ProviderDashboardScreen(
    onOpenProfile: () -> Unit,
    viewModel: ProviderDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var selectedTab by remember { mutableStateOf(ProviderDashboardTab.Dashboard) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == ProviderDashboardTab.Profile) {
            onOpenProfile()
            selectedTab = ProviderDashboardTab.Dashboard
        }
    }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            ProviderBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            ProviderDashboardTab.Dashboard -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProviderDashboardHeader(state = state)
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (state.isLoading) {
                                Text(
                                    text = "Loading dashboard...",
                                    color = LightTextSecondary
                                )
                            }
                            QuickActionsSection()
                            EarningsSection(state = state)
                            KpiSection(state = state)
                            NewRequestsSection(state = state, onAccept = { bookingId -> viewModel.acceptBooking(bookingId) })
                            ActiveJobsSection(state = state)
                            if (state.errorMessage != null) {
                                Text(
                                    text = state.errorMessage,
                                    color = Accent
                                )
                            }
                        }
                    }
                }
            }

            ProviderDashboardTab.Vehicles -> {
                ProviderVehiclesScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            ProviderDashboardTab.Jobs -> {
                ProviderToBeImplemented(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            ProviderDashboardTab.Profile -> Unit
        }
    }
}

@Composable
private fun ProviderDashboardHeader(state: ProviderDashboardUiState) {
    ProviderHeaderGradient(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good afternoon",
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = state.providerDisplayName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderGlassStatCard(
                    icon = Icons.Outlined.Person,
                    value = state.vehiclesCount.toString(),
                    label = "Vehicles",
                    modifier = Modifier.weight(1f)
                )
                ProviderGlassStatCard(
                    icon = Icons.Outlined.Groups,
                    value = state.driversCount.toString(),
                    label = "Drivers",
                    modifier = Modifier.weight(1f)
                )
                ProviderGlassStatCard(
                    icon = Icons.Outlined.Inventory2,
                    value = state.activeJobsCount.toString(),
                    label = "Active Jobs",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection() {
    ProviderSectionHeader(title = "Quick Actions")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProviderQuickActionCard(
            icon = Icons.Outlined.Add,
            title = "Add Vehicle",
            onClick = {},
            modifier = Modifier.weight(1f)
        )
        ProviderQuickActionCard(
            icon = Icons.Outlined.Groups,
            title = "Add Driver",
            onClick = {},
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EarningsSection(state: ProviderDashboardUiState) {
    ProviderSectionHeader(
        title = "Earnings",
        trailingText = "View Details"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProviderMetricCard(
            icon = Icons.Outlined.AttachMoney,
            amount = formatRs(state.earningsToday),
            label = "Today",
            modifier = Modifier.weight(1f)
        )
        ProviderMetricCard(
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            amount = formatRs(state.earningsThisWeek),
            label = "This Week",
            modifier = Modifier.weight(1f)
        )
        ProviderMetricCard(
            icon = Icons.Outlined.CalendarMonth,
            amount = formatRs(state.earningsThisMonth),
            label = "This Month",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiSection(state: ProviderDashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProviderKpiCard(
            value = DecimalFormat("0.0").format(state.rating),
            label = "Rating",
            leadingIcon = Icons.Outlined.Star,
            modifier = Modifier.weight(1f)
        )
        ProviderKpiCard(
            value = state.trips.toString(),
            label = "Trips",
            modifier = Modifier.weight(1f)
        )
        ProviderKpiCard(
            value = "${state.onTimePercent}%",
            label = "On-Time",
            valueColor = Success,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NewRequestsSection(state: ProviderDashboardUiState, onAccept: (String) -> Unit) {

    ProviderSectionHeader(title = "New Requests")
    state.newRequests.forEach { request ->
        NewRequestCard(request, onAccept)
    }

    if (state.newRequests.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Text(
                text = "No new requests right now",
                color = LightTextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun NewRequestCard(request: ProviderNewRequestUi, onAccept: (String) -> Unit) {
    val (serviceBackground, serviceColor) = requestServiceColors(request.service)

    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Accent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProviderTag(
                        text = request.service,
                        backgroundColor = serviceBackground,
                        textColor = serviceColor
                    )
                    Text(
                        text = request.ageLabel,
                        color = LightTextSecondary
                    )
                }
                ProviderTrailingChevron()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderRoutePoint(text = request.pickup, isPickup = true)
                ProviderRoutePoint(text = request.destination, isPickup = false)
            }

            HorizontalDivider(color = LightBorder)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.width(16.dp)
                    )
                    Text(
                        text = "Assign a vehicle & driver to accept",
                        color = LightTextSecondary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Accept button
                Button(onClick = { onAccept(request.bookingId) }) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
private fun ActiveJobsSection(state: ProviderDashboardUiState) {

    ProviderSectionHeader(title = "Active Jobs", trailingText = "Track All")
    state.activeJobs.forEach { job ->
        ActiveJobCard(job)
    }

    if (state.activeJobs.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Text(
                text = "No active jobs",
                color = LightTextSecondary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ActiveJobCard(job: ProviderActiveJobUi) {
    val (serviceBackground, serviceColor) = requestServiceColors(job.service)
    val (stateBackground, stateColor) = statusColors(job.status)

    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProviderTag(
                        text = job.service,
                        backgroundColor = serviceBackground,
                        textColor = serviceColor
                    )
                    ProviderTag(
                        text = job.status,
                        backgroundColor = stateBackground,
                        textColor = stateColor
                    )
                }
                Text(text = job.code, color = LightTextSecondary)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderRoutePoint(text = job.pickup, isPickup = true)
                ProviderRoutePoint(text = job.destination, isPickup = false)
            }

            HorizontalDivider(color = LightBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Driver", color = LightTextSecondary)
                    Text(text = job.driver, color = LightTextPrimary, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ETA", color = LightTextSecondary)
                    Text(text = job.eta, color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(text = job.vehicle, color = LightTextSecondary)
        }
    }
}

private fun requestServiceColors(service: String): Pair<Color, Color> {
    return when (service) {
        "MoveMax" -> Secondary.copy(alpha = 0.14f) to Success
        "MoveBig" -> Accent.copy(alpha = 0.14f) to Accent
        else -> Primary.copy(alpha = 0.12f) to Primary
    }
}

private fun statusColors(status: String): Pair<Color, Color> {
    return when (status) {
        "In Transit" -> Primary.copy(alpha = 0.12f) to Primary
        "Loading" -> Accent.copy(alpha = 0.14f) to Accent
        else -> Secondary.copy(alpha = 0.14f) to Success
    }
}

private fun formatRs(value: Double): String {
    val formatter = DecimalFormat("#,###")
    return "Rs${formatter.format(value)}"
}
