package com.example.moveon.ui.features.provider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt1
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
import com.example.moveon.domain.model.Driver
import com.example.moveon.domain.model.Vehicle
import com.example.moveon.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDriversScreen(
    modifier: Modifier = Modifier,
    triggerAddDriver: Boolean = false,
    onAddDriverTriggered: () -> Unit = {},
    viewModel: ProviderDriversViewModel = hiltViewModel()
) {
    val state by viewModel.uiState

    LaunchedEffect(triggerAddDriver) {
        if (triggerAddDriver) {
            viewModel.onEvent(DriversEvent.OpenAddForm)
            onAddDriverTriggered()
        }
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
                    .padding(top = 16.dp, bottom = 20.dp)
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { /* Back handled by tabs */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Driver Management",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.drivers.size} drivers registered",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.onEvent(DriversEvent.OpenAddForm) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Cards Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val availableCount = state.drivers.count { it.status == "Available" }
                    val onTripCount = state.drivers.count { it.status == "On Trip" }
                    val offDutyCount = state.drivers.count { it.status == "Off Duty" }

                    StatCard(count = availableCount, label = "Available", modifier = Modifier.weight(1f))
                    StatCard(count = onTripCount, label = "On Trip", modifier = Modifier.weight(1f))
                    StatCard(count = offDutyCount, label = "Off Duty", modifier = Modifier.weight(1f))
                }
            }
        }

        // Drivers List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading && state.drivers.isEmpty()) {
                item { Text("Loading drivers...", color = LightTextSecondary) }
            }
            
            items(state.drivers, key = { it.id }) { driver ->
                val assignedVehicle = state.vehicles.find { it.id == driver.vehicleId }
                DriverCard(
                    driver = driver,
                    assignedVehicle = assignedVehicle,
                    onAssignVehicle = { viewModel.onEvent(DriversEvent.OpenAssignForm(driver)) },
                    onDelete = { viewModel.onEvent(DriversEvent.PromptDelete(driver)) }
                )
            }
            
            if (!state.isLoading && state.drivers.isEmpty()) {
                item {
                    Text(
                        text = "No drivers registered. Click Add to register a driver.",
                        color = LightTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (state.isAddFormVisible) {
        RegisterDriverBottomSheet(
            state = state,
            onEvent = viewModel::onEvent
        )
    }

    if (state.isAssignFormVisible) {
        AssignVehicleBottomSheet(
            state = state,
            onEvent = viewModel::onEvent
        )
    }

    if (state.deleteCandidate != null) {
        ConfirmDeleteDriverDialog(
            driver = state.deleteCandidate!!,
            onConfirm = { viewModel.onEvent(DriversEvent.ConfirmDelete) },
            onDismiss = { viewModel.onEvent(DriversEvent.DismissDelete) }
        )
    }
}

@Composable
private fun StatCard(count: Int, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DriverCard(
    driver: Driver,
    assignedVehicle: Vehicle?,
    onAssignVehicle: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main Content Area
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF0F5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = Primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = driver.name.ifBlank { "Unknown Driver" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (statusBg, statusColor) = when (driver.status) {
                                    "On Trip" -> Primary.copy(alpha = 0.12f) to Primary
                                    "Available" -> Success.copy(alpha = 0.12f) to Success
                                    else -> LightTextSecondary.copy(alpha = 0.12f) to LightTextSecondary
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusBg, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = driver.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${driver.rating}", fontSize = 12.sp, color = LightTextSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "${driver.tripsCount} trips", fontSize = 12.sp, color = LightTextSecondary)
                            }
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                        Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LightTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (assignedVehicle != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${assignedVehicle.make} ${assignedVehicle.model} — ${assignedVehicle.plateNumber}",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No vehicle assigned",
                            color = Accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Expanded Area
            if (expanded) {
                HorizontalDivider(color = LightBorder)
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Phone", color = LightTextSecondary, fontSize = 11.sp)
                            Text(driver.phone, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CNIC", color = LightTextSecondary, fontSize = 11.sp)
                            Text(driver.cnic, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("License", color = LightTextSecondary, fontSize = 11.sp)
                            Text(driver.licenseNo, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Joined", color = LightTextSecondary, fontSize = 11.sp)
                            val dateStr = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(driver.joinedDateMillis))
                            Text(dateStr, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
                                .clickable { onAssignVehicle() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Assign Vehicle", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { onDelete() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = Accent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterDriverBottomSheet(
    state: ProviderDriversUiState,
    onEvent: (DriversEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(DriversEvent.CloseAddForm) },
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
                    Text("Register New Driver", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add a driver to your fleet", color = LightTextSecondary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onEvent(DriversEvent.CloseAddForm) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = LightBorder)
            Spacer(modifier = Modifier.height(16.dp))

            DriverField("Full Name *", state.form.name, { onEvent(DriversEvent.NameChanged(it)) }, "Driver's full name", Icons.Outlined.Person)
            Spacer(modifier = Modifier.height(12.dp))
            DriverField("Phone Number *", state.form.phone, { onEvent(DriversEvent.PhoneChanged(it)) }, "03xx-xxxxxxx", null, KeyboardType.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            DriverField("CNIC Number *", state.form.cnic, { onEvent(DriversEvent.CnicChanged(it)) }, "xxxxx-xxxxxxx-x")
            Spacer(modifier = Modifier.height(12.dp))
            DriverField("Driving License Number *", state.form.licenseNo, { onEvent(DriversEvent.LicenseChanged(it)) }, "License number")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEvent(DriversEvent.SaveDriver) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DriverField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignVehicleBottomSheet(
    state: ProviderDriversUiState,
    onEvent: (DriversEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val driver = state.assignFormState.driver

    ModalBottomSheet(
        onDismissRequest = { onEvent(DriversEvent.CloseAssignForm) },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Allow taking up to 80% height
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Assign Vehicle", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Select a vehicle for ${driver?.name ?: "this driver"}", color = LightTextSecondary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onEvent(DriversEvent.CloseAssignForm) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = LightBorder)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Option for Unassign
                item {
                    val isSelected = state.assignFormState.selectedVehicleId == null
                    VehicleSelectionCard(
                        title = "No Vehicle",
                        subtitle = "Unassign current vehicle",
                        tag = null,
                        tagColor = Color.Transparent,
                        tagBgColor = Color.Transparent,
                        isSelected = isSelected,
                        onClick = { onEvent(DriversEvent.SelectVehicle("")) }
                    )
                }

                items(state.vehicles, key = { it.id }) { vehicle ->
                    val isSelected = state.assignFormState.selectedVehicleId == vehicle.id
                    // Check if assigned to another driver
                    val assignedDriver = state.drivers.find { it.vehicleId == vehicle.id && it.id != driver?.id }
                    
                    val (tagBg, tagColor) = when(vehicle.type) {
                        "MoveMax" -> Secondary.copy(alpha = 0.14f) to Success
                        "MoveBig" -> Accent.copy(alpha = 0.14f) to Accent
                        else -> Primary.copy(alpha = 0.12f) to Primary
                    }

                    VehicleSelectionCard(
                        title = "${vehicle.make} ${vehicle.model} — ${vehicle.plateNumber}",
                        subtitle = assignedDriver?.let { "Currently: ${it.name}" },
                        tag = vehicle.type,
                        tagColor = tagColor,
                        tagBgColor = tagBg,
                        isSelected = isSelected,
                        onClick = { onEvent(DriversEvent.SelectVehicle(vehicle.id)) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onEvent(DriversEvent.ConfirmAssignment) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm Assignment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VehicleSelectionCard(
    title: String,
    subtitle: String?,
    tag: String?,
    tagColor: Color,
    tagBgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Primary else LightBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) Primary.copy(alpha = 0.05f) else Color.White, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = LightTextSecondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tag != null) {
                        Box(
                            modifier = Modifier
                                .background(tagBgColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = tag, color = tagColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (subtitle != null) {
                        Text(text = subtitle, color = LightTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDriverDialog(
    driver: Driver,
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
                Text(text = "Delete Driver", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            }
        },
        text = { 
            Text(
                text = "This will permanently remove ${driver.name} from your fleet. This can't be undone.",
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
                Text(text = "Delete Driver")
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
