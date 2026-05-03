package com.example.moveon.ui.features.book

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.Provider
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.DualMarkerMapPreview
import com.example.moveon.ui.components.LiveTrackingMap
import com.example.moveon.ui.components.LocationPickerField
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.theme.Error
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.util.LocationPermissionHandler
import com.example.moveon.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil

@Composable
fun BookScreen(
    onTabSelected: (DashboardTab) -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state.value
    val selectedService = moveOnServiceOptions.firstOrNull { it.id == state.selectedServiceId }
    val selectedProvider = state.providers.firstOrNull { it.id == state.selectedProviderId }
    val providerCards = state.providers.map { it.toProviderCardUi() }
    val createdBooking = state.createdBooking
    val pricing = calculatePriceSummary(selectedProvider, state.distanceKmText.toDoubleOrNull())

    val openDatePicker: () -> Unit = {
        val now = Calendar.getInstance()
        val initialDate = Calendar.getInstance().apply {
            timeInMillis = state.selectedDateMillis ?: now.timeInMillis
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.onDatePicked(picked.timeInMillis)
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = now.timeInMillis
        }.show()
    }

    val openTimePicker: () -> Unit = {
        val now = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute -> viewModel.onTimePicked(hourOfDay, minute) },
            state.selectedHour ?: now.get(Calendar.HOUR_OF_DAY),
            state.selectedMinute ?: now.get(Calendar.MINUTE),
            false
        ).show()
    }

    LocationPermissionHandler { requestPermission, isPermissionGranted ->
        Scaffold(
            containerColor = LightBackground,
            bottomBar = {
                MoveOnBottomBar(
                    selectedTab = DashboardTab.Book,
                    onTabSelected = onTabSelected
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (createdBooking != null) {
                        val bookingProvider = state.createdProvider ?: selectedProvider
                        TripDetailsContent(
                            createdBooking, bookingProvider, state.distanceKmText,
                            vehicleLat = state.vehicleLat,
                            vehicleLng = state.vehicleLng
                        )
                    } else {
                        when (state.currentStep) {
                            1 -> {
                                BookStepHeader(title = "Select Your Vehicle", subtitle = "Fixed rates, no bidding required", step = 1)
                                moveOnServiceOptions.forEach { option ->
                                    BookServiceListCard(
                                        service = option,
                                        selected = state.selectedServiceId == option.id,
                                        onSelect = { viewModel.onServiceSelected(option.id) }
                                    )
                                }
                            }
                            2 -> {
                                BookStepHeader(title = "Choose Provider", subtitle = "Deal directly with our trusted providers", step = 2)
                                if (state.isLoadingProviders) {
                                    Text(text = "Loading available providers...", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                                }
                                if (state.providersError != null) {
                                    Text(
                                        text = state.providersError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Error,
                                        modifier = Modifier.clickable { viewModel.refreshProviders() }.padding(vertical = 4.dp)
                                    )
                                }
                                providerCards.forEach { option ->
                                    BookProviderListCard(
                                        provider = option,
                                        selected = state.selectedProviderId == option.id,
                                        onSelect = { viewModel.onProviderSelected(option.id) }
                                    )
                                }
                            }
                            else -> {
                                BookStepHeader(title = "Move Details", subtitle = "Fill in your location & time", step = 3)

                                // Dynamic map preview with both markers
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, LightBorder),
                                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = "Route Preview", style = MaterialTheme.typography.titleSmall, color = LightTextPrimary)
                                        DualMarkerMapPreview(
                                            pickupLat = state.pickupLat,
                                            pickupLng = state.pickupLng,
                                            dropOffLat = state.dropOffLat,
                                            dropOffLng = state.dropOffLng
                                        )
                                    }
                                }

                                // Location picker fields
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, LightBorder),
                                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        LocationPickerField(
                                            address = state.pickupAddress,
                                            onAddressChanged = viewModel::onPickupAddressChanged,
                                            lat = state.pickupLat,
                                            lng = state.pickupLng,
                                            onLocationResolved = viewModel::onPickupLocationResolved,
                                            placeholder = "House 55, Block J3, WAPDA Town, Lahore",
                                            label = "Pickup Address",
                                            onRequestLocationPermission = requestPermission,
                                            isLocationPermissionGranted = isPermissionGranted
                                        )

                                        LocationPickerField(
                                            address = state.dropOffAddress,
                                            onAddressChanged = viewModel::onDropOffAddressChanged,
                                            lat = state.dropOffLat,
                                            lng = state.dropOffLng,
                                            onLocationResolved = viewModel::onDropOffLocationResolved,
                                            placeholder = "House 57, Sector E, DHA Phase I, Lahore",
                                            label = "Drop-off Address",
                                            onRequestLocationPermission = requestPermission,
                                            isLocationPermissionGranted = isPermissionGranted
                                        )
                                    }
                                }

                                // Date/Time/Distance
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, LightBorder),
                                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = "Pickup Date", style = MaterialTheme.typography.titleSmall, color = LightTextPrimary)
                                        BookingPickerField(value = state.scheduledDateText, placeholder = "Select date", icon = Icons.Outlined.AccessTime, onClick = openDatePicker)
                                        Text(text = "Time", style = MaterialTheme.typography.titleSmall, color = LightTextPrimary)
                                        BookingPickerField(value = state.scheduledTimeText, placeholder = "Select time", icon = Icons.Outlined.AccessTime, onClick = openTimePicker)
                                        OutlinedTextField(
                                            value = state.distanceKmText,
                                            onValueChange = viewModel::onDistanceKmChanged,
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("Distance (km)") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = bookingFieldColors()
                                        )
                                    }
                                }

                                // Price Summary
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDDECF9)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFF9FC4E9))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "Price Summary", style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary)
                                        PriceRow(label = "Base Rate (${selectedService?.title ?: "Vehicle"})", value = formatPkr(pricing.baseRate))
                                        PriceRow(label = "Estimated Duration", value = formatPkr(pricing.distanceCharge))
                                        PriceRow(label = "Service Fee", value = formatPkr(pricing.serviceFee))
                                        HorizontalDivider(color = Color(0xFF9FC4E9))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Total", style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary)
                                            Text(text = formatPkr(pricing.total), style = MaterialTheme.typography.headlineSmall, color = Primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.bookingError != null) {
                        Text(text = state.bookingError, style = MaterialTheme.typography.bodySmall, color = Error)
                    }
                    if (state.formError != null) {
                        Text(text = state.formError, style = MaterialTheme.typography.bodySmall, color = Error)
                    }
                }

                BookActionFooter(
                    primaryLabel = when {
                        createdBooking?.status == BookingStatus.COMPLETED -> "Trip Completed"
                        createdBooking != null -> "View OTP"
                        state.currentStep < BookViewModel.TOTAL_STEPS -> "Next"
                        state.isSubmittingBooking -> "Confirming..."
                        else -> "Confirm Booking"
                    },
                    onPrimaryClick = { if (createdBooking?.status != BookingStatus.COMPLETED) viewModel.onPrimaryAction() },
                    secondaryLabel = when {
                        createdBooking != null -> "Book Another Move"
                        state.currentStep > 1 -> "Back"
                        else -> null
                    },
                    onSecondaryClick = when {
                        createdBooking != null -> viewModel::startNewBooking
                        state.currentStep > 1 -> ({ viewModel.onStepBack() })
                        else -> null
                    },
                    enabled = if (createdBooking != null) true else viewModel.canAdvance(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Show waiting overlay when provider response is pending
    if (state.isWaitingForProviderResponse && createdBooking != null) {
        BackHandler {
            viewModel.dismissWaitingForProviderResponse()
        }
        WaitingForProviderOverlay()
    }

    if (state.showOtpDialog && createdBooking != null) {
        var remainingSeconds by remember(createdBooking.id) { mutableStateOf(295) }
        
        LaunchedEffect(createdBooking.id) {
            while (remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                remainingSeconds--
            }
        }
        val timerText = "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"

        Dialog(onDismissRequest = viewModel::dismissOtpDialog) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "6-Digit OTP", style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary)
                    Text(text = "Once everything's been dropped off, please give your driver this code", style = MaterialTheme.typography.titleMedium, color = LightTextSecondary, textAlign = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        createdBooking.otp.padStart(6, '0').take(6).forEach { digit ->
                            Box(
                                modifier = Modifier.size(width = 42.dp, height = 46.dp).background(LightSurfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = digit.toString(), style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text(text = "Code expires in $timerText", style = MaterialTheme.typography.titleSmall, color = LightTextSecondary)
                    TextButton(onClick = viewModel::dismissOtpDialog) { Text("Done") }
                }
            }
        }
    }
}

@Composable
private fun BookingPickerField(
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        OutlinedTextField(
            value = value, onValueChange = {}, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = LightTextSecondary) },
            placeholder = { Text(placeholder) }, singleLine = true, readOnly = true, enabled = false,
            shape = RoundedCornerShape(10.dp), colors = bookingFieldColors()
        )
    }
}

