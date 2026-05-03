package com.example.moveon.ui.features.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.repository.LogisticsRepository
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackBookingViewModel @Inject constructor(
    private val repo: LogisticsRepository
) : ViewModel() {
    var state by mutableStateOf(TrackBookingState(isLoading = true))
        private set

    fun load(bookingId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            repo.getBookingById(bookingId)
                .onSuccess { booking ->
                    state = TrackBookingState(isLoading = false, booking = booking)
                }
                .onFailure { e ->
                    state = TrackBookingState(isLoading = false, error = e.message ?: "Unable to load booking.")
                }
        }
    }
}

data class TrackBookingState(
    val isLoading: Boolean = false,
    val booking: Booking? = null,
    val error: String? = null
)

@Composable
fun TrackBookingScreen(
    bookingId: String,
    onBack: () -> Unit,
    viewModel: TrackBookingViewModel = hiltViewModel()
) {
    LaunchedEffect(bookingId) { viewModel.load(bookingId) }
    val state = viewModel.state

    Scaffold(containerColor = LightBackground, topBar = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LightTextPrimary)
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Move Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = LightTextPrimary)
            Text("Booking: $bookingId", style = MaterialTheme.typography.bodyMedium, color = LightTextSecondary)

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.isLoading) {
                Text("Loading...", color = LightTextSecondary)
            }

            state.booking?.let { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailRow("Status", booking.status.name)
                        DetailRow("Pickup", booking.pickupAddress)
                        DetailRow("Drop-off", booking.dropOffAddress)
                        DetailRow("Fare", booking.totalFare.toString())
                        DetailRow("Scheduled", booking.scheduledTime.toString())
                        if (booking.rating > 0f) DetailRow("Rating", booking.rating.toString())
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // If you want map + live route here, we can reuse LiveTrackingMap next.
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = LightTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = LightTextPrimary)
    }
}

