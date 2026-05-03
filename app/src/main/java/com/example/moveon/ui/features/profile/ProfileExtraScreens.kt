package com.example.moveon.ui.features.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.window.Dialog
import com.example.moveon.domain.model.Booking
import com.example.moveon.domain.model.BookingStatus
import com.example.moveon.domain.model.SavedAddress
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.LogisticsRepository
import com.example.moveon.domain.repository.SavedAddressRepository
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MapPickerDialog
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.components.ProviderBottomBar
import com.example.moveon.ui.components.ProviderDashboardTab
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val currentUser = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    fun updateProfile(firstName: String, lastName: String, email: String, phoneNumber: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.updateUserProfile(firstName, lastName, email, phoneNumber)
                .onSuccess {
                    _eventFlow.emit("Profile updated")
                    onSuccess()
                }
                .onFailure { e ->
                    _eventFlow.emit(e.message ?: "Failed to update profile")
                }
        }
    }
}

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser.collectAsState().value

    val initialFirst = user?.firstName.orEmpty()
    val initialLast = user?.lastName.orEmpty()
    val initialPhone = user?.phoneNumber.orEmpty()
    val initialEmail = user?.email.orEmpty()

    var fullName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    // Prefill when user arrives/changes, without fighting the user's edits.
    LaunchedEffect(user?.id) {
        fullName = listOf(initialFirst, initialLast).joinToString(" ").trim()
        phoneNumber = initialPhone
        email = initialEmail
    }

    val (firstName, lastName) = remember(fullName) { splitName(fullName) }
    val isDirty = remember(fullName, email, phoneNumber, initialFirst, initialLast, initialEmail, initialPhone) {
        firstName != initialFirst ||
            lastName != initialLast ||
            email.trim() != initialEmail ||
            normalizePhone(phoneNumber) != normalizePhone(initialPhone)
    }

    val emailError = remember(email) {
        if (email.isBlank()) "Email is required"
        else if (!isValidEmail(email)) "Enter a valid email"
        else null
    }

    val phoneError = remember(phoneNumber) {
        if (phoneNumber.isBlank()) "Phone number is required"
        else if (!isValidPhone(phoneNumber)) "Enter a valid phone number"
        else null
    }

    val nameError = remember(fullName) {
        if (fullName.trim().isBlank()) "Full name is required" else null
    }

    val canSubmit = isDirty && emailError == null && phoneError == null && nameError == null

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            if (isProviderMode) {
                ProviderBottomBar(selectedTab = ProviderDashboardTab.Profile, onTabSelected = onProviderTabSelected)
            } else {
                MoveOnBottomBar(selectedTab = DashboardTab.Profile, onTabSelected = onTabSelected)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileTopHeader(
                title = "Edit Profile",
                subtitle = "Update your personal information",
                onBack = onBack
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    leadingIcon = Icons.Outlined.Edit,
                    error = nameError
                )
                ProfileField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = Icons.Outlined.Email,
                    error = emailError
                )
                ProfileField(
                    label = "Phone Number",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    leadingIcon = Icons.Outlined.Work,
                    error = phoneError
                )
            }

            MoveOnPillButton(
                text = "Update Information",
                onClick = {
                    viewModel.updateProfile(firstName, lastName, email.trim(), normalizePhone(phoneNumber)) {
                        // keep screen open; profile will refresh through listeners
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                background = Primary,
                textColor = Color.White,
                enabled = canSubmit
            )
        }
    }
}

