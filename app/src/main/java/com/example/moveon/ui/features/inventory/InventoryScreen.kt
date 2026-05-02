package com.example.moveon.ui.features.inventory

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
// removed unused ChevronRight import; chevron removed from UI
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.BoxIcon
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MoveOnCategory
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.components.MoveOnStatCard
import com.example.moveon.ui.components.QrIcon
import com.example.moveon.ui.components.iconSpec
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.Success
import com.example.moveon.ui.theme.Tertiary
import com.example.moveon.ui.theme.ErrorDeep
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.print.PrintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PackedBoxUi(
    val boxUuid: String,
    val code: String,
    val roomLabel: String,
    val category: MoveOnCategory,
    val volume: String,
    val itemCount: Int
)

private data class UnpackedBoxUi(
    val boxUuid: String,
    val code: String,
    val category: MoveOnCategory,
    val roomLabel: String
)

private data class QrViewerData(
    val boxUuid: String,
    val boxId: String,
    val roomLabel: String
)

private data class ModifyBoxDialogData(
    val boxUuid: String,
    val boxId: String,
    val roomLabel: String,
    val category: MoveOnCategory
)

private data class DeleteBoxDialogData(
    val boxUuid: String,
    val boxId: String,
    val itemCount: Int?
)

@Composable
fun InventoryScreen(
    onTabSelected: (DashboardTab) -> Unit = {},
    onScanBoxClick: () -> Unit = {},
    onBoxClick: (boxUuid: String, scannedFromQr: Boolean) -> Unit = { _, _ -> },
    onAddItemsClick: (boxUuid: String, boxId: String) -> Unit = { _, _ -> },
    onAddBoxClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.value
    val context = LocalContext.current
    var qrViewerData by remember { mutableStateOf<QrViewerData?>(null) }
    var modifyBoxData by remember { mutableStateOf<ModifyBoxDialogData?>(null) }
    var deleteBoxData by remember { mutableStateOf<DeleteBoxDialogData?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is InventoryUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val packedBoxes = state.storedBoxes
        .filter { it.packed }
        .map {
            PackedBoxUi(
                boxUuid = it.boxUuid,
                code = "Box ${it.boxId}",
                roomLabel = it.label,
                category = it.category,
                volume = "15m³",
                itemCount = state.itemCountsByBoxId[it.boxId] ?: 0
            )
        }

    val unpackedBoxes = state.storedBoxes
        .filter { !it.packed }
        .map {
        UnpackedBoxUi(
            boxUuid = it.boxUuid,
            code = "Box ${it.boxId}",
            category = it.category,
            roomLabel = it.label
        )
    }

    val normalizedQuery = searchQuery.trim().lowercase()
    val filteredPackedBoxes = if (normalizedQuery.isBlank()) {
        packedBoxes
    } else {
        packedBoxes.filter { box ->
            listOf(box.code, box.roomLabel, toTitle(box.category))
                .any { it.lowercase().contains(normalizedQuery) }
        }
    }
    val filteredUnpackedBoxes = if (normalizedQuery.isBlank()) {
        unpackedBoxes
    } else {
        unpackedBoxes.filter { box ->
            listOf(box.code, box.roomLabel, toTitle(box.category))
                .any { it.lowercase().contains(normalizedQuery) }
        }
    }

    Scaffold(
        containerColor = LightBackground,
        bottomBar = {
            MoveOnBottomBar(
                selectedTab = DashboardTab.Inventory,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Your Inventory",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )
                Text(
                    text = "Digital tracking for your move",
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextSecondary
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MoveOnStatCard(
                        value = state.storedBoxes.size.toString(),
                        label = "Boxes",
                        modifier = Modifier.weight(1f)
                    )
                    MoveOnStatCard(
                        value = state.totalItemsCount.toString(),
                        label = "Items",
                        modifier = Modifier.weight(1f)
                    )
                    MoveOnStatCard(
                        value = state.totalFragileItemsCount.toString(),
                        label = "Fragile",
                        modifier = Modifier.weight(1f),
                        valueColor = Accent
                    )
                }

                Spacer(Modifier.height(12.dp))

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MoveOnPillButton(
                        text = "Scan Box",
                        onClick = onScanBoxClick,
                        modifier = Modifier.weight(1f),
                        background = Accent
                    )
                    MoveOnPillButton(
                        text = "Add Box",
                        onClick = {
                            onAddBoxClick()
                            viewModel.onEvent(InventoryEvent.OpenAddBoxDialog)
                        },
                        modifier = Modifier.weight(1f),
                        background = Primary
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Packed Boxes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )
            }

            if (filteredPackedBoxes.isEmpty()) {
                item {
                    Text(
                        text = if (normalizedQuery.isBlank()) {
                            "No packed boxes yet"
                        } else {
                            "No packed boxes match your search"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }
            }

            items(filteredPackedBoxes.size) { index ->
                PackedBoxCard(
                    box = filteredPackedBoxes[index],
                    onCardClick = { onBoxClick(filteredPackedBoxes[index].boxUuid, false) },
                    onViewQrCodeClick = { boxUuid, boxCode, roomLabel ->
                        qrViewerData = QrViewerData(
                            boxUuid = boxUuid,
                            boxId = extractBoxId(boxCode),
                            roomLabel = roomLabel
                        )
                    },
                    onAddItemsClick = {
                        onAddItemsClick(
                            filteredPackedBoxes[index].boxUuid,
                            extractBoxId(filteredPackedBoxes[index].code)
                        )
                    },
                    onMarkAsUnpackedClick = {
                        viewModel.onEvent(
                            InventoryEvent.SetPackedState(
                                boxUuid = filteredPackedBoxes[index].boxUuid,
                                boxId = extractBoxId(filteredPackedBoxes[index].code),
                                packed = false
                            )
                        )
                    },
                    onEditBoxClick = {
                        val box = filteredPackedBoxes[index]
                        modifyBoxData = ModifyBoxDialogData(
                            boxUuid = box.boxUuid,
                            boxId = extractBoxId(box.code),
                            roomLabel = box.roomLabel,
                            category = box.category
                        )
                    },
                    onDeleteBoxClick = {
                        deleteBoxData = DeleteBoxDialogData(
                            boxUuid = filteredPackedBoxes[index].boxUuid,
                            boxId = extractBoxId(filteredPackedBoxes[index].code),
                            itemCount = filteredPackedBoxes[index].itemCount
                        )
                    }
                )
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Unpacked Boxes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )
            }

            if (filteredUnpackedBoxes.isEmpty()) {
                item {
                    Text(
                        text = if (normalizedQuery.isBlank()) {
                            "No boxes found. Tap Add Box to create one."
                        } else {
                            "No boxes match your search"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }
            }

            items(filteredUnpackedBoxes.size) { index ->
                UnpackedBoxCard(
                    box = filteredUnpackedBoxes[index],
                    onCardClick = { onBoxClick(filteredUnpackedBoxes[index].boxUuid, false) },
                    onViewQrCodeClick = { boxUuid, boxCode, roomLabel ->
                        qrViewerData = QrViewerData(
                            boxUuid = boxUuid,
                            boxId = extractBoxId(boxCode),
                            roomLabel = roomLabel
                        )
                    },
                    onAddItemsClick = {
                        onAddItemsClick(
                            filteredUnpackedBoxes[index].boxUuid,
                            extractBoxId(filteredUnpackedBoxes[index].code)
                        )
                    },
                    onMarkAsUnpackedClick = {
                        viewModel.onEvent(
                            InventoryEvent.SetPackedState(
                                boxUuid = filteredUnpackedBoxes[index].boxUuid,
                                boxId = extractBoxId(filteredUnpackedBoxes[index].code),
                                packed = true
                            )
                        )
                    },
                    onEditBoxClick = {
                        val box = filteredUnpackedBoxes[index]
                        modifyBoxData = ModifyBoxDialogData(
                            boxUuid = box.boxUuid,
                            boxId = extractBoxId(box.code),
                            roomLabel = box.roomLabel,
                            category = box.category
                        )
                    },
                    onDeleteBoxClick = {
                        deleteBoxData = DeleteBoxDialogData(
                            boxUuid = filteredUnpackedBoxes[index].boxUuid,
                            boxId = extractBoxId(filteredUnpackedBoxes[index].code),
                            itemCount = null
                        )
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }

        if (state.isAddBoxDialogVisible) {
            AddBoxDialog(
                state = state,
                onEvent = viewModel::onEvent
            )
        }

        qrViewerData?.let { data ->
            ViewQrCodeDialog(
                boxUuid = data.boxUuid,
                boxId = data.boxId,
                roomLabel = data.roomLabel,
                onDismiss = { qrViewerData = null }
            )
        }

        modifyBoxData?.let { data ->
            ModifyBoxDialog(
                initialRoomName = data.roomLabel,
                initialCustomId = data.boxId,
                initialCategory = data.category,
                onDismiss = { modifyBoxData = null },
                onSave = { roomName, customId, colorHex ->
                    viewModel.onEvent(
                        InventoryEvent.ModifyBox(
                            boxUuid = data.boxUuid,
                            originalBoxId = data.boxId,
                            roomName = roomName,
                            customId = customId,
                            colorHex = colorHex
                        )
                    )
                    modifyBoxData = null
                }
            )
        }

        deleteBoxData?.let { data ->
            DeleteBoxConfirmationSheet(
                data = data,
                onDismiss = { deleteBoxData = null },
                onConfirm = {
                    viewModel.onEvent(
                        InventoryEvent.DeleteBox(
                            boxUuid = data.boxUuid,
                            boxId = data.boxId
                        )
                    )
                    deleteBoxData = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBoxConfirmationSheet(
    data: DeleteBoxDialogData,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val itemCount = data.itemCount?.takeIf { it > 0 }
    val description = if (itemCount != null) {
        "This will permanently remove the box and all $itemCount logged items. This can't be undone."
    } else {
        "This will permanently remove the box and its logged items. This can't be undone."
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LightSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ErrorDeep.copy(alpha = 0.1f), RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = ErrorDeep,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Delete Box ${data.boxId}?",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightTextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, LightBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightSurfaceVariant)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = LightTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = LightTextPrimary
                        )
                    }
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorDeep)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Delete box",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class DialogColorOption(
    val hex: String,
    val color: Color
)

private val dialogColorOptions = listOf(
    DialogColorOption(hex = "#1565C0", color = Primary),
    DialogColorOption(hex = "#FF6F00", color = Accent),
    DialogColorOption(hex = "#2E7D32", color = Success),
    DialogColorOption(hex = "#7C4DFF", color = Tertiary),
    DialogColorOption(hex = "#D32F2F", color = ErrorDeep),
    DialogColorOption(hex = "#00897B", color = Color(0xFF00897B))
)

private val roomNameSuggestions = listOf(
    "Living Room",
    "Bedroom",
    "Kitchen",
    "Bathroom",
    "Storage",
    "Office"
)

@Composable
private fun AddBoxDialog(
    state: InventoryUiState,
    onEvent: (InventoryEvent) -> Unit
) {
    Dialog(onDismissRequest = { onEvent(InventoryEvent.CloseAddBoxDialog) }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Box",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Room Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextPrimary
                    )

                    TextField(
                        value = state.roomName,
                        onValueChange = { onEvent(InventoryEvent.RoomNameChanged(it)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LightTextPrimary),
                        placeholder = {
                            Text(
                                text = "Enter room name",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightSurfaceVariant,
                            unfocusedContainerColor = LightSurfaceVariant,
                            disabledContainerColor = LightSurfaceVariant,
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextPrimary,
                            disabledTextColor = LightTextSecondary,
                            focusedPlaceholderColor = LightTextSecondary,
                            unfocusedPlaceholderColor = LightTextSecondary,
                            disabledPlaceholderColor = LightTextSecondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        )
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roomNameSuggestions.forEach { suggestion ->
                            val selected = state.selectedRoomSuggestion == suggestion
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Primary else LightBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(if (selected) Primary.copy(alpha = 0.08f) else LightSurface)
                                    .clickable { onEvent(InventoryEvent.RoomSuggestionSelected(suggestion)) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Custom ID (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextPrimary
                    )

                    TextField(
                        value = state.customId,
                        onValueChange = { onEvent(InventoryEvent.CustomIdChanged(it)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LightTextPrimary),
                        placeholder = {
                            Text(
                                text = "e.g., LR-001",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightSurfaceVariant,
                            unfocusedContainerColor = LightSurfaceVariant,
                            disabledContainerColor = LightSurfaceVariant,
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextPrimary,
                            disabledTextColor = LightTextSecondary,
                            focusedPlaceholderColor = LightTextSecondary,
                            unfocusedPlaceholderColor = LightTextSecondary,
                            disabledPlaceholderColor = LightTextSecondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        )
                    )

                    Text(
                        text = "Leave blank for auto-generated ID",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Color Code",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextPrimary
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        dialogColorOptions.forEach { colorOption ->
                            val selected = state.selectedColorHex == colorOption.hex
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorOption.color)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) LightTextPrimary else LightBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onEvent(InventoryEvent.ColorSelected(colorOption.hex)) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                MoveOnPillButton(
                    text = if (state.isSaving) "Creating..." else "Create Box",
                    onClick = { onEvent(InventoryEvent.CreateBox) },
                    modifier = Modifier.fillMaxWidth(),
                    background = Primary,
                    enabled = !state.isSaving
                )
            }
        }
    }
}

@Composable
private fun ModifyBoxDialog(
    initialRoomName: String,
    initialCustomId: String,
    initialCategory: MoveOnCategory,
    onDismiss: () -> Unit,
    onSave: (roomName: String, customId: String, colorHex: String) -> Unit
) {
    var roomName by rememberSaveable { mutableStateOf(initialRoomName) }
    var customId by rememberSaveable { mutableStateOf(initialCustomId) }
    var selectedRoomSuggestion by rememberSaveable { mutableStateOf(initialRoomName) }
    var selectedColorHex by rememberSaveable { mutableStateOf(InventoryViewModel.DEFAULT_COLOR_HEX) }

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
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Modify Box",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Room Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextPrimary
                    )

                    TextField(
                        value = roomName,
                        onValueChange = {
                            roomName = it
                            selectedRoomSuggestion = if (it == selectedRoomSuggestion) it else ""
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LightTextPrimary),
                        placeholder = {
                            Text(
                                text = "Enter room name",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightSurfaceVariant,
                            unfocusedContainerColor = LightSurfaceVariant,
                            disabledContainerColor = LightSurfaceVariant,
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextPrimary,
                            disabledTextColor = LightTextSecondary,
                            focusedPlaceholderColor = LightTextSecondary,
                            unfocusedPlaceholderColor = LightTextSecondary,
                            disabledPlaceholderColor = LightTextSecondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        )
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roomNameSuggestions.forEach { suggestion ->
                            val selected = selectedRoomSuggestion == suggestion
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Primary else LightBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(if (selected) Primary.copy(alpha = 0.08f) else LightSurface)
                                    .clickable {
                                        roomName = suggestion
                                        selectedRoomSuggestion = suggestion
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Custom ID (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextPrimary
                    )

                    TextField(
                        value = customId,
                        onValueChange = { customId = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LightTextPrimary),
                        placeholder = {
                            Text(
                                text = "e.g., LR-001",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightSurfaceVariant,
                            unfocusedContainerColor = LightSurfaceVariant,
                            disabledContainerColor = LightSurfaceVariant,
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextPrimary,
                            disabledTextColor = LightTextSecondary,
                            focusedPlaceholderColor = LightTextSecondary,
                            unfocusedPlaceholderColor = LightTextSecondary,
                            disabledPlaceholderColor = LightTextSecondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        )
                    )

                    Text(
                        text = "Leave blank for auto-generated ID",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Color Code",
                        style = MaterialTheme.typography.labelLarge,
                        color = LightTextPrimary
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        dialogColorOptions.forEach { colorOption ->
                            val selected = selectedColorHex == colorOption.hex
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 44.dp else 40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorOption.color)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) LightTextPrimary else LightBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedColorHex = colorOption.hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                MoveOnPillButton(
                    text = "Modify Box",
                    onClick = {
                        val normalizedRoom = roomName.trim()
                        val normalizedId = customId.trim().uppercase().ifBlank {
                            if (initialCustomId.isNotBlank()) initialCustomId else {
                                val prefix = when (initialCategory) {
                                    MoveOnCategory.LivingRoom -> "LR"
                                    MoveOnCategory.Bedroom -> "BR"
                                    MoveOnCategory.Kitchen -> "KT"
                                    MoveOnCategory.Bathroom -> "BT"
                                    MoveOnCategory.Storage -> "ST"
                                    MoveOnCategory.Office -> "OF"
                                }
                                "$prefix-001"
                            }
                        }
                        onSave(normalizedRoom, normalizedId, selectedColorHex)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    background = Primary,
                    enabled = roomName.isNotBlank()
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LightTextPrimary),
        placeholder = {
            Text(
                text = "Search boxes or items...",
                style = MaterialTheme.typography.labelLarge,
                color = LightTextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = LightTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = LightTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LightSurfaceVariant,
            unfocusedContainerColor = LightSurfaceVariant,
            disabledContainerColor = LightSurfaceVariant,
            focusedTextColor = LightTextPrimary,
            unfocusedTextColor = LightTextPrimary,
            disabledTextColor = LightTextSecondary,
            focusedPlaceholderColor = LightTextSecondary,
            unfocusedPlaceholderColor = LightTextSecondary,
            disabledPlaceholderColor = LightTextSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Primary
        )
    )
}

@Composable
private fun PackedBoxCard(
    box: PackedBoxUi,
    onCardClick: () -> Unit,
    onViewQrCodeClick: (String, String, String) -> Unit,
    onAddItemsClick: () -> Unit,
    onMarkAsUnpackedClick: (String) -> Unit,
    onEditBoxClick: (String) -> Unit,
    onDeleteBoxClick: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showAddItemsConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val spec = box.category.iconSpec()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(spec.containerColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                BoxIcon(tint = spec.iconTint)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = box.code,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightTextPrimary
                        )
                        Text(
                            text = "${toTitle(box.category)} • ${box.volume}",
                            style = MaterialTheme.typography.labelLarge,
                            color = LightTextSecondary
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Box options",
                                tint = LightTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        BoxOptionsMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            packedActionLabel = "Mark As Unpacked",
                            onAddItems = {
                                menuExpanded = false
                                showAddItemsConfirmation = true
                            },
                            onTogglePackedState = {
                                menuExpanded = false
                                onMarkAsUnpackedClick(box.code)
                            },
                            onViewQrCode = {
                                menuExpanded = false
                                onViewQrCodeClick(box.boxUuid, box.code, toTitle(box.category))
                            },
                            onEditBox = {
                                menuExpanded = false
                                onEditBoxClick(box.code)
                            },
                            onDeleteBox = {
                                menuExpanded = false
                                onDeleteBoxClick(box.code)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${box.itemCount} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextPrimary
                            )
                        }
                    }

                    // Chevron removed for cleaner row layout
                }
            }
        }
    }

    if (showAddItemsConfirmation) {
        PackedBoxAddConfirmationSheet(
            boxId = box.code,
            onDismiss = { showAddItemsConfirmation = false },
            onConfirm = {
                showAddItemsConfirmation = false
                onAddItemsClick()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackedBoxAddConfirmationSheet(
    boxId: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LightSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Add Item to Packed Box?",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightTextPrimary
                    )
                    Text(
                        text = "Box $boxId is already packed. Adding another item may mean you need to repack it later.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, LightBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightSurfaceVariant)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = LightTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = LightTextPrimary
                        )
                    }
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnpackedBoxCard(
    box: UnpackedBoxUi,
    onCardClick: () -> Unit,
    onViewQrCodeClick: (String, String, String) -> Unit,
    onAddItemsClick: () -> Unit,
    onMarkAsUnpackedClick: (String) -> Unit,
    onEditBoxClick: (String) -> Unit,
    onDeleteBoxClick: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val spec = box.category.iconSpec()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(spec.containerColor, RoundedCornerShape(12.dp))
                    .border(1.dp, spec.borderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                QrIcon(tint = spec.iconTint, iconSize = 20)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = box.code,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextPrimary
                )
                Text(
                    text = toTitle(box.category),
                    style = MaterialTheme.typography.labelLarge,
                    color = LightTextSecondary
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Box options",
                        tint = LightTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                BoxOptionsMenu(
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false },
                    packedActionLabel = "Mark As Packed",
                    onAddItems = {
                        menuExpanded = false
                        onAddItemsClick()
                    },
                    onTogglePackedState = {
                        menuExpanded = false
                        onMarkAsUnpackedClick(box.code)
                    },
                    onViewQrCode = {
                        menuExpanded = false
                        onViewQrCodeClick(
                            box.boxUuid,
                            box.code,
                            box.roomLabel.ifBlank { toTitle(box.category) }
                        )
                    },
                    onEditBox = {
                        menuExpanded = false
                        onEditBoxClick(box.code)
                    },
                    onDeleteBox = {
                        menuExpanded = false
                        onDeleteBoxClick(box.code)
                    }
                )
            }
        }
    }
}

@Composable
private fun BoxOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    packedActionLabel: String,
    onAddItems: () -> Unit,
    onTogglePackedState: () -> Unit,
    onViewQrCode: () -> Unit,
    onEditBox: () -> Unit,
    onDeleteBox: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = LightSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, LightBorder),
        shadowElevation = 6.dp
    ) {
        MenuActionRow(
            text = "Add Items",
            icon = Icons.Outlined.Add,
            tint = LightTextPrimary,
            onClick = onAddItems
        )
        MenuActionRow(
            text = packedActionLabel,
            icon = Icons.Outlined.Inventory2,
            tint = LightTextPrimary,
            onClick = onTogglePackedState
        )
        MenuActionRow(
            text = "View QR Code",
            icon = Icons.Outlined.Visibility,
            tint = LightTextPrimary,
            onClick = onViewQrCode
        )
        MenuActionRow(
            text = "Modify Box Information",
            icon = Icons.Outlined.Edit,
            tint = LightTextPrimary,
            onClick = onEditBox
        )
        MenuActionRow(
            text = "Delete Box",
            icon = Icons.Outlined.DeleteOutline,
            tint = MaterialTheme.colorScheme.error,
            onClick = onDeleteBox
        )
    }
}

@Composable
private fun MenuActionRow(
    text: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = tint
        )
    }
}

@Composable
private fun ViewQrCodeDialog(
    boxUuid: String,
    boxId: String,
    roomLabel: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val qrBitmap = remember(boxUuid) {
        generateQrBitmapOrNull(
            value = boxUuid,
            size = 260,
            darkColor = android.graphics.Color.parseColor("#1565C0"),
            lightColor = android.graphics.Color.TRANSPARENT
        )
    }
    val qrAndroidBitmap = remember(qrBitmap) { qrBitmap?.asAndroidBitmap() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 102.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.02f))
                            .border(2.dp, Primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = qrBitmap,
                                contentDescription = "QR for $boxId",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        } else {
                            Text(
                                text = "QR",
                                style = MaterialTheme.typography.labelLarge,
                                color = Primary
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = boxId,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = roomLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LightTextSecondary
                                )
                                Text(
                                    text = "Created ${java.text.SimpleDateFormat("h:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date())}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LightTextSecondary
                                )
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(boxId))
                                    Toast.makeText(context, "Box ID copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy box ID",
                                    tint = LightTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QrActionButton(
                                text = "Download",
                                icon = Icons.Outlined.Download,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val bitmap = qrAndroidBitmap
                                    if (bitmap == null) {
                                        Toast.makeText(context, "QR image not ready", Toast.LENGTH_SHORT).show()
                                        return@QrActionButton
                                    }

                                    coroutineScope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            saveQrCodeToDownloads(context, boxId, bitmap)
                                        }
                                        result
                                            .onSuccess { fileName ->
                                                Toast.makeText(context, "Saved $fileName to Downloads", Toast.LENGTH_SHORT).show()
                                            }
                                            .onFailure { throwable ->
                                                Toast.makeText(context, throwable.message ?: "Failed to save QR", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            )
                            QrActionButton(
                                text = "Print",
                                icon = Icons.Outlined.Print,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val bitmap = qrAndroidBitmap
                                    if (bitmap == null) {
                                        Toast.makeText(context, "QR image not ready", Toast.LENGTH_SHORT).show()
                                        return@QrActionButton
                                    }

                                    printQrCode(context, boxId, bitmap)
                                        .onFailure { throwable ->
                                            Toast.makeText(context, throwable.message ?: "Unable to open print", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Tap outside to close",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun QrActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
            .background(LightBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LightTextPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = LightTextPrimary
        )
    }
}

private fun extractBoxId(boxCode: String): String {
    return boxCode.removePrefix("Box ").trim().ifBlank { boxCode }
}

private fun generateQrBitmapOrNull(
    value: String,
    size: Int,
    darkColor: Int = android.graphics.Color.BLACK,
    lightColor: Int = android.graphics.Color.WHITE
): ImageBitmap? {
    return runCatching {
        val bits = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bits[x, y]) darkColor else lightColor)
            }
        }
        bitmap.asImageBitmap()
    }.getOrNull()
}

private fun saveQrCodeToDownloads(
    context: Context,
    boxId: String,
    bitmap: Bitmap
): Result<String> {
    return runCatching {
        val sanitizedId = boxId.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val fileName = "moveon_qr_${sanitizedId}_${System.currentTimeMillis()}.png"

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/MoveOn")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Could not create download entry")

        try {
            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IllegalStateException("Could not write QR image")
                }
            } ?: throw IllegalStateException("Could not open download output stream")

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            fileName
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }
}

private fun printQrCode(context: Context, boxId: String, bitmap: Bitmap): Result<Unit> {
    return runCatching {
        PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            colorMode = PrintHelper.COLOR_MODE_COLOR
        }.printBitmap("MoveOn_QR_$boxId", bitmap)
    }
}

private fun toTitle(category: MoveOnCategory): String {
    return when (category) {
        MoveOnCategory.LivingRoom -> "Living Room"
        MoveOnCategory.Bedroom -> "Bedroom"
        MoveOnCategory.Kitchen -> "Kitchen"
        MoveOnCategory.Bathroom -> "Bathroom"
        MoveOnCategory.Storage -> "Storage"
        MoveOnCategory.Office -> "Office"
    }
}
