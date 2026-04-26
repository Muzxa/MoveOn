package com.example.moveon.ui.features.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary

@Composable
fun BookScreen(
    onTabSelected: (DashboardTab) -> Unit
) {
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    var selectedServiceId by rememberSaveable { mutableStateOf("") }
    var selectedProviderId by rememberSaveable { mutableStateOf("") }
    var pickupAddress by rememberSaveable { mutableStateOf("") }
    var dropOffAddress by rememberSaveable { mutableStateOf("") }
    var distanceKmText by rememberSaveable { mutableStateOf("") }
    var scheduledDateText by rememberSaveable { mutableStateOf("") }
    var scheduledTimeText by rememberSaveable { mutableStateOf("") }

    val selectedService = remember(selectedServiceId) {
        moveOnServiceOptions.firstOrNull { it.id == selectedServiceId }
    }
    val selectedProvider = remember(selectedProviderId) {
        moveOnProviderOptions.firstOrNull { it.id == selectedProviderId }
    }

    val canAdvance = when (currentStep) {
        1 -> selectedServiceId.isNotBlank()
        2 -> selectedProviderId.isNotBlank()
        3 -> pickupAddress.isNotBlank() && dropOffAddress.isNotBlank() && distanceKmText.isNotBlank()
        else -> true
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
                when (currentStep) {
                    1 -> {
                        BookStepHeader(
                            title = "Vehicle Selection",
                            subtitle = "Choose a move size and vehicle category.",
                            step = 1,
                            totalSteps = 4
                        )

                        moveOnServiceOptions.forEach { option ->
                            BookServiceListCard(
                                service = option,
                                selected = selectedServiceId == option.id,
                                onSelect = {
                                    selectedServiceId = option.id
                                    selectedProviderId = ""
                                }
                            )
                        }
                    }

                    2 -> {
                        BookStepHeader(
                            title = "Pick Provider",
                            subtitle = "Select a verified provider for your selected vehicle.",
                            step = 2,
                            totalSteps = 4
                        )

                        moveOnProviderOptions.forEach { option ->
                            BookProviderListCard(
                                provider = option,
                                selected = selectedProviderId == option.id,
                                onSelect = { selectedProviderId = option.id }
                            )
                        }
                    }

                    3 -> {
                        BookStepHeader(
                            title = "Booking Details",
                            subtitle = "Enter route and schedule details for your move.",
                            step = 3,
                            totalSteps = 4
                        )

                        OutlinedTextField(
                            value = pickupAddress,
                            onValueChange = { pickupAddress = it },
                            label = { Text("Pickup Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dropOffAddress,
                            onValueChange = { dropOffAddress = it },
                            label = { Text("Drop-off Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = distanceKmText,
                            onValueChange = { distanceKmText = it },
                            label = { Text("Distance (km)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = scheduledDateText,
                            onValueChange = { scheduledDateText = it },
                            label = { Text("Move Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = scheduledTimeText,
                            onValueChange = { scheduledTimeText = it },
                            label = { Text("Move Time") },
                            placeholder = { Text("HH:MM") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    else -> {
                        BookStepHeader(
                            title = "Trip Details",
                            subtitle = "Review your booking details before confirmation.",
                            step = 4,
                            totalSteps = 4
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LightSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, LightBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = selectedService?.title ?: "Service pending",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LightTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = selectedProvider?.name ?: "Provider pending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "From: ${pickupAddress.ifBlank { "-" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "To: ${dropOffAddress.ifBlank { "-" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "Distance: ${distanceKmText.ifBlank { "-" }} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "Schedule: ${scheduledDateText.ifBlank { "-" }} ${scheduledTimeText.ifBlank { "" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Submission and confirmation will be finalized in the next implementation step.",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LightTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            BookActionFooter(
                primaryLabel = if (currentStep < 4) "Continue" else "Continue to Confirm",
                onPrimaryClick = {
                    if (currentStep < 4) {
                        currentStep += 1
                    }
                },
                secondaryLabel = if (currentStep > 1) "Back" else null,
                onSecondaryClick = if (currentStep > 1) {
                    { currentStep -= 1 }
                } else {
                    null
                },
                enabled = canAdvance,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private val moveOnServiceOptions = listOf(
    BookServiceCardUi(
        id = "suzuki",
        title = "Suzuki Pickup",
        subtitle = "Ideal for studio moves and light furniture",
        capacityLabel = "Up to 600 kg",
        etaLabel = "ETA 20-30 min",
        baseRateLabel = "From PKR 2,500"
    ),
    BookServiceCardUi(
        id = "shahzore",
        title = "Shahzore",
        subtitle = "Great for 1-2 bedroom apartment moves",
        capacityLabel = "Up to 1,200 kg",
        etaLabel = "ETA 25-35 min",
        baseRateLabel = "From PKR 4,500"
    ),
    BookServiceCardUi(
        id = "mazda",
        title = "Mazda Truck",
        subtitle = "Best for full home and office relocation",
        capacityLabel = "Up to 2,500 kg",
        etaLabel = "ETA 35-45 min",
        baseRateLabel = "From PKR 7,500"
    )
)

private val moveOnProviderOptions = listOf(
    BookProviderCardUi(
        id = "provider_a",
        name = "FastMove Logistics",
        ratingLabel = "4.8 (310 trips)",
        fleetLabel = "12 vehicles",
        priceLabel = "Base PKR 3,200",
        etaLabel = "Available now"
    ),
    BookProviderCardUi(
        id = "provider_b",
        name = "CityRelocate Co.",
        ratingLabel = "4.6 (280 trips)",
        fleetLabel = "9 vehicles",
        priceLabel = "Base PKR 2,900",
        etaLabel = "Available in 15 min"
    ),
    BookProviderCardUi(
        id = "provider_c",
        name = "MoveOn Partners",
        ratingLabel = "4.9 (420 trips)",
        fleetLabel = "15 vehicles",
        priceLabel = "Base PKR 3,400",
        etaLabel = "Available now"
    )
)
