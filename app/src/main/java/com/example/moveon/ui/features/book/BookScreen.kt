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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.data.session.CustomerActiveBookingState
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.Provider
import com.example.moveon.ui.components.ActiveMoveErrorCard
import com.example.moveon.ui.components.ActiveMoveLoadingCard
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.DualMarkerMapPreview
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
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
fun BookScreen(
    onTabSelected: (DashboardTab) -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isInteractingWithMap by remember { mutableStateOf(false) }
    val state = viewModel.state.value
    val selectedService = moveOnServiceOptions.firstOrNull { it.id == state.selectedServiceId }
    val selectedProvider = state.providers.firstOrNull { it.id == state.selectedProviderId }
    val providerCards = state.providers.map { it.toProviderCardUi() }
    val createdBooking = state.createdBooking
    val sessionState = state.customerBookingSession
    val sessionBooking = (sessionState as? CustomerActiveBookingState.Ready)?.booking
    val bookingForUi = createdBooking ?: sessionBooking
    val hasActiveBookingForUi = bookingForUi != null && bookingForUi.status != BookingStatus.COMPLETED
    val showSessionLoading =
        sessionState is CustomerActiveBookingState.Loading && !hasActiveBookingForUi
    val showSessionError =
        sessionState is CustomerActiveBookingState.Error && !hasActiveBookingForUi
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
                        .then(
                            if (!isInteractingWithMap) {
                                Modifier.verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when {
                        showSessionLoading -> ActiveMoveLoadingCard()
                        showSessionError -> {
                            val msg = (sessionState as CustomerActiveBookingState.Error).message
                            ActiveMoveErrorCard(message = msg, onRetry = viewModel::retryCustomerBookingSession)
                        }
                        bookingForUi != null -> {
                            val bookingProvider = state.createdProvider ?: selectedProvider
                            TripDetailsContent(
                                bookingForUi, bookingProvider, state.distanceKmText,
                                vehicleLat = state.vehicleLat,
                                vehicleLng = state.vehicleLng,
                                onMapTouchChanged = { isInteractingWithMap = it }
                            )
                        }
                        else -> when (state.currentStep) {
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

                    if (!showSessionLoading && !showSessionError && state.bookingError != null) {
                        Text(text = state.bookingError, style = MaterialTheme.typography.bodySmall, color = Error)
                    }
                    if (!showSessionLoading && !showSessionError && state.formError != null) {
                        Text(text = state.formError, style = MaterialTheme.typography.bodySmall, color = Error)
                    }
                }

                if (!showSessionLoading && !showSessionError) {
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
                            createdBooking?.status == BookingStatus.COMPLETED -> "Book Another Move"
                            state.currentStep > 1 -> "Back"
                            else -> null
                        },
                        onSecondaryClick = when {
                            createdBooking?.status == BookingStatus.COMPLETED -> viewModel::startNewBooking
                            state.currentStep > 1 -> ({ viewModel.onStepBack() })
                            else -> null
                        },
                        enabled = if (createdBooking != null) true else viewModel.canAdvance(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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

private data class PriceSummary(val baseRate: Double, val distanceCharge: Double, val serviceFee: Double) {
    val total: Double get() = baseRate + distanceCharge + serviceFee
}

private val moveOnServiceOptions = listOf(
    BookServiceCardUi(id = "move_lite", title = "MoveLite", subtitle = "Perfect for small moves like studio apartments or single rooms with minimal furniture", iconEmoji = "🚙"),
    BookServiceCardUi(id = "move_big", title = "MoveBig", subtitle = "Great for medium homes, apartments, or offices with standard furniture and belongings", iconEmoji = "🚚", recommended = true),
    BookServiceCardUi(id = "move_max", title = "MoveMax", subtitle = "Built for large homes or complete office relocations with heavy furniture and appliances", iconEmoji = "🚛")
)
