package com.example.moveon.ui.features.inventory

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moveon.domain.model.Item
import com.example.moveon.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BoxItemsViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(BoxItemsUiState())
    val uiState: State<BoxItemsUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<BoxItemsUiEvent>()
    val eventFlow: SharedFlow<BoxItemsUiEvent> = _eventFlow.asSharedFlow()

    private var observedBoxId: String? = null

    fun observeItems(boxId: String) {
        if (observedBoxId == boxId) return
        observedBoxId = boxId

        viewModelScope.launch {
            inventoryRepository.getItemsInBox(boxId)
                .collectLatest { items ->
                    _uiState.value = _uiState.value.copy(items = items)
                }
        }
    }

    fun addItem(
        boxId: String,
        name: String,
        quantity: Int,
        description: String,
        isFragile: Boolean,
        imageUrl: String = "",
        onResult: (Boolean) -> Unit = {}
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Item name is required"))
                onResult(false)
            }
            return
        }

        if (quantity <= 0) {
            viewModelScope.launch {
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Quantity must be at least 1"))
                onResult(false)
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            var saveSuccessful = false

            runCatching {
                inventoryRepository.addItemToInventory(
                    Item(
                        id = UUID.randomUUID().toString(),
                        boxId = boxId,
                        name = normalizedName,
                        quantity = quantity,
                        description = description.trim(),
                        imageUrl = imageUrl,
                        isFragile = isFragile
                    )
                )
            }.onSuccess {
                saveSuccessful = true
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Item saved"))
            }.onFailure { throwable ->
                _eventFlow.emit(
                    BoxItemsUiEvent.ShowToast(
                        throwable.message ?: "Failed to save item"
                    )
                )
            }

            _uiState.value = _uiState.value.copy(isSaving = false)
            onResult(saveSuccessful)
        }
    }

    fun updateItem(
        item: Item,
        onResult: (Boolean) -> Unit = {}
    ) {
        val normalizedName = item.name.trim()
        if (normalizedName.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Item name is required"))
                onResult(false)
            }
            return
        }

        if (item.quantity <= 0) {
            viewModelScope.launch {
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Quantity must be at least 1"))
                onResult(false)
            }
            return
        }

        val updatedItem = item.copy(
            name = normalizedName,
            description = item.description.trim()
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            var saveSuccessful = false

            runCatching {
                inventoryRepository.updateItemInInventory(updatedItem)
            }.onSuccess {
                saveSuccessful = true
                _uiState.value = _uiState.value.copy(
                    items = _uiState.value.items.map { current ->
                        if (current.id == updatedItem.id) updatedItem else current
                    }
                )
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Item updated"))
            }.onFailure { throwable ->
                _eventFlow.emit(
                    BoxItemsUiEvent.ShowToast(
                        throwable.message ?: "Failed to update item"
                    )
                )
            }

            _uiState.value = _uiState.value.copy(isSaving = false)
            onResult(saveSuccessful)
        }
    }

    fun deleteItem(
        item: Item,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            var deleteSuccessful = false

            runCatching {
                inventoryRepository.deleteItemFromInventory(item)
            }.onSuccess {
                deleteSuccessful = true
                _uiState.value = _uiState.value.copy(
                    items = _uiState.value.items.filterNot { current -> current.id == item.id }
                )
                _eventFlow.emit(BoxItemsUiEvent.ShowToast("Item deleted"))
            }.onFailure { throwable ->
                _eventFlow.emit(
                    BoxItemsUiEvent.ShowToast(
                        throwable.message ?: "Failed to delete item"
                    )
                )
            }

            onResult(deleteSuccessful)
        }
    }
}

data class BoxItemsUiState(
    val items: List<Item> = emptyList(),
    val isSaving: Boolean = false
)

sealed class BoxItemsUiEvent {
    data class ShowToast(val message: String) : BoxItemsUiEvent()
}