@Composable
private fun bookingFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = LightSurface, unfocusedContainerColor = LightSurface, disabledContainerColor = LightSurface,
    focusedTextColor = LightTextPrimary, unfocusedTextColor = LightTextPrimary, disabledTextColor = LightTextPrimary,
    focusedPlaceholderColor = LightTextSecondary, unfocusedPlaceholderColor = LightTextSecondary, disabledPlaceholderColor = LightTextSecondary,
    focusedLeadingIconColor = LightTextSecondary, unfocusedLeadingIconColor = LightTextSecondary, disabledLeadingIconColor = LightTextSecondary,
    focusedIndicatorColor = LightBorder, unfocusedIndicatorColor = LightBorder, disabledIndicatorColor = LightBorder
)

@Composable
private fun TripDetailsContent(
    booking: Booking,
    provider: Provider?,
    distanceKmText: String,
    vehicleLat: Double? = null,
    vehicleLng: Double? = null
) {
    val context = LocalContext.current
    val scheduleLabel = formatEpochToTime(booking.scheduledTime)
    val completionLabel = formatEpochToTime(booking.scheduledTime + estimateDurationMinutes(distanceKmText) * 60_000L)
    val providerName = provider?.establishmentName?.ifBlank { "Assigned Provider" } ?: "Assigned Provider"
    val providerInitials = providerName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("").ifBlank { "MO" }
    val vehiclePosition = if (vehicleLat != null && vehicleLng != null) {
        LatLng(vehicleLat, vehicleLng)
    } else null

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
        val toPickupOverview = com.example.moveon.util.LocationUtils.fetchRouteOverview(
            context,
            routeOrigin,
            LatLng(booking.pickupLat, booking.pickupLng)
        )
        routeToPickupPoints = if (booking.status == BookingStatus.ACTIVE) {
            emptyList()
        } else {
            toPickupOverview.points
        }

        val pickupToDropOverview = com.example.moveon.util.LocationUtils.fetchRouteOverview(
            context,
            LatLng(booking.pickupLat, booking.pickupLng),
            LatLng(booking.dropOffLat, booking.dropOffLng)
        )

        val activeToDropOverview = com.example.moveon.util.LocationUtils.fetchRouteOverview(
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
                val totalSecs = toPickupOverview.durationSeconds + pickupToDropOverview.durationSeconds + trafficBufferSeconds
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

    // Live tracking map or fallback
    if (hasCoordinates) {
        LiveTrackingMap(
            pickupLat = booking.pickupLat,
            pickupLng = booking.pickupLng,
            dropOffLat = booking.dropOffLat,
            dropOffLng = booking.dropOffLng,
            vehicleLat = vehicleLat,
            vehicleLng = vehicleLng,
            routeToPickupPoints = routeToPickupPoints,
            routeToDropoffPoints = routeToDropoffPoints
        )
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().height(230.dp)
                .background(Color(0xFFDDECF9), RoundedCornerShape(20.dp)).padding(16.dp)
        ) {
            Text(text = "Pickup", modifier = Modifier.align(Alignment.BottomStart).background(Color.White, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelLarge, color = Primary)
            Text(text = "Drop-off", modifier = Modifier.align(Alignment.TopEnd).background(Color.White, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelLarge, color = LightTextPrimary)
            Box(modifier = Modifier.align(Alignment.Center).size(48.dp).background(Color(0xFFF4A261), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.LocalShipping, contentDescription = null, tint = Color.White)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF9FC4E9)), colors = CardDefaults.cardColors(containerColor = Color(0xFFDDECF9))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.AccessTime, contentDescription = null, tint = Color.White)
            }
            Column {
                val etaText = arrivalAtLabel ?: etaFromLiveLocationLabel ?: estimateEtaLabel(booking.scheduledTime)
                Text(text = "Arriving at", style = MaterialTheme.typography.titleMedium, color = LightTextSecondary)
                Text(text = etaText, style = MaterialTheme.typography.headlineLarge, color = Primary)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder), colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(Color(0xFFE8EDF3), CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = providerInitials, style = MaterialTheme.typography.titleSmall, color = Primary)
                }
                Column {
                    Text(text = providerName, style = MaterialTheme.typography.titleLarge, color = LightTextPrimary)
                    Text(text = "${provider?.rating ?: 4.8}", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val providerPhone = provider?.phoneNumber ?: ""
                CircleIconButton(icon = Icons.Outlined.Call, onClick = {
                    if (providerPhone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + providerPhone))
                        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                })

                CircleIconButton(icon = Icons.Outlined.ChatBubbleOutline, onClick = {
                    if (providerPhone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + providerPhone))
                        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                })
            }
        }
    }

    TripInfoCard(title = "Trip Information", rows = listOf(
        "Start Time" to scheduleLabel, "Est. Completion" to (completionAtLabel ?: completionLabel),
        "Total Distance" to "${distanceKmText.ifBlank { "-" }} km"
    ))

    TripInfoCard(title = "Route Information", rows = listOf(
        "Pickup Location" to booking.pickupAddress, "Drop-off Location" to booking.dropOffAddress
    ))
}

