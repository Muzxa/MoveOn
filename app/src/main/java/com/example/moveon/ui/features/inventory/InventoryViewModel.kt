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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID
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

    private val pendingCloudActions = mutableListOf<PendingCloudAction>()
    private var retryJob: Job? = null

    init {
        observeBoxes()
    }

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
                    selectedRoomSuggestion = if (event.value == _uiState.value.selectedRoomSuggestion) {
                        _uiState.value.selectedRoomSuggestion
                    } else {
                        null
                    },
                    errorMessage = null
                )
            }

            is InventoryEvent.RoomSuggestionSelected -> {
                _uiState.value = _uiState.value.copy(
                    roomName = event.value,
                    selectedRoomSuggestion = event.value,
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

            is InventoryEvent.SetPackedState -> {
                setPackedState(event.boxUuid, event.boxId, event.packed)
            }

            is InventoryEvent.DeleteBox -> {
                deleteBox(event.boxUuid, event.boxId)
            }

            is InventoryEvent.ModifyBox -> {
                modifyBox(
                    boxUuid = event.boxUuid,
                    originalBoxId = event.originalBoxId,
                    roomName = event.roomName,
                    customId = event.customId,
                    colorHex = event.colorHex
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
                .ifBlank { generateBoxId(room) }
            val boxUuid = UUID.randomUUID().toString()

            val inferredCategory = roomNameToCategory(room)

            val box = Box(
                boxUuid = boxUuid,
                boxId = boxId,
                bookingId = DEFAULT_BOOKING_ID,
                vehicleId = null,
                category = inferredCategory.name,
                label = room,
                volume = 15.0,
                packed = false
            )

            val cloudResult = inventoryRepository.addNewBoxToCloud(
                box = box,
                userId = uid,
                colorHex = current.selectedColorHex
            )

            if (cloudResult.isFailure) {
                val message = cloudResult.exceptionOrNull()?.message ?: "Could not save box to Firestore"
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = message
                )
                _eventFlow.emit(InventoryUiEvent.ShowToast("Failed to create box: $message"))
                return@launch
            }

            // Persist locally only after Firestore creation succeeds.
            runCatching {
                inventoryRepository.addNewBox(box)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = throwable.message ?: "Saved to Firestore but failed to cache locally"
                )
                _eventFlow.emit(
                    InventoryUiEvent.ShowToast(
                        "Box $boxId saved to Firestore, but local cache failed"
                    )
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isAddBoxDialogVisible = false,
                roomName = "",
                customId = "",
                selectedRoomSuggestion = null,
                selectedCategory = MoveOnCategory.LivingRoom,
                selectedColorHex = DEFAULT_COLOR_HEX,
                isSaving = false,
                errorMessage = null
            )

            _eventFlow.emit(InventoryUiEvent.ShowToast("Box $boxId created successfully"))
        }
    }

    private fun observeBoxes() {
        viewModelScope.launch {
            inventoryRepository.getBoxesForMove(DEFAULT_BOOKING_ID)
                .collect { boxes ->
                    _uiState.value = _uiState.value.copy(
                        storedBoxes = boxes.map {
                            StoredInventoryBox(
                                boxUuid = it.boxUuid,
                                boxId = it.boxId,
                                label = it.label,
                                category = it.category.toMoveOnCategory(),
                                packed = it.packed
                            )
                        }
                    )
                }
        }
    }

    private fun setPackedState(boxUuid: String, boxId: String, packed: Boolean) {
        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Sign in required to update $boxId"))
                return@launch
            }

            // Optimistic local update first.
            runCatching {
                inventoryRepository.updateBoxPackedStatus(boxUuid, packed)
            }.onFailure {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Failed to update $boxId locally"))
                return@launch
            }

            val cloudResult = inventoryRepository.updateBoxPackedStatusInCloud(
                boxUuid = boxUuid,
                userId = uid,
                isPacked = packed
            )

            if (cloudResult.isFailure) {
                enqueuePendingAction(
                    PendingCloudAction.UpdatePacked(
                        boxUuid = boxUuid,
                        boxId = boxId,
                        packed = packed
                    )
                )
                _eventFlow.emit(InventoryUiEvent.ShowToast("$boxId updated locally. Cloud sync will retry."))
            } else {
                val statusLabel = if (packed) "packed" else "unpacked"
                _eventFlow.emit(InventoryUiEvent.ShowToast("$boxId marked as $statusLabel"))
            }
        }
    }

    private fun deleteBox(boxUuid: String, boxId: String) {
        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Sign in required to delete $boxId"))
                return@launch
            }

            // Optimistic local delete first.
            runCatching {
                inventoryRepository.deleteBox(boxUuid)
            }.onFailure {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Failed to delete $boxId locally"))
                return@launch
            }

            val cloudResult = inventoryRepository.deleteBoxFromCloud(
                boxUuid = boxUuid,
                userId = uid
            )

            if (cloudResult.isFailure) {
                enqueuePendingAction(
                    PendingCloudAction.Delete(
                        boxUuid = boxUuid,
                        boxId = boxId
                    )
                )
                _eventFlow.emit(InventoryUiEvent.ShowToast("$boxId deleted locally. Cloud sync will retry."))
            } else {
                _eventFlow.emit(InventoryUiEvent.ShowToast("$boxId deleted"))
            }
        }
    }

    private fun modifyBox(
        boxUuid: String,
        originalBoxId: String,
        roomName: String,
        customId: String,
        colorHex: String
    ) {
        val room = roomName.trim()
        if (room.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Room name is required"))
            }
            return
        }

        val nextBoxId = customId.trim().uppercase().ifBlank { originalBoxId }
        val nextCategory = roomNameToCategory(room).name

        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Sign in required to modify $originalBoxId"))
                return@launch
            }

            // Optimistic local update first.
            runCatching {
                inventoryRepository.updateBoxInfo(
                    boxUuid = boxUuid,
                    boxId = nextBoxId,
                    category = nextCategory,
                    label = room
                )
            }.onFailure {
                _eventFlow.emit(InventoryUiEvent.ShowToast("Failed to modify $originalBoxId locally"))
                return@launch
            }

            val cloudResult = inventoryRepository.updateBoxInfoInCloud(
                boxUuid = boxUuid,
                userId = uid,
                boxId = nextBoxId,
                category = nextCategory,
                label = room,
                colorHex = colorHex
            )

            if (cloudResult.isFailure) {
                enqueuePendingAction(
                    PendingCloudAction.Modify(
                        boxUuid = boxUuid,
                        originalBoxId = originalBoxId,
                        nextBoxId = nextBoxId,
                        category = nextCategory,
                        label = room,
                        colorHex = colorHex
                    )
                )
                _eventFlow.emit(InventoryUiEvent.ShowToast("$nextBoxId updated locally. Cloud sync will retry."))
            } else {
                _eventFlow.emit(InventoryUiEvent.ShowToast("$nextBoxId updated"))
            }
        }
    }

    private fun enqueuePendingAction(action: PendingCloudAction) {
        pendingCloudActions.removeAll { it.key() == action.key() }
        pendingCloudActions.add(action)
        ensureRetryLoop()
    }

    private fun ensureRetryLoop() {
        if (retryJob?.isActive == true) return

        retryJob = viewModelScope.launch {
            while (isActive) {
                if (pendingCloudActions.isEmpty()) {
                    delay(15_000)
                    continue
                }

                retryPendingCloudActions()
                delay(15_000)
            }
        }
    }

    private suspend fun retryPendingCloudActions() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val iterator = pendingCloudActions.toList()

        iterator.forEach { action ->
            val result = when (action) {
                is PendingCloudAction.UpdatePacked -> {
                    inventoryRepository.updateBoxPackedStatusInCloud(
                        boxUuid = action.boxUuid,
                        userId = uid,
                        isPacked = action.packed
                    )
                }

                is PendingCloudAction.Delete -> {
                    inventoryRepository.deleteBoxFromCloud(
                        boxUuid = action.boxUuid,
                        userId = uid
                    )
                }

                is PendingCloudAction.Modify -> {
                    inventoryRepository.updateBoxInfoInCloud(
                        boxUuid = action.boxUuid,
                        userId = uid,
                        boxId = action.nextBoxId,
                        category = action.category,
                        label = action.label,
                        colorHex = action.colorHex
                    )
                }
            }

            if (result.isSuccess) {
                pendingCloudActions.removeAll { it.key() == action.key() }
            }
        }
    }

    private fun String.toMoveOnCategory(): MoveOnCategory {
        return when (trim().lowercase()) {
            "livingroom", "living_room", "living room" -> MoveOnCategory.LivingRoom
            "bedroom", "bed room" -> MoveOnCategory.Bedroom
            "kitchen" -> MoveOnCategory.Kitchen
            "bathroom", "bath room" -> MoveOnCategory.Bathroom
            "storage" -> MoveOnCategory.Storage
            "office" -> MoveOnCategory.Office
            else -> roomNameToCategory(this)
        }
    }

    private fun generateBoxId(roomName: String): String {
        val words = roomName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val initials = when {
            words.size >= 2 -> ("${words[0].first()}${words[1].first()}")
            words.size == 1 -> words[0].take(2).padEnd(2, 'X')
            else -> "BX"
        }.uppercase()

        val uniqueId = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(6)
            .uppercase()

        return "$initials-$uniqueId"
    }

    private fun roomNameToCategory(roomName: String): MoveOnCategory {
        val normalized = roomName.trim().lowercase()
        return when {
            normalized.contains("living") -> MoveOnCategory.LivingRoom
            normalized.contains("bed") -> MoveOnCategory.Bedroom
            normalized.contains("kitchen") -> MoveOnCategory.Kitchen
            normalized.contains("bath") -> MoveOnCategory.Bathroom
            normalized.contains("storage") -> MoveOnCategory.Storage
            normalized.contains("office") -> MoveOnCategory.Office
            else -> MoveOnCategory.Storage
        }
    }

    companion object {
        const val DEFAULT_COLOR_HEX = "#1565C0"
        private const val DEFAULT_BOOKING_ID = 0
    }
}

