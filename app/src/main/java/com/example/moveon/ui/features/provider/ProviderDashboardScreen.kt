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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

private data class NewRequestUi(
    val service: String,
    val serviceBackground: Color,
    val serviceColor: Color,
    val age: String,
    val pickup: String,
    val destination: String
)

private data class ActiveJobUi(
    val service: String,
    val serviceBackground: Color,
    val serviceColor: Color,
    val state: String,
    val stateBackground: Color,
    val stateColor: Color,
    val code: String,
    val pickup: String,
    val destination: String,
    val driver: String,
    val eta: String,
    val vehicle: String
)

@Composable
fun ProviderDashboardScreen(
    onOpenProfile: () -> Unit
) {
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
        if (selectedTab != ProviderDashboardTab.Dashboard && selectedTab != ProviderDashboardTab.Profile) {
            ProviderToBeImplemented(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProviderDashboardHeader()
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionsSection()
                    EarningsSection()
                    KpiSection()
                    NewRequestsSection()
                    ActiveJobsSection()
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ProviderDashboardHeader() {
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
                        text = "Ahmed Transport",
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
                    value = "5",
                    label = "Vehicles",
                    modifier = Modifier.weight(1f)
                )
                ProviderGlassStatCard(
                    icon = Icons.Outlined.Groups,
                    value = "4",
                    label = "Drivers",
                    modifier = Modifier.weight(1f)
                )
                ProviderGlassStatCard(
                    icon = Icons.Outlined.Inventory2,
                    value = "2",
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
private fun EarningsSection() {
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
            amount = "Rs18,450",
            label = "Today",
            modifier = Modifier.weight(1f)
        )
        ProviderMetricCard(
            icon = Icons.Outlined.TrendingUp,
            amount = "Rs95,200",
            label = "This Week",
            modifier = Modifier.weight(1f)
        )
        ProviderMetricCard(
            icon = Icons.Outlined.CalendarMonth,
            amount = "Rs383K",
            label = "This Month",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProviderKpiCard(
            value = "4.8",
            label = "Rating",
            leadingIcon = Icons.Outlined.Star,
            modifier = Modifier.weight(1f)
        )
        ProviderKpiCard(
            value = "547",
            label = "Trips",
            modifier = Modifier.weight(1f)
        )
        ProviderKpiCard(
            value = "97%",
            label = "On-Time",
            valueColor = Success,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NewRequestsSection() {
    val requests = listOf(
        NewRequestUi(
            service = "MoveMax",
            serviceBackground = Secondary.copy(alpha = 0.14f),
            serviceColor = Success,
            age = "3 mins ago",
            pickup = "Gulberg III, Lahore",
            destination = "Model Town, Lahore"
        ),
        NewRequestUi(
            service = "MoveBig",
            serviceBackground = Accent.copy(alpha = 0.14f),
            serviceColor = Accent,
            age = "7 mins ago",
            pickup = "Saddar, Rawalpindi",
            destination = "Bahria Town, Islamabad"
        )
    )

    ProviderSectionHeader(title = "New Requests")
    requests.forEach { request ->
        NewRequestCard(request)
    }
}

@Composable
private fun NewRequestCard(request: NewRequestUi) {
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
                        backgroundColor = request.serviceBackground,
                        textColor = request.serviceColor
                    )
                    Text(
                        text = request.age,
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
        }
    }
}

@Composable
private fun ActiveJobsSection() {
    val jobs = listOf(
        ActiveJobUi(
            service = "MoveBig",
            serviceBackground = Accent.copy(alpha = 0.14f),
            serviceColor = Accent,
            state = "In Transit",
            stateBackground = Primary.copy(alpha = 0.12f),
            stateColor = Primary,
            code = "#0312",
            pickup = "Blue Area, Islamabad",
            destination = "DHA Phase 5, Lahore",
            driver = "Usman Ali",
            eta = "2h 15m",
            vehicle = "Hino Dutro - LHR-4521"
        ),
        ActiveJobUi(
            service = "MoveLite",
            serviceBackground = Primary.copy(alpha = 0.12f),
            serviceColor = Primary,
            state = "Loading",
            stateBackground = Accent.copy(alpha = 0.14f),
            stateColor = Accent,
            code = "#0314",
            pickup = "F-10 Markaz, Islamabad",
            destination = "G-11/4, Islamabad",
            driver = "Faisal Mehmood",
            eta = "30m",
            vehicle = "Suzuki Bolan - ISB-7823"
        )
    )

    ProviderSectionHeader(title = "Active Jobs", trailingText = "Track All")
    jobs.forEach { job ->
        ActiveJobCard(job)
    }
}

@Composable
private fun ActiveJobCard(job: ActiveJobUi) {
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
                        backgroundColor = job.serviceBackground,
                        textColor = job.serviceColor
                    )
                    ProviderTag(
                        text = job.state,
                        backgroundColor = job.stateBackground,
                        textColor = job.stateColor
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

