package com.example.moveon.ui.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moveon.ui.components.LocationPickerField
import com.example.moveon.ui.components.MapPreviewCard
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.util.LocationPermissionHandler

@Composable
fun ProviderSetupStepOneScreen(
    flowViewModel: AuthFlowViewModel,
    onNext: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBackground) {
        LocationPermissionHandler { requestPermission, isPermissionGranted ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ProviderStepHeader(title = "Provider Setup", subtitle = "Step 1 of 3", step = 1)

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Professional Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Tell us about your transport business", color = LightTextSecondary)

                    ProviderSectionCard {
                        ProviderField(
                            label = "Business/Establishment Name *",
                            value = flowViewModel.businessName,
                            onValueChange = { flowViewModel.businessName = it },
                            placeholder = "e.g., Ahmed Transport Services",
                            leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) }
                        )
                        ProviderField(
                            label = "Email",
                            value = flowViewModel.email,
                            onValueChange = { flowViewModel.email = it },
                            placeholder = "you@example.com",
                            keyboardType = KeyboardType.Email,
                            leadingIcon = { Icon(Icons.Outlined.MailOutline, contentDescription = null) }
                        )
                        ProviderField(
                            label = "Phone *",
                            value = flowViewModel.phoneNumber,
                            onValueChange = { flowViewModel.phoneNumber = it },
                            placeholder = "0300 1234 567",
                            keyboardType = KeyboardType.Phone,
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) }
                        )
                    }

                    ProviderSectionCard {
                        LocationPickerField(
                            address = flowViewModel.businessAddress,
                            onAddressChanged = { flowViewModel.businessAddress = it },
                            lat = flowViewModel.businessLat,
                            lng = flowViewModel.businessLng,
                            onLocationResolved = { lat, lng, address ->
                                flowViewModel.businessLat = lat
                                flowViewModel.businessLng = lng
                                flowViewModel.businessAddress = address
                            },
                            placeholder = "Street address, building number",
                            label = "Business Address *",
                            onRequestLocationPermission = requestPermission,
                            isLocationPermissionGranted = isPermissionGranted
                        )

                        // Show map preview when location is resolved
                        if (flowViewModel.businessLat != null && flowViewModel.businessLng != null) {
                            MapPreviewCard(
                                lat = flowViewModel.businessLat!!,
                                lng = flowViewModel.businessLng!!,
                                title = flowViewModel.businessName.ifBlank { "Business Location" }
                            )
                        }

                        ProviderField(
                            label = "City *",
                            value = flowViewModel.city,
                            onValueChange = { flowViewModel.city = it },
                            placeholder = "Lahore",
                            leadingIcon = { Icon(Icons.Outlined.Pin, contentDescription = null) }
                        )
                    }

                    ProviderSectionCard {
                        ProviderField(
                            label = "Business Description",
                            value = flowViewModel.businessDescription,
                            onValueChange = { flowViewModel.businessDescription = it },
                            placeholder = "Describe your services, specialities, and what makes your business unique...",
                            leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                            singleLine = false,
                            minLines = 3
                        )
                        ProviderField(
                            label = "Years of Experience",
                            value = flowViewModel.yearsOfExperience,
                            onValueChange = { flowViewModel.yearsOfExperience = it },
                            placeholder = "5",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        enabled = flowViewModel.businessName.isNotBlank() && flowViewModel.phoneNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Primary.copy(alpha = 0.45f)
                        )
                    ) {
                        Text("Next >")
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderSetupStepTwoScreen(
    flowViewModel: AuthFlowViewModel,
    onNext: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProviderStepHeader(title = "Vehicle Registration", subtitle = "Step 2 of 3", step = 2)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Vehicle Information", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Add your vehicle details.", color = LightTextSecondary)

                ProviderSectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderField(
                            label = "Make *",
                            value = flowViewModel.vehicleMake,
                            onValueChange = { flowViewModel.vehicleMake = it },
                            placeholder = "e.g., Toyota",
                            modifier = Modifier.weight(1f)
                        )
                        ProviderField(
                            label = "Model *",
                            value = flowViewModel.vehicleModel,
                            onValueChange = { flowViewModel.vehicleModel = it },
                            placeholder = "e.g., Hiace",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderField(
                            label = "Year *",
                            value = flowViewModel.vehicleYear,
                            onValueChange = { flowViewModel.vehicleYear = it },
                            placeholder = "2020",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        ProviderField(
                            label = "Color",
                            value = flowViewModel.vehicleColor,
                            onValueChange = { flowViewModel.vehicleColor = it },
                            placeholder = "White",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ProviderField(
                        label = "Plate Number *",
                        value = flowViewModel.plateNumber,
                        onValueChange = { flowViewModel.plateNumber = it },
                        placeholder = "ABC-1234",
                        leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) }
                    )
                }

                ProviderSectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderField(
                            label = "Max Capacity (kg) *",
                            value = flowViewModel.maxCapacityKg,
                            onValueChange = { flowViewModel.maxCapacityKg = it },
                            placeholder = "1500",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Outlined.LocalShipping, contentDescription = null) }
                        )
                        ProviderField(
                            label = "Max Volume (m3) *",
                            value = flowViewModel.maxVolumeM3,
                            onValueChange = { flowViewModel.maxVolumeM3 = it },
                            placeholder = "12",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Outlined.Speed, contentDescription = null) }
                        )
                    }
                }

                ProviderSectionCard {
                    Text("Pricing", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderField(
                            label = "Base Rate *",
                            value = flowViewModel.baseRate,
                            onValueChange = { flowViewModel.baseRate = it },
                            placeholder = "PKR 2000",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Outlined.Payments, contentDescription = null) }
                        )
                        ProviderField(
                            label = "Rate per KM *",
                            value = flowViewModel.ratePerKm,
                            onValueChange = { flowViewModel.ratePerKm = it },
                            placeholder = "PKR 500",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Outlined.DirectionsCar, contentDescription = null) }
                        )
                    }
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    enabled = flowViewModel.vehicleMake.isNotBlank() && flowViewModel.vehicleModel.isNotBlank() && flowViewModel.plateNumber.isNotBlank() && flowViewModel.baseRate.isNotBlank() && flowViewModel.ratePerKm.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.45f)
                    )
                ) {
                    Text("Next >")
                }
            }
        }
    }
}

