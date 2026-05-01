package com.example.moveon.ui.features.provider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.ui.components.ProviderSectionHeader
import com.example.moveon.ui.components.ProviderTag
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Success

@Composable
fun ProviderVehiclesScreen(
    modifier: Modifier = Modifier,
    viewModel: ProviderVehiclesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState
    val errorMessage = state.errorMessage
    val deleteCandidate = state.deleteCandidate

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProviderSectionHeader(title = "Vehicles")
                IconButton(onClick = { viewModel.onEvent(VehiclesEvent.OpenAddForm) }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add vehicle",
                        tint = Primary
                    )
                }
            }
        }

        if (state.isLoading) {
            item {
                Text(text = "Loading vehicles...", color = LightTextSecondary)
            }
        }

        items(state.vehicles, key = { it.id }) { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                onEdit = { viewModel.onEvent(VehiclesEvent.OpenEditForm(vehicle)) },
                onDelete = { viewModel.onEvent(VehiclesEvent.PromptDelete(vehicle)) },
                onAvailabilityChanged = { isAvailable ->
                    viewModel.onEvent(VehiclesEvent.ToggleAvailability(vehicle, isAvailable))
                }
            )
        }

        if (!state.isLoading && state.vehicles.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LightBorder)
                ) {
                    Text(
                        text = "No vehicles added yet.",
                        color = LightTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (errorMessage != null) {
            item {
                Text(text = errorMessage, color = Accent)
            }
        }
    }

    if (state.isFormVisible) {
        VehicleFormDialog(
            state = state,
            onEvent = viewModel::onEvent
        )
    }

    if (deleteCandidate != null) {
        ConfirmDeleteDialog(
            vehicle = deleteCandidate,
            onConfirm = { viewModel.onEvent(VehiclesEvent.ConfirmDelete) },
            onDismiss = { viewModel.onEvent(VehiclesEvent.DismissDelete) }
        )
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    val availabilityText = if (vehicle.isAvailable) "Available" else "Unavailable"
    val availabilityColor = if (vehicle.isAvailable) Success else Accent
    val availabilityBackground = availabilityColor.copy(alpha = 0.12f)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = vehicle.type.ifBlank { "Vehicle" },
                            color = LightTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = vehicle.plateNumber,
                            color = LightTextSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit vehicle",
                            tint = LightTextSecondary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete vehicle",
                            tint = Accent
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Capacity", color = LightTextSecondary, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${vehicle.maxCapacityKg} kg",
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                ProviderTag(
                    text = availabilityText,
                    backgroundColor = availabilityBackground,
                    textColor = availabilityColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Availability", color = LightTextSecondary)
                Switch(
                    checked = vehicle.isAvailable,
                    onCheckedChange = onAvailabilityChanged
                )
            }
        }
    }
}

@Composable
private fun VehicleFormDialog(
    state: VehiclesUiState,
    onEvent: (VehiclesEvent) -> Unit
) {
    val isEdit = state.form.id != null
    AlertDialog(
        onDismissRequest = { onEvent(VehiclesEvent.CloseForm) },
        title = {
            Text(text = if (isEdit) "Edit vehicle" else "Add vehicle")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VehicleField(
                    label = "Type",
                    value = state.form.type,
                    onValueChange = { onEvent(VehiclesEvent.TypeChanged(it)) },
                    placeholder = "Truck, Van"
                )
                VehicleField(
                    label = "Plate number",
                    value = state.form.plateNumber,
                    onValueChange = { onEvent(VehiclesEvent.PlateChanged(it)) },
                    placeholder = "ABC-123"
                )
                VehicleField(
                    label = "Capacity (kg)",
                    value = state.form.capacityKg,
                    onValueChange = { onEvent(VehiclesEvent.CapacityChanged(it)) },
                    placeholder = "1500",
                    keyboardType = KeyboardType.Number
                )
                VehicleField(
                    label = "Volume (kg)",
                    value = state.form.volumeKg,
                    onValueChange = { onEvent(VehiclesEvent.VolumeChanged(it)) },
                    placeholder = "800",
                    keyboardType = KeyboardType.Number
                )
                VehicleField(
                    label = "Make",
                    value = state.form.make,
                    onValueChange = { onEvent(VehiclesEvent.MakeChanged(it)) },
                    placeholder = "Toyota"
                )
                VehicleField(
                    label = "Model",
                    value = state.form.model,
                    onValueChange = { onEvent(VehiclesEvent.ModelChanged(it)) },
                    placeholder = "HiAce"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Available", color = LightTextSecondary)
                    Switch(
                        checked = state.form.isAvailable,
                        onCheckedChange = { onEvent(VehiclesEvent.AvailabilityChanged(it)) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(VehiclesEvent.SaveVehicle) }) {
                Text(text = if (isEdit) "Update" else "Save", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(VehiclesEvent.CloseForm) }) {
                Text(text = "Cancel", color = LightTextSecondary)
            }
        }
    )
}

@Composable
private fun VehicleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = LightTextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightSurfaceVariant,
                unfocusedContainerColor = LightSurfaceVariant,
                disabledContainerColor = LightSurfaceVariant,
                focusedIndicatorColor = LightBorder,
                unfocusedIndicatorColor = LightBorder,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedPlaceholderColor = Color.Black.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    vehicle: Vehicle,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete vehicle") },
        text = { Text(text = "Remove ${vehicle.type} (${vehicle.plateNumber}) from your fleet?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = LightTextSecondary)
            }
        }
    )
}
