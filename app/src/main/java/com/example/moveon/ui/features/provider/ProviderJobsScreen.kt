package com.example.moveon.ui.features.provider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.ProviderRoutePoint
import com.example.moveon.ui.components.ProviderSectionHeader
import com.example.moveon.ui.components.ProviderTag
import com.example.moveon.ui.components.ProviderTrailingChevron
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Secondary
import com.example.moveon.ui.theme.Success

@Composable
fun ProviderJobsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProviderDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pending Requests Section
        item {
            ProviderSectionHeader(title = "Pending Requests")
        }

        if (state.newRequests.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBorder)
                ) {
                    Text(
                        text = "No pending requests",
                        color = LightTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.newRequests) { request ->
                PendingRequestCard(
                    request = request,
                    onAccept = { viewModel.acceptBooking(request.bookingId) }
                )
            }
        }

        item {
            HorizontalDivider(color = LightBorder, modifier = Modifier.padding(vertical = 8.dp))
        }

        // Active Jobs Section
        item {
            ProviderSectionHeader(title = "Active Jobs (${state.activeJobsCount})")
        }

        if (state.activeJobs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightBorder)
                ) {
                    Text(
                        text = "No active jobs right now",
                        color = LightTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.activeJobs) { job ->
                ActiveJobCard(job = job)
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: ProviderNewRequestUi,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (serviceBackground, serviceColor) = requestServiceColors(request.service)

    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Accent),
        modifier = modifier
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
                        color = LightTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                ProviderTrailingChevron()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderRoutePoint(text = request.pickup, isPickup = true)
                ProviderRoutePoint(text = request.destination, isPickup = false)
            }

            HorizontalDivider(color = LightBorder)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Assign a vehicle & driver to accept",
                        color = LightTextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Accept button
                Button(onClick = onAccept) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
private fun ActiveJobCard(
    job: ProviderActiveJobUi,
    modifier: Modifier = Modifier
) {
    val (serviceBackground, serviceColor) = requestServiceColors(job.service)

    Card(
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder),
        modifier = modifier
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
                    Text(
                        text = job.code,
                        color = LightTextPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.2f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = job.status,
                        color = Success,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Driver: ${job.driver}",
                        color = LightTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Vehicle: ${job.vehicle}",
                        color = LightTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ETA",
                        color = LightTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = job.eta,
                        color = LightTextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun requestServiceColors(service: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (service) {
        "MoveLite" -> androidx.compose.ui.graphics.Color(0xFFE8F5E9) to androidx.compose.ui.graphics.Color(0xFF2E7D32)
        "MoveBig" -> androidx.compose.ui.graphics.Color(0xFFE3F2FD) to androidx.compose.ui.graphics.Color(0xFF1565C0)
        "MoveMax" -> androidx.compose.ui.graphics.Color(0xFFFCE4EC) to androidx.compose.ui.graphics.Color(0xFFC2185B)
        else -> androidx.compose.ui.graphics.Color(0xFFF0F0F0) to androidx.compose.ui.graphics.Color(0xFF666666)
    }
}
