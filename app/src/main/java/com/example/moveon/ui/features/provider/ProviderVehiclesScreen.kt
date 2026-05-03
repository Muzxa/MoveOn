package com.example.moveon.ui.features.provider

import androidx.compose.foundation.border

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.ui.components.ProviderTag
import com.example.moveon.util.VehicleCategoryHelper
import com.example.moveon.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderVehiclesScreen(
    modifier: Modifier = Modifier,
    triggerAddVehicle: Boolean = false,
    onAddVehicleTriggered: () -> Unit = {},
    viewModel: ProviderVehiclesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState
    val errorMessage = state.errorMessage
    val deleteCandidate = state.deleteCandidate
    val selectedTab = state.selectedTab

    LaunchedEffect(triggerAddVehicle) {
        if (triggerAddVehicle) {
            viewModel.onEvent(VehiclesEvent.OpenAddForm)
            onAddVehicleTriggered()
        }
    }

    val tabs = listOf("All", "MoveLite", "MoveBig", "MoveMax")
    
    val filteredVehicles = if (selectedTab == "All") {
        state.vehicles
    } else {
        state.vehicles.filter { it.type == selectedTab }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Blue Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                // Top Row: Back Button, Title, Subtitle, +Add Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Back button removed as per request
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Fleet Management",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.vehicles.size} vehicles registered",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.onEvent(VehiclesEvent.OpenAddForm) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tabs Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs) { tab ->
                        val isSelected = tab == selectedTab
                        val count = if (tab == "All") state.vehicles.size else state.vehicles.count { it.type == tab }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.15f))
                                .clickable { viewModel.onEvent(VehiclesEvent.TabChanged(tab)) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$tab ($count)",
                                color = if (isSelected) Primary else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // List Section
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                item {
                    Text(text = "Loading vehicles...", color = LightTextSecondary)
                }
            }

            items(filteredVehicles, key = { it.id }) { vehicle ->
                VehicleCard(
                    vehicle = vehicle,
                    onDelete = { viewModel.onEvent(VehiclesEvent.PromptDelete(vehicle)) }
                )
            }

            if (!state.isLoading && filteredVehicles.isEmpty()) {
                item {
                    Text(
                        text = "No vehicles found in this category.",
                        color = LightTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            if (errorMessage != null) {
                item {
                    Text(text = errorMessage, color = Accent, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    if (state.isFormVisible) {
        VehicleFormBottomSheet(
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
    onDelete: () -> Unit
) {
    val (catBg, catColor) = when(vehicle.type) {
        "MoveMax" -> Secondary.copy(alpha = 0.14f) to Success
        "MoveBig" -> Accent.copy(alpha = 0.14f) to Accent
        else -> Primary.copy(alpha = 0.12f) to Primary
    }

    val (statusBg, statusColor) = if (vehicle.isAvailable) {
        Success.copy(alpha = 0.12f) to Success
    } else {
        Accent.copy(alpha = 0.12f) to Accent
    }
    
    val statusText = if (vehicle.isAvailable) "Active" else "Idle"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
        ) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProviderTag(text = vehicle.type, backgroundColor = catBg, textColor = catColor)
                    ProviderTag(text = statusText, backgroundColor = statusBg, textColor = statusColor)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = LightTextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFF0F0F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = LightTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${vehicle.make} ${vehicle.model}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${vehicle.year} • ${vehicle.color}",
                            color = LightTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                
                // Plate Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vehicle.plateNumber,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LightBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Driver Assignment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.People, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No driver assigned",
                        color = LightTextSecondary,
                        fontSize = 14.sp
                    )
                }
                // Assign button removed as per request
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleFormBottomSheet(
    state: VehiclesUiState,
    onEvent: (VehiclesEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(VehiclesEvent.CloseForm) },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Add Vehicle", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add one or multiple vehicles at once", color = LightTextSecondary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onEvent(VehiclesEvent.CloseForm) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.Black, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = LightBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Form Content
            val capacityVal = state.form.capacityKg.toDoubleOrNull() ?: 0.0
            val volumeVal = state.form.volumeKg.toDoubleOrNull() ?: 0.0
            val computedCategory = VehicleCategoryHelper.determineCategory(volumeVal, capacityVal)

            Text("MoveOn Category *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryCard("MoveLite", "Small moves — apartments, rooms", computedCategory == "MoveLite", Modifier.weight(1f))
                CategoryCard("MoveBig", "Medium — homes, offices", computedCategory == "MoveBig", Modifier.weight(1f))
                CategoryCard("MoveMax", "Large — warehouses, full houses", computedCategory == "MoveMax", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleField("Make *", state.form.make, { onEvent(VehiclesEvent.MakeChanged(it)) }, "Toyota", Modifier.weight(1f))
                VehicleField("Model *", state.form.model, { onEvent(VehiclesEvent.ModelChanged(it)) }, "HiAce", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleField("Year *", state.form.year, { onEvent(VehiclesEvent.YearChanged(it)) }, "2020", Modifier.weight(1f), KeyboardType.Number)
                VehicleField("Color", state.form.color, { onEvent(VehiclesEvent.ColorChanged(it)) }, "White", Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleField("Capacity (kg) *", state.form.capacityKg, { onEvent(VehiclesEvent.CapacityChanged(it)) }, "1500", Modifier.weight(1f), KeyboardType.Number)
                VehicleField("Volume (m³) *", state.form.volumeKg, { onEvent(VehiclesEvent.VolumeChanged(it)) }, "800", Modifier.weight(1f), KeyboardType.Number)
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleField("Base Rate (Rs) *", state.form.baseRate, { onEvent(VehiclesEvent.BaseRateChanged(it)) }, "500", Modifier.weight(1f), KeyboardType.Number)
                VehicleField("Rate Per KM (Rs) *", state.form.ratePerKm, { onEvent(VehiclesEvent.RatePerKmChanged(it)) }, "25", Modifier.weight(1f), KeyboardType.Number)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("How many of this vehicle?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                
                // Toggle Switch
                Row(
                    modifier = Modifier
                        .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (!state.form.isMultiple) Color(0xFFF5F5F5) else Color.Transparent)
                            .clickable { onEvent(VehiclesEvent.IsMultipleChanged(false)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Single", fontSize = 12.sp, color = if (!state.form.isMultiple) Color.Black else LightTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (state.form.isMultiple) Color.White else Color.Transparent)
                            .clickable { onEvent(VehiclesEvent.IsMultipleChanged(true)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Multiple", fontSize = 12.sp, color = if (state.form.isMultiple) Color.Black else LightTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.form.isMultiple) "Plate Numbers (${state.form.plateNumbers.size} vehicles)" else "Plate Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                if (state.form.isMultiple) {
                    Text(
                        text = "+ Add more",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onEvent(VehiclesEvent.AddPlateNumber) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            state.form.plateNumbers.forEachIndexed { index, plate ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.form.isMultiple) {
                        Text("${index + 1}", color = LightTextSecondary, modifier = Modifier.width(20.dp))
                    }
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { onEvent(VehiclesEvent.PlateNumberChanged(index, it)) },
                        placeholder = { Text("ABC-1234", color = LightTextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (state.form.isMultiple) {
                        IconButton(onClick = { onEvent(VehiclesEvent.RemovePlateNumber(index)) }) {
                            Text("✕", color = LightTextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.form.isMultiple) {
                Text(
                    text = "ⓘ Same make/model, different plate numbers",
                    color = LightTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEvent(VehiclesEvent.SaveVehicle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                val btnText = if (state.form.isMultiple) "Add ${state.form.plateNumbers.size} Vehicles" else "Add Vehicle"
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryCard(title: String, subtitle: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Primary else LightBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) Primary.copy(alpha = 0.05f) else Color.White, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = LightTextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun VehicleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = LightTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF9F9F9),
                unfocusedContainerColor = Color(0xFFF9F9F9),
                disabledContainerColor = Color(0xFFF9F9F9),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
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
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(Accent.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = Accent)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Delete Vehicle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            }
        },
        text = { 
            Text(
                text = "This will permanently remove the vehicle ${vehicle.plateNumber} and its history. This can't be undone.",
                color = LightTextSecondary,
                fontSize = 14.sp
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Delete Vehicle")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Text(text = "✕  Cancel", color = Color.Black)
            }
        }
    )
}