@Composable
private fun CircleIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(Color(0xFFF5F5F5), CircleShape)
            .border(1.dp, LightBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(imageVector = icon, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(16.dp)) }
}

@Composable
private fun TripInfoCard(title: String, rows: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, LightBorder), colors = CardDefaults.cardColors(containerColor = LightSurface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary)
            rows.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(text = label, style = MaterialTheme.typography.titleMedium, color = LightTextSecondary, modifier = Modifier.weight(1f))
                    Text(text = value, style = MaterialTheme.typography.titleSmall, color = LightTextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = LightTextSecondary)
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

private fun Provider.toProviderCardUi(): BookProviderCardUi {
    val prettyName = establishmentName.ifBlank { "Provider ${id.takeLast(4)}" }
    val safeRating = if (rating <= 0.0) 4.7 else rating
    val seed = abs(id.hashCode())
    return BookProviderCardUi(
        id = id, name = prettyName,
        initials = prettyName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("").ifBlank { "PR" },
        rating = String.format(Locale.US, "%.1f", safeRating),
        ratingCount = (140 + seed % 140).toString(),
        movesLabel = "${180 + seed % 170} moves",
        etaLabel = when (seed % 3) { 0 -> "45 min"; 1 -> "< 10 min"; else -> "< 15 min" },
        priceLabel = "Variable Rates"
    )
}