@Composable
fun ProviderSetupStepThreeScreen(
    flowViewModel: AuthFlowViewModel,
    isLoading: Boolean,
    onRegister: () -> Unit
) {
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            flowViewModel.cnicUploaded = true
            flowViewModel.cnicUploadLabel = "Photo captured"
        }
    }

    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            flowViewModel.cnicUploaded = true
            flowViewModel.cnicUploadLabel = "File uploaded"
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = LightBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProviderStepHeader(title = "Document Verification", subtitle = "Step 3 of 3", step = 3)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Verify Your Credentials", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Upload required documents to complete your profile", color = LightTextSecondary)

                ProviderSectionCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(LightSurfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Badge, contentDescription = null, tint = LightTextSecondary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CNIC *", fontWeight = FontWeight.SemiBold)
                            Text("Front and back of your CNIC", color = LightTextSecondary, fontSize = 12.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, LightBorder)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                            Text(" Take Photo")
                        }
                        Button(
                            onClick = { uploadLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Upload File")
                        }
                    }

                    if (flowViewModel.cnicUploaded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = flowViewModel.cnicUploadLabel,
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0xFFFFF9ED),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFF3D08B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = Color(0xFFC88700))
                            Text(" Document Guidelines", fontWeight = FontWeight.SemiBold)
                        }
                        Text("- Ensure all text is clearly visible and readable", fontSize = 12.sp, color = LightTextSecondary)
                        Text("- Photos should be taken in a well-lit room", fontSize = 12.sp, color = LightTextSecondary)
                        Text("- Documents must be valid and not expired", fontSize = 12.sp, color = LightTextSecondary)
                        Text("- Accepted formats: JPG, PNG, PDF", fontSize = 12.sp, color = LightTextSecondary)
                    }
                }

                Button(
                    onClick = onRegister,
                    enabled = flowViewModel.cnicUploaded && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.45f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Register >")
                    }
                }

                TextButton(onClick = {
                    flowViewModel.cnicUploaded = false
                    flowViewModel.cnicUploadLabel = ""
                }) {
                    Text("Reset uploads", color = LightTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ProviderStepHeader(
    title: String,
    subtitle: String,
    step: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = Color.White)
            }
            Column {
                Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color.White.copy(alpha = 0.88f), fontSize = 12.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (index + 1 <= step) Color.White else Color.White.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun ProviderSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun ProviderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = leadingIcon,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightSurfaceVariant,
                unfocusedContainerColor = LightSurfaceVariant,
                disabledContainerColor = LightSurfaceVariant,
                focusedIndicatorColor = LightBorder,
                unfocusedIndicatorColor = LightBorder,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black.copy(alpha = 0.5f),
                focusedPlaceholderColor = Color.Black.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.6f)
            )
        )
    }
}

