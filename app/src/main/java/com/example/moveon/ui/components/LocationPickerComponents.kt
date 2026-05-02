package com.example.moveon.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.util.LocationUtils
import com.example.moveon.util.isLocationPermissionGranted
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced address input field with "Pin on Map" and "Use Current Location" buttons.
 * Text input triggers geocoding after a debounce delay.
 *
 * @param address Current address text
 * @param onAddressChanged Called when text changes
 * @param lat Current latitude (null if not resolved)
 * @param lng Current longitude (null if not resolved)
 * @param onLocationResolved Called with (lat, lng, resolvedAddress) when location is resolved
 * @param placeholder Placeholder text for the field
 * @param label Label text above the field
 * @param onRequestLocationPermission Lambda to request location permission
 * @param isLocationPermissionGranted Whether location permission is granted
 */
@Composable
fun LocationPickerField(
    address: String,
    onAddressChanged: (String) -> Unit,
    lat: Double?,
    lng: Double?,
    onLocationResolved: (Double, Double, String) -> Unit,
    placeholder: String = "Enter address",
    label: String = "Address",
    onRequestLocationPermission: () -> Unit = {},
    isLocationPermissionGranted: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeocoding by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }
    var suggestions by remember { mutableStateOf<List<com.example.moveon.util.LocationUtils.PlaceSuggestion>>(emptyList()) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
            value = address,
            onValueChange = { newValue ->
                onAddressChanged(newValue)
                // Debounced geocoding on text input
                        geocodeJob?.cancel()
                        suggestions = emptyList()
                        geocodeJob = scope.launch {
                            delay(600) // shorter debounce for suggestions
                            if (newValue.length >= 3) {
                                isGeocoding = true
                                val preds = LocationUtils.fetchPlaceSuggestions(context, newValue)
                                isGeocoding = false
                                if (preds.isNotEmpty()) {
                                    suggestions = preds
                                } else {
                                    // fallback to Geocoder for best-effort single result
                                    isGeocoding = true
                                    val result = LocationUtils.geocodeAddress(context, newValue)
                                    isGeocoding = false
                                    if (result != null) {
                                        onLocationResolved(result.latitude, result.longitude, newValue)
                                    }
                                }
                            }
                        }
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = if (lat != null) Primary else LightTextSecondary
                )
            },
            trailingIcon = {
                if (isGeocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Primary
                    )
                }
            },
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LightSurfaceVariant,
                unfocusedContainerColor = LightSurfaceVariant,
                disabledContainerColor = LightSurfaceVariant,
                focusedIndicatorColor = if (lat != null) Primary else LightBorder,
                unfocusedIndicatorColor = LightBorder,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedPlaceholderColor = Color.Black.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.6f)
            )
        )

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pin on Map button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showMapPicker = true },
                shape = RoundedCornerShape(10.dp),
                color = Primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.EditLocationAlt,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Pin on Map",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }

            // Use Current Location button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (isLocationPermissionGranted) {
                            isFetchingLocation = true
                            scope.launch {
                                val fusedClient =
                                    LocationServices.getFusedLocationProviderClient(context)
                                val currentLoc = LocationUtils.getCurrentLocation(fusedClient)
                                if (currentLoc != null) {
                                    val resolvedAddress = LocationUtils.reverseGeocode(
                                        context,
                                        currentLoc.latitude,
                                        currentLoc.longitude
                                    ) ?: "Lat: ${
                                        String.format(
                                            "%.4f",
                                            currentLoc.latitude
                                        )
                                    }, Lng: ${String.format("%.4f", currentLoc.longitude)}"
                                    onAddressChanged(resolvedAddress)
                                    onLocationResolved(
                                        currentLoc.latitude,
                                        currentLoc.longitude,
                                        resolvedAddress
                                    )
                                }
                                isFetchingLocation = false
                            }
                        } else {
                            onRequestLocationPermission()
                        }
                    },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2E7D32).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Current Location",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        // Resolved location indicator
        AnimatedVisibility(
            visible = lat != null && lng != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Location resolved (${String.format("%.4f", lat ?: 0.0)}, ${String.format("%.4f", lng ?: 0.0)})",
                    fontSize = 10.sp,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }

    // Suggestions dropdown
    AnimatedVisibility(visible = suggestions.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                suggestions.forEach { sug ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // resolve selected suggestion
                                scope.launch {
                                    val res = LocationUtils.fetchPlaceLatLng(context, sug.placeId)
                                    if (res != null) {
                                        val (latLng, addressText) = res
                                        val resolved = addressText ?: sug.fullText
                                        onAddressChanged(resolved)
                                        onLocationResolved(latLng.latitude, latLng.longitude, resolved)
                                    } else {
                                        // fallback: set text to suggestion full text
                                        onAddressChanged(sug.fullText)
                                    }
                                    suggestions = emptyList()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(sug.primaryText, fontWeight = FontWeight.SemiBold)
                            Text(sug.fullText, fontSize = 12.sp, color = LightTextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Map Picker Dialog
    if (showMapPicker) {
        MapPickerDialog(
            initialLat = lat ?: 31.5204,
            initialLng = lng ?: 74.3587,
            onLocationPicked = { pickedLat, pickedLng, resolvedAddress ->
                onAddressChanged(resolvedAddress)
                onLocationResolved(pickedLat, pickedLng, resolvedAddress)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}

/**
 * Full-screen dialog with a Google Map for picking a location.
 * User taps on the map to place/move a pin, then confirms.
 * The pin's location is reverse-geocoded to a human-readable address.
 */
@Composable
fun MapPickerDialog(
    initialLat: Double = 31.5204,
    initialLng: Double = 74.3587,
    onLocationPicked: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedLocation by remember { mutableStateOf(LatLng(initialLat, initialLng)) }
    var resolvedAddress by remember { mutableStateOf("Tap on the map to pick a location") }
    var isResolving by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 14f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LightSurface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Pick Location",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Map
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(mapType = MapType.NORMAL),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            compassEnabled = true,
                            myLocationButtonEnabled = isLocationPermissionGranted(context)
                        ),
                        onMapClick = { latLng ->
                            pickedLocation = latLng
                            isResolving = true
                            scope.launch {
                                val address = LocationUtils.reverseGeocode(
                                    context,
                                    latLng.latitude,
                                    latLng.longitude
                                )
                                resolvedAddress = address ?: "Lat: ${String.format("%.4f", latLng.latitude)}, Lng: ${String.format("%.4f", latLng.longitude)}"
                                isResolving = false
                            }
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = pickedLocation),
                            title = "Selected Location"
                        )
                    }

                    // Crosshair hint
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Tap anywhere on the map to select",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                // Bottom section with resolved address and confirm button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Place,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (isResolving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Resolving address...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LightTextSecondary
                                )
                            } else {
                                Text(
                                    text = resolvedAddress,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LightTextPrimary,
                                    maxLines = 2
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onLocationPicked(
                                    pickedLocation.latitude,
                                    pickedLocation.longitude,
                                    resolvedAddress
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isResolving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = Primary.copy(alpha = 0.45f)
                            )
                        ) {
                            Text("Confirm Location")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Small map preview card showing a single marker at the given coordinates.
 * Non-interactive (read-only).
 */
@Composable
fun MapPreviewCard(
    lat: Double,
    lng: Double,
    modifier: Modifier = Modifier,
    title: String = "Location"
) {
    val position = remember(lat, lng) { LatLng(lat, lng) }
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    LaunchedEffect(lat, lng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = position),
                title = title
            )
        }
    }
}

/**
 * Dynamic map preview showing both pickup and dropoff markers.
 */
@Composable
fun DualMarkerMapPreview(
    pickupLat: Double?,
    pickupLng: Double?,
    dropOffLat: Double?,
    dropOffLng: Double?,
    modifier: Modifier = Modifier
) {
    val defaultCenter = LatLng(31.5204, 74.3587)
    val center = when {
        pickupLat != null && pickupLng != null && dropOffLat != null && dropOffLng != null -> {
            LatLng((pickupLat + dropOffLat) / 2, (pickupLng + dropOffLng) / 2)
        }
        pickupLat != null && pickupLng != null -> LatLng(pickupLat, pickupLng)
        dropOffLat != null && dropOffLng != null -> LatLng(dropOffLat, dropOffLng)
        else -> defaultCenter
    }

    val hasAnyMarker = (pickupLat != null && pickupLng != null) || (dropOffLat != null && dropOffLng != null)
    val zoom = if (pickupLat != null && dropOffLat != null) 11f else if (hasAnyMarker) 14f else 12f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    LaunchedEffect(pickupLat, pickupLng, dropOffLat, dropOffLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                mapToolbarEnabled = false
            )
        ) {
            if (pickupLat != null && pickupLng != null) {
                Marker(
                    state = MarkerState(position = LatLng(pickupLat, pickupLng)),
                    title = "Pickup"
                )
            }
            if (dropOffLat != null && dropOffLng != null) {
                Marker(
                    state = MarkerState(position = LatLng(dropOffLat, dropOffLng)),
                    title = "Drop-off"
                )
            }
        }
    }
}