@Composable
fun SavedAddressesScreen(
    onBack: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    viewModel: SavedAddressesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SavedAddress?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            if (isProviderMode) {
                ProviderBottomBar(selectedTab = ProviderDashboardTab.Profile, onTabSelected = onProviderTabSelected)
            } else {
                MoveOnBottomBar(selectedTab = DashboardTab.Profile, onTabSelected = onTabSelected)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileTopHeader(
                title = "Saved Addresses",
                subtitle = "Manage your frequent locations",
                onBack = onBack
            )

            state.items.forEach { item ->
                SavedAddressCard(
                    item = item,
                    onSetDefault = { viewModel.setDefault(item.id) },
                    onEdit = {
                        editing = item
                        showEditor = true
                    },
                    onDelete = { viewModel.delete(item.id) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    editing = null
                    showEditor = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Add Address", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showEditor) {
        AddEditAddressDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { draft ->
                if (draft.id.isBlank()) viewModel.add(draft) else viewModel.update(draft)
                showEditor = false
            }
        )
    }
}

@HiltViewModel
class MoveHistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {
    private val _state = mutableStateOf(MoveHistoryState(isLoading = true))
    val state = _state

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user == null) {
                    _state.value = MoveHistoryState(isLoading = false, errorMessage = "Unable to load move history.")
                    return@collect
                }
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                logisticsRepository.getBookingsForUser(user.id)
                    .onSuccess { bookings ->
                        _state.value = MoveHistoryState(isLoading = false, items = bookings.sortedByDescending { it.createdAt })
                    }
                    .onFailure { e ->
                        _state.value = MoveHistoryState(isLoading = false, errorMessage = e.message ?: "Unable to load move history.")
                    }
            }
        }
    }
}

data class MoveHistoryState(
    val isLoading: Boolean = false,
    val items: List<Booking> = emptyList(),
    val errorMessage: String? = null
)

@Composable
fun MoveHistoryScreen(
    onClose: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    isProviderMode: Boolean = false,
    onProviderTabSelected: (ProviderDashboardTab) -> Unit = {},
    onViewDetails: (String) -> Unit = {},
    viewModel: MoveHistoryViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Move History",
                        style = MaterialTheme.typography.headlineSmall,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "View all your past moves",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LightTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close move history", tint = LightTextSecondary)
                }
            }
        },
        bottomBar = {
            if (isProviderMode) {
                ProviderBottomBar(selectedTab = ProviderDashboardTab.Profile, onTabSelected = onProviderTabSelected)
            } else {
                MoveOnBottomBar(selectedTab = DashboardTab.Profile, onTabSelected = onTabSelected)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.errorMessage?.let {
                Text(it, color = ErrorDeep)
            }

            if (state.items.isEmpty() && state.errorMessage == null && !state.isLoading) {
                Text("No moves yet.", color = LightTextSecondary)
            }

            state.items.forEach { booking ->
                MoveHistoryCard(
                    booking = booking,
                    onReceipt = {},
                    onViewDetails = { onViewDetails(booking.id) }
                )
            }
        }
    }
}

@Composable
private fun ProfileTopHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = LightTextPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    error: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            // Don't hard-lock height: supporting/error text needs room, and fixed height can clip text.
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(18.dp)) },
            isError = error != null,
            supportingText = if (error == null) null else {
                { Text(error, color = ErrorDeep) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = ErrorDeep,
                focusedTextColor = LightTextPrimary,
                unfocusedTextColor = LightTextPrimary,
                disabledTextColor = LightTextPrimary.copy(alpha = 0.6f),
                errorTextColor = LightTextPrimary,
                focusedLeadingIconColor = LightTextSecondary,
                unfocusedLeadingIconColor = LightTextSecondary,
                disabledLeadingIconColor = LightTextSecondary.copy(alpha = 0.6f),
                errorLeadingIconColor = LightTextSecondary,
                focusedPlaceholderColor = LightTextSecondary,
                unfocusedPlaceholderColor = LightTextSecondary,
                disabledPlaceholderColor = LightTextSecondary.copy(alpha = 0.6f),
                errorPlaceholderColor = LightTextSecondary,
                errorSupportingTextColor = ErrorDeep
            )
        )
    }
}

@Composable
private fun SavedAddressCard(
    item: SavedAddress,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.9.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.label.lowercase()) {
                        "work" -> Icons.Outlined.Work
                        "home" -> Icons.Outlined.LocationOn
                        else -> Icons.Outlined.LocationOn
                    },
                    contentDescription = null,
                    tint = LightTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.label, style = MaterialTheme.typography.titleMedium, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
                Text(item.addressLine1, style = MaterialTheme.typography.bodySmall, color = LightTextPrimary)
                if (item.addressLine2.isNotBlank()) {
                    Text(item.addressLine2, style = MaterialTheme.typography.bodySmall, color = LightTextPrimary)
                }
                Text(item.city, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.isDefault) {
                        SmallPillButton(text = "Default", onClick = {}, enabled = false)
                    } else {
                        SmallPillButton(text = "Set Default", onClick = onSetDefault)
                    }
                    SmallPillButton(text = "Edit", onClick = onEdit)
                    SmallPillButton(text = "Delete", onClick = onDelete, textColor = ErrorDeep)
                }
            }
        }
    }
}

