package com.example.moveon.ui.features.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.Provider
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun BookScreen(
    onTabSelected: (DashboardTab) -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val selectedService = moveOnServiceOptions.firstOrNull { it.id == state.selectedServiceId }
    val selectedProvider = state.providers.firstOrNull { it.id == state.selectedProviderId }
    val providerCards = state.providers.map { provider ->
        provider.toProviderCardUi()
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
                when (state.currentStep) {
                    1 -> {
                        BookStepHeader(
                            title = "Vehicle Selection",
                            subtitle = "Choose a move size and vehicle category.",
                            step = 1,
                            totalSteps = BookViewModel.TOTAL_STEPS
                        )

                        moveOnServiceOptions.forEach { option ->
                            BookServiceListCard(
                                service = option,
                                selected = state.selectedServiceId == option.id,
                                onSelect = {
                                    viewModel.onServiceSelected(option.id)
                                }
                            )
                        }
                    }

                    2 -> {
                        BookStepHeader(
                            title = "Pick Provider",
                            subtitle = "Select a verified provider for your selected vehicle.",
                            step = 2,
                            totalSteps = BookViewModel.TOTAL_STEPS
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
                                color = LightTextSecondary,
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

                        if (!state.isLoadingProviders && providerCards.isEmpty() && state.providersError == null) {
                            Text(
                                text = "No verified providers are available right now.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        }
                    }

                    3 -> {
                        BookStepHeader(
                            title = "Booking Details",
                            subtitle = "Enter route and schedule details for your move.",
                            step = 3,
                            totalSteps = BookViewModel.TOTAL_STEPS
                        )

                        OutlinedTextField(
                            value = state.pickupAddress,
                            onValueChange = viewModel::onPickupAddressChanged,
                            label = { Text("Pickup Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.dropOffAddress,
                            onValueChange = viewModel::onDropOffAddressChanged,
                            label = { Text("Drop-off Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.distanceKmText,
                            onValueChange = viewModel::onDistanceKmChanged,
                            label = { Text("Distance (km)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.scheduledDateText,
                            onValueChange = viewModel::onScheduledDateChanged,
                            label = { Text("Move Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.scheduledTimeText,
                            onValueChange = viewModel::onScheduledTimeChanged,
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
                            totalSteps = BookViewModel.TOTAL_STEPS
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
                                    text = selectedProvider?.establishmentName?.ifBlank { "Provider pending" } ?: "Provider pending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "From: ${state.pickupAddress.ifBlank { "-" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "To: ${state.dropOffAddress.ifBlank { "-" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "Distance: ${state.distanceKmText.ifBlank { "-" }} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "Schedule: ${state.scheduledDateText.ifBlank { "-" }} ${state.scheduledTimeText.ifBlank { "" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = estimateFareLabel(
                                        provider = selectedProvider,
                                        distanceText = state.distanceKmText
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LightTextPrimary
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
                primaryLabel = if (state.currentStep < BookViewModel.TOTAL_STEPS) {
                    "Continue"
                } else {
                    "Continue to Confirm"
                },
                onPrimaryClick = {
                    viewModel.onStepAdvance()
                },
                secondaryLabel = if (state.currentStep > 1) "Back" else null,
                onSecondaryClick = if (state.currentStep > 1) {
                    { viewModel.onStepBack() }
                } else {
                    null
                },
                enabled = viewModel.canAdvance(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun Provider.toProviderCardUi(): BookProviderCardUi {
    val prettyName = establishmentName.ifBlank { "Provider ${id.takeLast(4)}" }
    val ratingNumber = if (rating <= 0.0) "New" else String.format(Locale.US, "%.1f", rating)

    return BookProviderCardUi(
        id = id,
        name = prettyName,
        ratingLabel = "$ratingNumber rating",
        fleetLabel = "Rate/km ${formatPkr(ratePerKm)}",
        priceLabel = "Base ${formatPkr(baseRate)}",
        etaLabel = if (isVerified) "Verified" else "Pending verification"
    )
}

private fun estimateFareLabel(
    provider: Provider?,
    distanceText: String
): String {
    val parsedDistance = distanceText.toDoubleOrNull()
    if (provider == null || parsedDistance == null) {
        return "Estimated fare appears after provider and distance selection."
    }

    val estimatedFare = provider.baseRate + (provider.ratePerKm * parsedDistance)
    return "Estimated Fare: ${formatPkr(estimatedFare)}"
}

private fun formatPkr(value: Double): String {
    val formatter = DecimalFormat("#,###")
    return "PKR ${formatter.format(value)}"
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
