package com.example.moveon.ui.features.settings

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.data.local.dao.UserPreferences
import com.example.moveon.domain.model.User
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val inventoryRepository: InventoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = mutableStateOf(SettingsUiState(isLoading = true))
    val uiState: State<SettingsUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    init {
        loadSettings()
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setPushNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
        }
    }

    fun setEmailNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setEmailNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(emailNotificationsEnabled = enabled)
        }
    }

    fun setShareLiveLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setShareLiveLocationEnabled(enabled)
            _uiState.value = _uiState.value.copy(shareLiveLocationEnabled = enabled)
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
            _uiState.value = _uiState.value.copy(darkModeEnabled = enabled)
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoSyncEnabled(enabled)
            _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
            refreshStorageSummary()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            userPreferences.setLastManualSyncAt(System.currentTimeMillis())
            refreshStorageSummary()
            _eventFlow.emit("Data synced locally")
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            clearCacheDirectory(context.cacheDir)
            refreshStorageSummary()
            _eventFlow.emit("Cache cleared")
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val pushNotificationsEnabled = userPreferences.isPushNotificationsEnabled()
            val emailNotificationsEnabled = userPreferences.isEmailNotificationsEnabled()
            val shareLiveLocationEnabled = userPreferences.isShareLiveLocationEnabled()
            val darkModeEnabled = userPreferences.isDarkModeEnabled()
            val autoSyncEnabled = userPreferences.isAutoSyncEnabled()

            val totalBoxes = runCatching { inventoryRepository.getTotalBoxesCount() }.getOrDefault(0)
            val totalItems = runCatching { inventoryRepository.getTotalItemsCount().first() }.getOrDefault(0)
            val cacheSizeLabel = formatBytes(directorySize(context.cacheDir))
            val lastSyncLabel = userPreferences.getLastManualSyncAt()?.let(::formatTimestamp)

            _uiState.value = SettingsUiState(
                isLoading = false,
                pushNotificationsEnabled = pushNotificationsEnabled,
                emailNotificationsEnabled = emailNotificationsEnabled,
                shareLiveLocationEnabled = shareLiveLocationEnabled,
                darkModeEnabled = darkModeEnabled,
                autoSyncEnabled = autoSyncEnabled,
                offlineBoxesCount = totalBoxes,
                offlineItemsCount = totalItems,
                cacheSizeLabel = cacheSizeLabel,
                lastSyncLabel = lastSyncLabel
            )
        }
    }

    private suspend fun refreshStorageSummary() {
        val totalBoxes = runCatching { inventoryRepository.getTotalBoxesCount() }.getOrDefault(0)
        val totalItems = runCatching { inventoryRepository.getTotalItemsCount().first() }.getOrDefault(0)
        val cacheSizeLabel = formatBytes(directorySize(context.cacheDir))
        val lastSyncLabel = userPreferences.getLastManualSyncAt()?.let(::formatTimestamp)

        _uiState.value = _uiState.value.copy(
            offlineBoxesCount = totalBoxes,
            offlineItemsCount = totalItems,
            cacheSizeLabel = cacheSizeLabel,
            lastSyncLabel = lastSyncLabel
        )
    }

    private fun clearCacheDirectory(directory: File) {
        directory.listFiles()?.forEach { file ->
            file.deleteRecursively()
        }
    }

    private fun directorySize(directory: File): Long {
        if (!directory.exists()) return 0L
        return directory.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / 1024f / 1024f
        return String.format(Locale.getDefault(), "%.1f MB", mb.coerceAtLeast(0f))
    }

    private fun formatTimestamp(timestampMillis: Long): String {
        val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return formatter.format(Date(timestampMillis))
    }
}

@HiltViewModel
class SecurityViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val currentUser = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}

@HiltViewModel
class PasswordUpdateViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()
    private val _requiresReauth = mutableStateOf(false)
    val requiresReauth: State<Boolean> = _requiresReauth

    fun updatePassword(newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.updatePassword(newPassword)
            result.onSuccess {
                _requiresReauth.value = false
                onSuccess()
            }.onFailure { e ->
                val message = e.message.orEmpty()
                if (message.contains("requires recent authentication", ignoreCase = true) ||
                    message.contains("recent login", ignoreCase = true)
                ) {
                    _requiresReauth.value = true
                    _eventFlow.emit("Please enter your current password to continue.")
                } else {
                _eventFlow.emit(e.message ?: "Failed to update password")
                }
            }
        }
    }

    fun reauthenticateAndUpdatePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val reauthResult = authRepository.reauthenticate(currentPassword)
            reauthResult.onSuccess {
                updatePassword(newPassword, onSuccess)
            }.onFailure { e ->
                _eventFlow.emit(e.message ?: "Re-authentication failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _eventFlow.emit("Password reset link sent to $email")
                onSuccess()
            }.onFailure { e ->
                _eventFlow.emit(e.message ?: "Failed to send reset email")
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val pushNotificationsEnabled: Boolean = false,
    val emailNotificationsEnabled: Boolean = false,
    val shareLiveLocationEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val offlineBoxesCount: Int = 0,
    val offlineItemsCount: Int = 0,
    val cacheSizeLabel: String = "0.0 MB",
    val lastSyncLabel: String? = null
)