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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.Provider
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.theme.Error
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

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
                    TripDetailsContent(createdBooking, selectedProvider, state.distanceKmText)
                } else {
                    when (state.currentStep) {
                        1 -> {
                            BookStepHeader(
                                title = "Select Your Vehicle",
                                subtitle = "Fixed rates, no bidding required",
                                step = 1
                            )
                            moveOnServiceOptions.forEach { option ->
                                BookServiceListCard(
                                    service = option,
                                    selected = state.selectedServiceId == option.id,
                                    onSelect = { viewModel.onServiceSelected(option.id) }
                                )
                            }
                        }

                        2 -> {
                            BookStepHeader(
                                title = "Choose Provider",
                                subtitle = "Deal directly with our trusted providers",
                                step = 2
                            )

                            if (state.isLoadingProviders) {
                                Text(
                                    text = "Loading available providers...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                            }

                            if (state.providersError != null) {
                                Text(
                                    text = state.providersError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error,
                                    modifier = Modifier
                                        .clickable { viewModel.refreshProviders() }
                                        .padding(vertical = 4.dp)
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
                            BookStepHeader(
                                title = "Move Details",
                                subtitle = "Fill in your location & time",
                                step = 3
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, LightBorder),
                                colors = CardDefaults.cardColors(containerColor = LightSurface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Pickup Address",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LightTextPrimary
                                    )
                                    OutlinedTextField(
                                        value = state.pickupAddress,
                                        onValueChange = viewModel::onPickupAddressChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = LightTextSecondary
                                            )
                                        },
                                        placeholder = { Text("House 55, Block J3, WAPDA Town, Lahore") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Text(
                                        text = "Drop-off Address",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LightTextPrimary
                                    )
                                    OutlinedTextField(
                                        value = state.dropOffAddress,
                                        onValueChange = viewModel::onDropOffAddressChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = LightTextSecondary
                                            )
                                        },
                                        placeholder = { Text("House 57, Sector E, DHA Phase I, Lahore") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, LightBorder),
                                colors = CardDefaults.cardColors(containerColor = LightSurface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Pickup Date",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LightTextPrimary
                                    )

                                    OutlinedTextField(
                                        value = state.scheduledDateText,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = openDatePicker),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = LightTextSecondary
                                            )
                                        },
                                        placeholder = { Text("Select date") },
                                        singleLine = true,
                                        readOnly = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Text(
                                        text = "Time",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LightTextPrimary
                                    )

                                    OutlinedTextField(
                                        value = state.scheduledTimeText,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = openTimePicker),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = LightTextSecondary
                                            )
                                        },
                                        placeholder = { Text("Select time") },
                                        singleLine = true,
                                        readOnly = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = state.distanceKmText,
                                        onValueChange = viewModel::onDistanceKmChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Distance (km)") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFDDECF9)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFF9FC4E9))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Price Summary",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = LightTextPrimary
                                    )
                                    PriceRow(
                                        label = "Base Rate (${selectedService?.title ?: "Vehicle"})",
                                        value = formatPkr(pricing.baseRate)
                                    )
                                    PriceRow(
                                        label = "Estimated Duration",
                                        value = formatPkr(pricing.distanceCharge)
                                    )
                                    PriceRow(
                                        label = "Service Fee",
                                        value = formatPkr(pricing.serviceFee)
                                    )

                                    HorizontalDivider(color = Color(0xFF9FC4E9))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = LightTextPrimary
                                        )
                                        Text(
                                            text = formatPkr(pricing.total),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.bookingError != null) {
                    Text(
                        text = state.bookingError,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error
                    )
                }

                if (state.formError != null) {
                    Text(
                        text = state.formError,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error
                    )
                }
            }

            BookActionFooter(
                primaryLabel = if (createdBooking != null) {
                    "View OTP"
                } else if (state.currentStep < BookViewModel.TOTAL_STEPS) {
                    "Next"
                } else if (state.isSubmittingBooking) {
                    "Confirming..."
                } else {
                    "Confirm Booking"
                },
                onPrimaryClick = { viewModel.onPrimaryAction() },
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

    if (state.showOtpDialog && createdBooking != null) {
        Dialog(onDismissRequest = viewModel::dismissOtpDialog) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "6-Digit OTP",
                        style = MaterialTheme.typography.headlineSmall,
                        color = LightTextPrimary
                    )

                    Text(
                        text = "Once everything's been dropped off, please give your driver this code",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        createdBooking.otp.padStart(6, '0').take(6).forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .size(width = 42.dp, height = 46.dp)
                                    .background(LightSurfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = LightTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Code expires in 4:55",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightTextSecondary
                    )

                    TextButton(onClick = viewModel::dismissOtpDialog) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun TripDetailsContent(
    booking: Booking,
    provider: Provider?,
    distanceKmText: String
) {
    val scheduleLabel = formatEpochToTime(booking.scheduledTime)
    val completionLabel = formatEpochToTime(booking.scheduledTime + estimateDurationMinutes(distanceKmText) * 60_000L)
    val providerName = provider?.establishmentName?.ifBlank { "Assigned Provider" } ?: "Assigned Provider"
    val providerInitials = providerName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2)
        .joinToString("")
        .ifBlank { "MO" }

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF9FC4E9)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDECF9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                Text(
                    text = "Arriving in",
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextSecondary
                )
                Text(
                    text = "40-45 mins",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Primary
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    Text(
                        text = providerName,
                        style = MaterialTheme.typography.titleLarge,
                        color = LightTextPrimary
                    )
                    Text(
                        text = "${provider?.rating ?: 4.8}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleIconButton(icon = Icons.Outlined.Call)
                CircleIconButton(icon = Icons.Outlined.ChatBubbleOutline)
            }
        }
    }

    TripInfoCard(
        title = "Trip Information",
        rows = listOf(
            "Move ID" to booking.id,
            "Start Time" to scheduleLabel,
            "Est. Completion" to completionLabel,
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
private fun CircleIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(Color(0xFFF5F5F5), CircleShape)
            .border(1.dp, LightBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LightTextSecondary,
            modifier = Modifier.size(16.dp)
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = LightTextPrimary
            )

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

@Composable
private fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = LightTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = LightTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun Provider.toProviderCardUi(): BookProviderCardUi {
    val prettyName = establishmentName.ifBlank { "Provider ${id.takeLast(4)}" }
    val safeRating = if (rating <= 0.0) 4.7 else rating
    val seed = abs(id.hashCode())

    return BookProviderCardUi(
        id = id,
        name = prettyName,
        initials = prettyName
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .take(2)
            .joinToString("")
            .ifBlank { "PR" },
        rating = String.format(Locale.US, "%.1f", safeRating),
        ratingCount = (140 + seed % 140).toString(),
        movesLabel = "${180 + seed % 170} moves",
        etaLabel = when (seed % 3) {
            0 -> "45 min"
            1 -> "< 10 min"
            else -> "< 15 min"
        },
        priceLabel = "Base ${formatPkr(baseRate)}"
    )
}

private fun calculatePriceSummary(provider: Provider?, distanceKm: Double?): PriceSummary {
    if (provider == null || distanceKm == null || distanceKm <= 0.0) {
        return PriceSummary(0.0, 0.0, 500.0)
    }

    val distanceCharge = provider.ratePerKm * distanceKm
    return PriceSummary(
        baseRate = provider.baseRate,
        distanceCharge = distanceCharge,
        serviceFee = 500.0
    )
}

private fun formatPkr(value: Double): String {
    val formatter = DecimalFormat("#,###")
    return "PKR ${formatter.format(value)}"
}

private fun estimateDurationMinutes(distanceKmText: String): Long {
    val distance = distanceKmText.toDoubleOrNull() ?: return 225L
    return (distance * 5.0).coerceIn(40.0, 360.0).toLong()
}

private fun formatEpochToTime(millis: Long): String {
    if (millis <= 0L) return "-"
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
}

private data class PriceSummary(
    val baseRate: Double,
    val distanceCharge: Double,
    val serviceFee: Double
) {
    val total: Double get() = baseRate + distanceCharge + serviceFee
}

private val moveOnServiceOptions = listOf(
    BookServiceCardUi(
        id = "move_lite",
        title = "MoveLite",
        subtitle = "Perfect for small moves like studio apartments or single rooms with minimal furniture",
        iconEmoji = "🚙",
        baseRateLabel = "PKR 3,500"
    ),
    BookServiceCardUi(
        id = "move_big",
        title = "MoveBig",
        subtitle = "Great for medium homes, apartments, or offices with standard furniture and belongings",
        iconEmoji = "🚚",
        recommended = true,
        baseRateLabel = "PKR 5,000"
    ),
    BookServiceCardUi(
        id = "move_max",
        title = "MoveMax",
        subtitle = "Built for large homes or complete office relocations with heavy furniture and appliances",
        iconEmoji = "🚛",
        baseRateLabel = "PKR 7,000"
    )
)