private fun calculatePriceSummary(provider: Provider?, distanceKm: Double?): PriceSummary {
    if (provider == null || distanceKm == null || distanceKm <= 0.0) return PriceSummary(0.0, 0.0, 500.0)
    return PriceSummary(baseRate = 500.0, distanceCharge = 25.0 * distanceKm, serviceFee = 500.0)
}

private fun formatPkr(value: Double): String = "PKR ${DecimalFormat("#,###").format(value)}"

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
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
}

private fun estimateEtaLabel(scheduledTime: Long): String {
    if (scheduledTime <= 0L) return "ETA unavailable"
    val diffMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(scheduledTime - System.currentTimeMillis())
    return when {
        diffMinutes <= 0L -> "Arriving soon"
        diffMinutes < 60L -> "${diffMinutes} mins"
        else -> "${diffMinutes / 60}h ${diffMinutes % 60}m"
    }
}

private data class PriceSummary(val baseRate: Double, val distanceCharge: Double, val serviceFee: Double) {
    val total: Double get() = baseRate + distanceCharge + serviceFee
}

private val moveOnServiceOptions = listOf(
    BookServiceCardUi(id = "move_lite", title = "MoveLite", subtitle = "Perfect for small moves like studio apartments or single rooms with minimal furniture", iconEmoji = "🚙"),
    BookServiceCardUi(id = "move_big", title = "MoveBig", subtitle = "Great for medium homes, apartments, or offices with standard furniture and belongings", iconEmoji = "🚚", recommended = true),
    BookServiceCardUi(id = "move_max", title = "MoveMax", subtitle = "Built for large homes or complete office relocations with heavy furniture and appliances", iconEmoji = "🚛")
)