@Composable
private fun SmallPillButton(
    text: String,
    onClick: () -> Unit,
    textColor: Color = LightTextPrimary,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .background(Color(0xFFFAFAFA), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) textColor else LightTextSecondary
        )
    }
}

@HiltViewModel
class SavedAddressesViewModel @Inject constructor(
    private val repo: SavedAddressRepository
) : ViewModel() {
    private val _state = mutableStateOf(SavedAddressesState(isLoading = true))
    val state = _state

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.observeSavedAddresses()
                .catch { emit(emptyList()) }
                .collect { items ->
                    _state.value = SavedAddressesState(isLoading = false, items = items)
                }
        }
    }

    fun add(draft: SavedAddress) {
        viewModelScope.launch {
            repo.addSavedAddress(draft).onFailure { e ->
                _eventFlow.emit(e.message ?: "Failed to add address")
            }
        }
    }

    fun update(draft: SavedAddress) {
        viewModelScope.launch {
            repo.updateSavedAddress(draft).onFailure { e ->
                _eventFlow.emit(e.message ?: "Failed to update address")
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repo.deleteSavedAddress(id).onFailure { e ->
                _eventFlow.emit(e.message ?: "Failed to delete address")
            }
        }
    }

    fun setDefault(id: String) {
        viewModelScope.launch {
            repo.setDefaultAddress(id).onFailure { e ->
                _eventFlow.emit(e.message ?: "Failed to set default")
            }
        }
    }
}

data class SavedAddressesState(
    val isLoading: Boolean = false,
    val items: List<SavedAddress> = emptyList()
)

@Composable
private fun AddEditAddressDialog(
    initial: SavedAddress?,
    onDismiss: () -> Unit,
    onSave: (SavedAddress) -> Unit
) {
    var label by rememberSaveable(initial?.label) { mutableStateOf(initial?.label.orEmpty()) }
    var line1 by rememberSaveable(initial?.addressLine1) { mutableStateOf(initial?.addressLine1.orEmpty()) }
    var line2 by rememberSaveable(initial?.addressLine2) { mutableStateOf(initial?.addressLine2.orEmpty()) }
    var city by rememberSaveable(initial?.city) { mutableStateOf(initial?.city.orEmpty()) }
    var lat by rememberSaveable(initial?.lat) { mutableStateOf(initial?.lat) }
    var lng by rememberSaveable(initial?.lng) { mutableStateOf(initial?.lng) }
    var showMap by rememberSaveable { mutableStateOf(false) }

    val labelError = if (label.isBlank()) "Label is required" else null
    val line1Error = if (line1.isBlank()) "Address line 1 is required" else null
    val cityError = if (city.isBlank()) "City is required" else null
    val pinError = if (lat == null || lng == null) "Pin location is required" else null
    val canSave = labelError == null && line1Error == null && cityError == null && pinError == null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = if (initial == null) "Add Address" else "Edit Address",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )

                ProfileField(label = "Label", value = label, onValueChange = { label = it }, leadingIcon = Icons.Outlined.Edit, error = labelError)
                ProfileField(label = "Address Line 1", value = line1, onValueChange = { line1 = it }, leadingIcon = Icons.Outlined.LocationOn, error = line1Error)
                ProfileField(label = "Address Line 2", value = line2, onValueChange = { line2 = it }, leadingIcon = Icons.Outlined.LocationOn, error = null)
                ProfileField(label = "City", value = city, onValueChange = { city = it }, leadingIcon = Icons.Outlined.LocationOn, error = cityError)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pin Location *", style = MaterialTheme.typography.labelLarge, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showMap = true }) {
                        Text(
                            text = if (lat == null || lng == null) "Select on map" else "Change pin (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})",
                            color = Primary
                        )
                    }
                    pinError?.let { Text(it, color = ErrorDeep, style = MaterialTheme.typography.bodySmall) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightSurfaceVariant)
                    ) {
                        Text("Cancel", color = LightTextPrimary)
                    }
                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            onSave(
                                SavedAddress(
                                    id = initial?.id.orEmpty(),
                                    label = label.trim(),
                                    addressLine1 = line1.trim(),
                                    addressLine2 = line2.trim(),
                                    city = city.trim(),
                                    lat = lat ?: 0.0,
                                    lng = lng ?: 0.0,
                                    isDefault = initial?.isDefault ?: false,
                                    updatedAt = now,
                                    createdAt = initial?.createdAt ?: 0L
                                )
                            )
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showMap) {
        MapPickerDialog(
            initialLat = lat ?: 31.5204,
            initialLng = lng ?: 74.3587,
            onLocationPicked = { pickedLat, pickedLng, _ ->
                // IMPORTANT: pin selection does NOT populate address lines
                lat = pickedLat
                lng = pickedLng
                showMap = false
            },
            onDismiss = { showMap = false }
        )
    }
}

