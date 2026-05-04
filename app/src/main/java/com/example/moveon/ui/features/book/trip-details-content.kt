package com.example.moveon.ui.features.book

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.Provider
import com.example.moveon.ui.components.LiveTrackingMap
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

@Composable
fun TripDetailsContent(
    booking: Booking,
    provider: Provider?,
    distanceKmText: String,
    vehicleLat: Double? = null,
    vehicleLng: Double? = null,
    onMapTouchChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scheduleLabel = formatEpochToTime(booking.scheduledTime)
    val completionLabel = formatEpochToTime(
        booking.scheduledTime + estimateDurationMinutes(distanceKmText) * 60_000L
    )
    val providerName = provider?.establishmentName?.ifBlank { "Assigned Provider" } ?: "Assigned Provider"
    val providerInitials = providerName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("").ifBlank { "MO" }
    val vehiclePosition = if (vehicleLat != null && vehicleLng != null) {
        LatLng(vehicleLat, vehicleLng)
    } else {
        null
    }

    val routeOrigin = when {
        vehiclePosition != null -> vehiclePosition
        provider != null && provider.businessLat != 0.0 -> LatLng(provider.businessLat, provider.businessLng)
        else -> LatLng(booking.pickupLat, booking.pickupLng)
    }
    val baseStartMillis = (booking.scheduledTime.takeIf { it > 0L } ?: booking.createdAt).takeIf { it > 0L }

    val etaFromLiveLocationLabel = vehiclePosition?.let { current ->
        val target = if (booking.status == BookingStatus.ACTIVE) {
            LatLng(booking.dropOffLat, booking.dropOffLng)
        } else {
            LatLng(booking.pickupLat, booking.pickupLng)
        }
        val remainingKm = LocationUtils.calculateDistanceKm(current, target)
        val assumedSpeedKmh = 28.0
        val etaMinutes = ceil((remainingKm / assumedSpeedKmh) * 60.0).toLong().coerceAtLeast(1L)
        val anchor = baseStartMillis ?: System.currentTimeMillis()
        formatEpochToTime(anchor + etaMinutes * 60_000L)
    }

    var routeToPickupPoints by remember(booking.id, routeOrigin) { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeToDropoffPoints by remember(booking.id) { mutableStateOf<List<LatLng>>(emptyList()) }
    var arrivalAtLabel by remember { mutableStateOf<String?>(null) }
    var completionAtLabel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(routeOrigin, booking.pickupLat, booking.dropOffLat, booking.status, vehicleLat, vehicleLng) {
        val toPickupOverview = LocationUtils.fetchRouteOverview(
            context,
            routeOrigin,
            LatLng(booking.pickupLat, booking.pickupLng)
        )
        routeToPickupPoints = if (booking.status == BookingStatus.ACTIVE) {
            emptyList()
        } else {
            toPickupOverview.points
        }

        val pickupToDropOverview = LocationUtils.fetchRouteOverview(
            context,
            LatLng(booking.pickupLat, booking.pickupLng),
            LatLng(booking.dropOffLat, booking.dropOffLng)
        )

        val activeToDropOverview = LocationUtils.fetchRouteOverview(
            context,
            routeOrigin,
            LatLng(booking.dropOffLat, booking.dropOffLng)
        )

        routeToDropoffPoints = if (booking.status == BookingStatus.ACTIVE) {
            activeToDropOverview.points
        } else {
            pickupToDropOverview.points
        }

        val activeLegDuration = if (booking.status == BookingStatus.ACTIVE) {
            activeToDropOverview.durationSeconds
        } else {
            toPickupOverview.durationSeconds
        }

        val trafficBufferSeconds = activeLegDuration
            ?.let { estimateTrafficBufferMinutes(it) * 60L }
            ?: 0L

        arrivalAtLabel = activeLegDuration?.let { durSecs ->
            val anchor = baseStartMillis ?: System.currentTimeMillis()
            val arrivalMillis = anchor + (durSecs + trafficBufferSeconds) * 1000L
            formatEpochToTime(arrivalMillis)
        }

        completionAtLabel = when {
            booking.status == BookingStatus.ACTIVE && activeToDropOverview.durationSeconds != null -> {
                val anchor = baseStartMillis ?: System.currentTimeMillis()
                val arrivalMillis = anchor +
                    (activeToDropOverview.durationSeconds + trafficBufferSeconds) * 1000L
                formatEpochToTime(arrivalMillis)
            }
            toPickupOverview.durationSeconds != null && pickupToDropOverview.durationSeconds != null -> {
                val totalSecs = toPickupOverview.durationSeconds + pickupToDropOverview.durationSeconds +
                    trafficBufferSeconds
                val anchor = baseStartMillis ?: System.currentTimeMillis()
                val arrivalMillis = anchor + totalSecs * 1000L
                formatEpochToTime(arrivalMillis)
            }
            pickupToDropOverview.durationSeconds != null -> {
                val anchor = baseStartMillis ?: System.currentTimeMillis()
                val arrivalMillis = anchor +
                    (pickupToDropOverview.durationSeconds + trafficBufferSeconds) * 1000L
                formatEpochToTime(arrivalMillis)
            }
            else -> null
        }
    }

    val hasCoordinates = booking.pickupLat != 0.0 && booking.dropOffLat != 0.0

    if (hasCoordinates) {
        LiveTrackingMap(
            pickupLat = booking.pickupLat,
            pickupLng = booking.pickupLng,
            dropOffLat = booking.dropOffLat,
            dropOffLng = booking.dropOffLng,
            vehicleLat = vehicleLat,
            vehicleLng = vehicleLng,
            routeToPickupPoints = routeToPickupPoints,
            routeToDropoffPoints = routeToDropoffPoints,
            modifier = Modifier.pointerInteropFilter { ev ->
                when (ev.actionMasked) {
                    MotionEvent.ACTION_DOWN -> onMapTouchChanged(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onMapTouchChanged(false)
                }
                false
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(Color(0xFFDDECF9), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Pickup",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Primary
            )
            Text(
                text = "Drop-off",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelLarge,
                color = LightTextPrimary
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color(0xFFF4A261), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF9FC4E9)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDECF9))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Column {
                val etaText = arrivalAtLabel ?: etaFromLiveLocationLabel ?: estimateEtaLabel(booking.scheduledTime)
                Text(text = "Arriving at", style = MaterialTheme.typography.titleMedium, color = LightTextSecondary)
                Text(text = etaText, style = MaterialTheme.typography.headlineLarge, color = Primary)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE8EDF3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = providerInitials,
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary
                    )
                }
                Column {
                    Text(text = providerName, style = MaterialTheme.typography.titleLarge, color = LightTextPrimary)
                    Text(
                        text = "${provider?.rating ?: 4.8}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val providerPhone = provider?.phoneNumber?.trim().orEmpty()
                val canContactProvider = providerPhone.isNotBlank()

                TripDetailsCircleIconButton(
                    icon = Icons.Outlined.Call,
                    enabled = canContactProvider,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$providerPhone"))
                        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )

                TripDetailsCircleIconButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    enabled = canContactProvider,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$providerPhone"))
                        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    TripInfoCard(
        title = "Trip Information",
        rows = listOf(
            "Start Time" to scheduleLabel,
            "Est. Completion" to (completionAtLabel ?: completionLabel),
            "Total Distance" to "${distanceKmText.ifBlank { "-" }} km"
        )
    )

    TripInfoCard(
        title = "Route Information",
        rows = listOf(
            "Pickup Location" to booking.pickupAddress,
            "Drop-off Location" to booking.dropOffAddress
        )
    )
}

@Composable
private fun TripDetailsCircleIconButton(
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(Color(0xFFF5F5F5), CircleShape)
            .border(1.dp, LightBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val tint = if (enabled) LightTextSecondary else LightTextSecondary.copy(alpha = 0.35f)
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun TripInfoCard(title: String, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary)
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = LightTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleSmall,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

private fun estimateDurationMinutes(distanceKmText: String): Long {
    val distance = distanceKmText.toDoubleOrNull() ?: return 225L
    return (distance * 5.0).coerceIn(40.0, 360.0).toLong()
}

private fun estimateTrafficBufferMinutes(routeDurationSeconds: Long): Long {
    return when {
        routeDurationSeconds <= 10 * 60L -> 2L
        routeDurationSeconds <= 30 * 60L -> 3L
        routeDurationSeconds <= 60 * 60L -> 4L
        else -> 5L
    }
}

private fun formatEpochToTime(millis: Long): String {
    if (millis <= 0L) return "-"
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
        .format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
}

private fun estimateEtaLabel(scheduledTime: Long): String {
    if (scheduledTime <= 0L) return "ETA unavailable"
    val diffMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(
        scheduledTime - System.currentTimeMillis()
    )
    return when {
        diffMinutes <= 0L -> "Arriving soon"
        diffMinutes < 60L -> "${diffMinutes} mins"
        else -> "${diffMinutes / 60}h ${diffMinutes % 60}m"
    }
}
