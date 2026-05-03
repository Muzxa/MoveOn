package com.example.moveon.ui.features.provider

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.components.LiveTrackingMap
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Success
import com.example.moveon.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import kotlin.math.ceil

@Composable
fun ProviderBookingRequestDetailScreen(
    request: ProviderNewRequestUi,
    onClose: () -> Unit,
    onAssignAndDispatch: (ProviderAssignmentOptionUi) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAssignment by remember(request.bookingId) {
        mutableStateOf<ProviderAssignmentOptionUi?>(
            request.assignmentOptions.firstOrNull { it.isAvailable }
                ?: request.assignmentOptions.firstOrNull()
        )
    }

    var remainingSeconds by remember(request.bookingId) { mutableStateOf(90) }

    LaunchedEffect(request.bookingId) {
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            remainingSeconds--
        }
    }
    val timerText = "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { selectedAssignment?.let(onAssignAndDispatch) },
                    enabled = selectedAssignment != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Accept & Dispatch")
                }
                TextButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                ) {
                    Text("Decline", color = LightTextPrimary)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF6F00), Color(0xFFF57C00))
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "New Booking Request",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = request.bookingCode,
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = request.service,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable(onClick = onClose),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Respond within:", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                        Text(timerText, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = Primary)
                            }
                            Column {
                                Text(request.customerName, color = LightTextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
                                    Text(String.format("%.1f", request.customerRating), color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                                    Text("•", color = LightTextSecondary)
                                    Text("${request.customerBookingCount} bookings", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Call, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RouteInfoRow("PICKUP", request.pickupScheduleLabel, request.pickup, true)
                        RouteInfoRow("DROP-OFF", request.dropOffEstimateLabel, request.destination, false)
                        HorizontalDivider(color = LightBorder)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Navigation, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(14.dp))
                                Text(String.format("%.1f km", request.distanceKm), color = LightTextPrimary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(14.dp))
                                Text(String.format("%.1f hrs", request.estimatedHours), color = LightTextPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFB300)),
                    border = BorderStroke(1.dp, Color(0x33FFB300)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Instructions", color = LightTextPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = request.instructions.ifBlank { "No special instructions." },
                            color = LightTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0D2E7D32)),
                    border = BorderStroke(1.dp, Color(0x332E7D32)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Fare", color = LightTextPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("Rs${request.totalFare.toInt()}", color = Success, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Platform fee: 8% · Your earnings: Rs${request.providerEarnings.toInt()}",
                            color = LightTextSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assign Vehicle & Driver", color = LightTextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Matching ${request.service}", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                }
            }

            items(request.assignmentOptions) { option ->
                val selected = selectedAssignment?.vehicleId == option.vehicleId && selectedAssignment?.driverId == option.driverId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { selectedAssignment = option },
                    colors = CardDefaults.cardColors(containerColor = if (selected) Color(0x0D2E7D32) else LightSurface),
                    border = BorderStroke(
                        1.dp,
                        when {
                            selected -> Success
                            !option.isAvailable -> LightBorder.copy(alpha = 0.7f)
                            else -> LightBorder
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(option.vehicleLabel, color = if (option.isAvailable) LightTextPrimary else LightTextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(option.serviceTag, color = Accent, style = MaterialTheme.typography.labelMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(option.driverLabel, color = if (option.isAvailable) LightTextPrimary else LightTextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("${String.format("%.1f", option.rating)}  •  ${option.trips} trips", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(option.statusLabel, color = if (option.isAvailable) Success else LightTextSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun RouteInfoRow(
    title: String,
    badge: String,
    location: String,
    pickup: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
            Text(
                badge,
                color = LightTextPrimary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (pickup) 10.dp else 12.dp)
                    .background(if (pickup) Primary else Accent, CircleShape)
            )
            Text(location, color = LightTextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProviderTripDetailsScreen(
    trip: ProviderActiveJobUi,
    vehicleLat: Double?,
    vehicleLng: Double?,
    onArrivedAtPickup: () -> Unit,
    onVerifyAndCompleteTrip: (String) -> Unit,
    onCallCustomer: () -> Unit,
    onChatCustomer: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehiclePosition = if (vehicleLat != null && vehicleLng != null) LatLng(vehicleLat, vehicleLng) else null
    val pickupLatLng = LatLng(trip.pickupLat, trip.pickupLng)
    val dropOffLatLng = LatLng(trip.dropOffLat, trip.dropOffLng)
    val routeOrigin = vehiclePosition ?: pickupLatLng
    val target = if (trip.status == "In Transit") dropOffLatLng else pickupLatLng

    val headingDistanceKm = LocationUtils.calculateDistanceKm(routeOrigin, target)
    val headingMins = ceil((headingDistanceKm / 28.0) * 60.0).toInt().coerceAtLeast(1)

    var routeToPickupPoints by remember(trip.bookingId, vehicleLat, vehicleLng) { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeToDropoffPoints by remember(trip.bookingId, vehicleLat, vehicleLng) { mutableStateOf<List<LatLng>>(emptyList()) }
    
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trip.bookingId, vehicleLat, vehicleLng, trip.status) {
        val toPickup = LocationUtils.fetchRouteOverview(context, routeOrigin, pickupLatLng)
        val pickupToDrop = LocationUtils.fetchRouteOverview(context, pickupLatLng, dropOffLatLng)
        val activeToDrop = LocationUtils.fetchRouteOverview(context, routeOrigin, dropOffLatLng)

        routeToPickupPoints = if (trip.status == "In Transit") emptyList() else toPickup.points
        routeToDropoffPoints = if (trip.status == "In Transit") activeToDrop.points else pickupToDrop.points
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                if (trip.status == "In Transit") {
                    Button(
                        onClick = { showOtpDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Icon(Icons.Outlined.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Complete Trip", modifier = Modifier.padding(start = 8.dp))
                    }
                } else {
                    Button(
                        onClick = onArrivedAtPickup,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Outlined.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Arrived at Pickup", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LiveTrackingMap(
                    pickupLat = trip.pickupLat,
                    pickupLng = trip.pickupLng,
                    dropOffLat = trip.dropOffLat,
                    dropOffLng = trip.dropOffLng,
                    vehicleLat = vehicleLat,
                    vehicleLng = vehicleLng,
                    routeToPickupPoints = routeToPickupPoints,
                    routeToDropoffPoints = routeToDropoffPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (trip.status == "In Transit") "Heading to Drop-off" else "Heading to Pickup", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                                Text("$headingMins mins", color = LightTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    String.format("%.1f km", headingDistanceKm),
                                    color = Primary,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, LightBorder, CircleShape)
                                        .clickable(onClick = onClose),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = LightTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, LightBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(Accent.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Accent)
                            }
                            Column {
                                Text("PICKUP LOCATION", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                                Text(trip.pickup, color = LightTextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, LightBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = Primary)
                                }
                                Column {
                                    Text(trip.customerName, color = LightTextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("Customer", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(40.dp).background(Success.copy(alpha = 0.12f), CircleShape).clickable(onClick = onCallCustomer), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Call, contentDescription = null, tint = Success)
                                }
                                Box(modifier = Modifier.size(40.dp).background(Primary.copy(alpha = 0.12f), CircleShape).clickable(onClick = onChatCustomer), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Primary)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, LightBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TripMetric("Boxes", trip.boxesCount.toString())
                            TripMetric("Distance", String.format("%.1f km", trip.distanceKm))
                            TripMetric("Fare", "Rs${trip.totalFare.toInt()}", valueColor = Success)
                        }
                    }
                }
            }
        }
    }

    if (showOtpDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Verify Completion", fontWeight = FontWeight.Bold, color = LightTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Please ask the customer for the 6-digit OTP to complete this trip.",
                        color = LightTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = otpInput,
                        onValueChange = { 
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                otpInput = it
                                otpError = null
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        singleLine = true,
                        isError = otpError != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextPrimary,
                            disabledTextColor = LightTextPrimary,
                            cursorColor = LightTextPrimary,
                            focusedBorderColor = LightBorder,
                            unfocusedBorderColor = LightBorder,
                            errorBorderColor = Accent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorSupportingTextColor = Accent
                        )
                    )
                    if (otpError != null) {
                        Text(otpError!!, color = Accent, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entered = otpInput.trim()
                        if (entered.length != 6) {
                            otpError = "Enter the 6-digit OTP shown to the customer."
                            return@Button
                        }
                        showOtpDialog = false
                        onVerifyAndCompleteTrip(entered)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel", color = LightTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun TripMetric(label: String, value: String, valueColor: Color = LightTextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = valueColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}
