package com.example.moveon.ui.features.inventory

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Box
import com.example.moveon.domain.repository.InventoryRepository
import com.example.moveon.ui.components.MoveOnCategory
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = mutableStateOf(InventoryUiState())
    val uiState: State<InventoryUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<InventoryUiEvent>()
    val eventFlow: SharedFlow<InventoryUiEvent> = _eventFlow.asSharedFlow()

    fun onEvent(event: InventoryEvent) {
        when (event) {
            InventoryEvent.OpenAddBoxDialog -> {
                _uiState.value = _uiState.value.copy(
                    isAddBoxDialogVisible = true,
                    errorMessage = null
                )
            }

            InventoryEvent.CloseAddBoxDialog -> {
                if (_uiState.value.isSaving) return
                _uiState.value = _uiState.value.copy(
                    isAddBoxDialogVisible = false,
                    errorMessage = null
                )
            }

            is InventoryEvent.RoomNameChanged -> {
                _uiState.value = _uiState.value.copy(
                    roomName = event.value,
                    errorMessage = null
                )
            }

            is InventoryEvent.CustomIdChanged -> {
                _uiState.value = _uiState.value.copy(
                    customId = event.value,
                    errorMessage = null
                )
            }

            is InventoryEvent.CategorySelected -> {
                _uiState.value = _uiState.value.copy(
                    selectedCategory = event.category,
                    errorMessage = null
                )
            }

            is InventoryEvent.ColorSelected -> {
                _uiState.value = _uiState.value.copy(
                    selectedColorHex = event.hex,
                    errorMessage = null
                )
            }

            InventoryEvent.CreateBox -> createBox()
        }
    }

    private fun createBox() {
        val current = _uiState.value
        if (current.isSaving) return

        val room = current.roomName.trim()
        if (room.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Room name is required")
            viewModelScope.launch {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Enter a room name to create a box"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val uid = firebaseAuth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "You need to be logged in to create a box"
                )
                _eventFlow.emit(InventoryUiEvent.ShowToast("Sign in required to save box"))
                return@launch
            }

            val boxId = current.customId
                .trim()
                .uppercase()
                .ifBlank { generateBoxId(current.selectedCategory) }

            val box = Box(
                id = boxId,
                bookingId = "0",
                vehicleId = null,
                category = current.selectedCategory.name,
                label = room,
                volume = 15.0,
                qrImagePath = ""
            )

            val cloudResult = inventoryRepository.addNewBoxToCloud(
                box = box,
                userId = uid,
                colorHex = current.selectedColorHex
            )

            if (cloudResult.isFailure) {
                val message = cloudResult.exceptionOrNull()?.message ?: "Could not save box"
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = message
                )
                _eventFlow.emit(InventoryUiEvent.ShowToast("Failed to create box: $message"))
                return@launch
            }

            runCatching {
                inventoryRepository.addNewBox(box)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = throwable.message ?: "Saved remotely but failed to cache locally"
                )
                _eventFlow.emit(
                    InventoryUiEvent.ShowToast(
                        "Box $boxId synced to cloud, but local cache update failed"
                    )
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isAddBoxDialogVisible = false,
                roomName = "",
                customId = "",
                selectedCategory = MoveOnCategory.LivingRoom,
                selectedColorHex = DEFAULT_COLOR_HEX,
                isSaving = false,
                errorMessage = null,
                createdBoxes = listOf(
                    CreatedInventoryBox(
                        id = boxId,
                        category = current.selectedCategory
                    )
                ) + _uiState.value.createdBoxes
            )

            _eventFlow.emit(InventoryUiEvent.ShowToast("Box $boxId created successfully"))
        }
    }

    private fun generateBoxId(category: MoveOnCategory): String {
        val prefix = when (category) {
            MoveOnCategory.LivingRoom -> "LR"
            MoveOnCategory.Bedroom -> "BR"
            MoveOnCategory.Kitchen -> "KT"
            MoveOnCategory.Bathroom -> "BT"
            MoveOnCategory.Storage -> "ST"
            MoveOnCategory.Office -> "OF"
        }

        val suffix = (System.currentTimeMillis() % 1000).toString().padStart(3, '0')
        return "$prefix-$suffix"
    }

    companion object {
        const val DEFAULT_COLOR_HEX = "#1565C0"
    }
}

data class InventoryUiState(
    val isAddBoxDialogVisible: Boolean = false,
    val roomName: String = "",
    val customId: String = "",
    val selectedCategory: MoveOnCategory = MoveOnCategory.LivingRoom,
    val selectedColorHex: String = InventoryViewModel.DEFAULT_COLOR_HEX,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdBoxes: List<CreatedInventoryBox> = emptyList()
)

data class CreatedInventoryBox(
    val id: String,
    val category: MoveOnCategory
)

sealed class InventoryEvent {
    object OpenAddBoxDialog : InventoryEvent()
    object CloseAddBoxDialog : InventoryEvent()
    data class RoomNameChanged(val value: String) : InventoryEvent()
    data class CustomIdChanged(val value: String) : InventoryEvent()
    data class CategorySelected(val category: MoveOnCategory) : InventoryEvent()
    data class ColorSelected(val hex: String) : InventoryEvent()
    object CreateBox : InventoryEvent()
}

sealed class InventoryUiEvent {
    data class ShowToast(val message: String) : InventoryUiEvent()
}