@Composable
private fun MoveHistoryCard(
    booking: Booking,
    onReceipt: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.12.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = booking.id,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        BookingStatusBadge(booking.status)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(14.dp))
                        Text(formatShortDate(booking.scheduledTime), style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                        Text("•", style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                        Icon(Icons.Outlined.Timer, contentDescription = null, tint = LightTextSecondary, modifier = Modifier.size(14.dp))
                        Text(estimateDurationLabel(booking), style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (booking.rating > 0f) {
                        Icon(Icons.Outlined.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                        Text(String.format(Locale.getDefault(), "%.1f", booking.rating), color = LightTextPrimary)
                    }
                }
            }

            AddressRow(title = "Pickup", address = booking.pickupAddress, filledDot = true)
            AddressRow(title = "Drop-off", address = booking.dropOffAddress, filledDot = false)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinePillAction(
                    modifier = Modifier.weight(1f),
                    text = "Receipt",
                    onClick = onReceipt
                )
                OutlinePillAction(
                    modifier = Modifier.weight(1f),
                    text = "View Details",
                    onClick = onViewDetails,
                    showChevron = true
                )
            }
        }
    }
}

@Composable
private fun BookingStatusBadge(status: BookingStatus) {
    val (bg, fg, label) = when (status) {
        BookingStatus.COMPLETED -> Triple(Color(0xFF2E7D32), Color.White, "Completed")
        BookingStatus.CONFIRMED -> Triple(Primary, Color.White, "Confirmed")
        BookingStatus.SEARCHING -> Triple(Color(0xFFFFA000), Color.White, "Searching")
        BookingStatus.ACTIVE -> Triple(Primary, Color.White, "Active")
        else -> Triple(Color(0xFF757575), Color.White, status.name.lowercase().replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AddressRow(title: String, address: String, filledDot: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        val dotColor = if (filledDot) Primary else Color.Transparent
        val border = if (filledDot) null else BorderStroke(1.12.dp, Primary)

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(dotColor, CircleShape)
                .let { m -> if (border == null) m else m.background(Color.White, CircleShape) }
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = LightTextSecondary)
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OutlinePillAction(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    showChevron: Boolean = false
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .background(Color(0xFFFAFAFA), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
            if (showChevron) {
                Text("›", color = LightTextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatShortDate(timestampMillis: Long): String {
    return try {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestampMillis))
    } catch (_: Throwable) {
        ""
    }
}

private fun estimateDurationLabel(booking: Booking): String {
    val delta = (booking.scheduledTime - booking.createdAt).coerceAtLeast(0)
    val mins = (delta / 60000L).toInt()
    if (mins <= 0) return "--"
    val h = mins / 60
    val m = mins % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun splitName(fullName: String): Pair<String, String> {
    val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "" to ""
    if (parts.size == 1) return parts.first() to ""
    return parts.dropLast(1).joinToString(" ") to parts.last()
}

private fun initialsOf(firstName: String, lastName: String): String {
    val first = firstName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val second = lastName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    return (first + second).ifBlank { "MO" }
}

private fun isValidEmail(email: String): Boolean {
    val value = email.trim()
    return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(value)
}

private fun normalizePhone(input: String): String {
    return input.trim().replace(Regex("\\s+"), " ")
}

private fun isValidPhone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    // allow country code; keep loose but safe for UI
    return digits.length in 10..15
}