data class InventoryUiState(
    val isAddBoxDialogVisible: Boolean = false,
    val roomName: String = "",
    val selectedRoomSuggestion: String? = null,
    val customId: String = "",
    val selectedCategory: MoveOnCategory = MoveOnCategory.LivingRoom,
    val selectedColorHex: String = InventoryViewModel.DEFAULT_COLOR_HEX,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val storedBoxes: List<StoredInventoryBox> = emptyList()
)

data class StoredInventoryBox(
    val boxUuid: String,
    val boxId: String,
    val label: String,
    val category: MoveOnCategory,
    val packed: Boolean
)

sealed class InventoryEvent {
    object OpenAddBoxDialog : InventoryEvent()
    object CloseAddBoxDialog : InventoryEvent()
    data class RoomNameChanged(val value: String) : InventoryEvent()
    data class RoomSuggestionSelected(val value: String) : InventoryEvent()
    data class CustomIdChanged(val value: String) : InventoryEvent()
    data class CategorySelected(val category: MoveOnCategory) : InventoryEvent()
    data class ColorSelected(val hex: String) : InventoryEvent()
    data class SetPackedState(val boxUuid: String, val boxId: String, val packed: Boolean) : InventoryEvent()
    data class DeleteBox(val boxUuid: String, val boxId: String) : InventoryEvent()
    data class ModifyBox(
        val boxUuid: String,
        val originalBoxId: String,
        val roomName: String,
        val customId: String,
        val colorHex: String
    ) : InventoryEvent()
    object CreateBox : InventoryEvent()
}

sealed class InventoryUiEvent {
    data class ShowToast(val message: String) : InventoryUiEvent()
}

private sealed class PendingCloudAction {
    data class UpdatePacked(
        val boxUuid: String,
        val boxId: String,
        val packed: Boolean
    ) : PendingCloudAction()

    data class Delete(
        val boxUuid: String,
        val boxId: String
    ) : PendingCloudAction()

    data class Modify(
        val boxUuid: String,
        val originalBoxId: String,
        val nextBoxId: String,
        val category: String,
        val label: String,
        val colorHex: String
    ) : PendingCloudAction()

    fun key(): String {
        return when (this) {
            is UpdatePacked -> "updatePacked:$boxUuid"
            is Delete -> "delete:$boxUuid"
            is Modify -> "modify:$boxUuid"
        }
    }
}