/**
 * Live tracking map that shows pickup, dropoff and a moving vehicle marker.
 */
@Composable
fun LiveTrackingMap(
    pickupLat: Double,
    pickupLng: Double,
    dropOffLat: Double,
    dropOffLng: Double,
    vehicleLat: Double?,
    vehicleLng: Double?,
    routeToPickupPoints: List<LatLng>? = null,
    routeToDropoffPoints: List<LatLng>? = null,
    modifier: Modifier = Modifier
) {
    val vehiclePosition = if (vehicleLat != null && vehicleLng != null) {
        LatLng(vehicleLat, vehicleLng)
    } else null

    val center = vehiclePosition ?: LatLng((pickupLat + dropOffLat) / 2, (pickupLng + dropOffLng) / 2)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 13f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    rotationGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    mapToolbarEnabled = false
                )
            ) {
                // Pickup marker
                Marker(
                    state = MarkerState(position = LatLng(pickupLat, pickupLng)),
                    title = "Pickup"
                )

                // Dropoff marker
                Marker(
                    state = MarkerState(position = LatLng(dropOffLat, dropOffLng)),
                    title = "Drop-off"
                )

                val mapRouteToPickup = when {
                    routeToPickupPoints != null && routeToPickupPoints.size >= 2 -> routeToPickupPoints
                    else -> null
                }

                val mapRouteToDropoff = when {
                    routeToDropoffPoints != null && routeToDropoffPoints.size >= 2 -> routeToDropoffPoints
                    else -> listOf(LatLng(pickupLat, pickupLng), LatLng(dropOffLat, dropOffLng))
                }

                // Draw provider/vehicle -> pickup route (if available) as a subtle color
                if (mapRouteToPickup != null) {
                    Polyline(
                        points = mapRouteToPickup,
                        color = Color(0xFF9FC4E9),
                        width = 6f
                    )
                }

                // Draw pickup -> dropoff route (primary route)
                Polyline(
                    points = mapRouteToDropoff,
                    color = Primary,
                    width = 6f
                )

                // Vehicle marker (moving)
                if (vehiclePosition != null) {
                    Marker(
                        state = MarkerState(position = vehiclePosition),
                        title = "Vehicle"
                    )
                }
            }

            // Live indicator badge
            if (vehiclePosition != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, CircleShape)
                        )
                        Text(
                            "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Pickup label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Pickup",
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary
                )
            }

            // Drop-off label
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Drop-off",
                    style = MaterialTheme.typography.labelLarge,
                    color = LightTextPrimary
                )
            }
        }
    }
}
