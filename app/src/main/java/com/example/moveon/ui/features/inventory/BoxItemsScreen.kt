package com.example.moveon.ui.features.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.moveon.domain.model.Item
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.components.MoveOnSwipeActionBox
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.ErrorDeep
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary

@Composable
fun BoxItemsScreen(
    boxUuid: String,
    scannedFromQr: Boolean,
    onBack: () -> Unit,
    onAddItem: (boxUuid: String, boxId: String) -> Unit,
    onScanAnotherBox: () -> Unit,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    boxItemsViewModel: BoxItemsViewModel = hiltViewModel()
) {
    val box = inventoryViewModel.uiState.value.storedBoxes.firstOrNull { it.boxUuid == boxUuid }
    val itemsState = boxItemsViewModel.uiState.value
    val context = LocalContext.current
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var deleteItem by remember { mutableStateOf<Item?>(null) }
    var itemInfo by remember { mutableStateOf<Item?>(null) }
    var showPackedBoxAddConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        boxItemsViewModel.eventFlow.collect { event ->
            when (event) {
                is BoxItemsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(box?.boxId) {
        box?.let { boxItemsViewModel.observeItems(it.boxId) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(LightBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF1976D2))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = box?.boxId ?: "Unknown Box",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (box != null) "${box.label} • 15m3" else "Box not found",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (box == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, LightBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "We could not find a box matching this UUID.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightTextSecondary
                        )
                    }
                }
            } else if (itemsState.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, LightBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Box Items",
                                style = MaterialTheme.typography.titleMedium,
                                color = LightTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Items for this box will show here once they are added.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(itemsState.items, key = { it.id }) { item ->
                    MoveOnSwipeActionBox(
                        onSwipeStart = { editingItem = item },
                        onSwipeEnd = { deleteItem = item },
                        startBackgroundColor = Primary.copy(alpha = 0.12f),
                        endBackgroundColor = ErrorDeep.copy(alpha = 0.12f),
                        idleBackgroundColor = LightSurfaceVariant,
                        backgroundShape = RoundedCornerShape(16.dp),
                        startContent = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelLarge,
                                color = Primary
                            )
                        },
                        endContent = {
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.labelLarge,
                                color = ErrorDeep
                            )
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = ErrorDeep,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp)
                                .clickable { itemInfo = item },
                            colors = CardDefaults.cardColors(containerColor = LightSurface),
                            border = BorderStroke(1.dp, LightBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = "${item.name} thumbnail",
                                        modifier = Modifier
                                            .size(92.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                color = LightSurfaceVariant,
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(92.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                color = LightSurfaceVariant,
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Inventory2,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(92.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = LightTextPrimary
                                        )
                                        Text(
                                            text = "x${item.quantity}",
                                            fontSize = 28.sp,
                                            lineHeight = 28.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LightTextSecondary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = item.description.ifBlank { "No description" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LightTextSecondary,
                                            maxLines = 2
                                        )
                                    }

                                    if (item.isFragile) {
                                        Text(
                                            text = "Fragile",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Accent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (box != null) {
            MoveOnPillButton(
                text = if (itemsState.isSaving) "Saving..." else "Add Item",
                onClick = {
                    if (box.packed) {
                        showPackedBoxAddConfirmation = true
                    } else {
                        onAddItem(box.boxUuid, box.boxId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                background = Primary,
                enabled = !itemsState.isSaving
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MoveOnPillButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                background = LightSurface,
                textColor = LightTextPrimary
            )

            if (scannedFromQr) {
                MoveOnPillButton(
                    text = "Scan Another Box",
                    onClick = onScanAnotherBox,
                    modifier = Modifier.weight(2f),
                    background = Primary
                )
            }
        }

        editingItem?.let { item ->
            AddItemDialog(
                imageUri = item.imageUrl.takeIf { it.isNotBlank() },
                onDismiss = { editingItem = null },
                onSave = { name, quantity, description, isFragile, imageUrl ->
                    boxItemsViewModel.updateItem(
                        item.copy(
                            name = name,
                            quantity = quantity,
                            description = description,
                            isFragile = isFragile,
                            imageUrl = imageUrl
                        )
                    )
                    editingItem = null
                },
                initialName = item.name,
                initialQuantity = item.quantity,
                initialDescription = item.description,
                initialIsFragile = item.isFragile,
                title = "Edit Item",
                confirmLabel = "Save Changes"
            )
        }

        deleteItem?.let { item ->
            DeleteItemConfirmationSheet(
                item = item,
                onDismiss = { deleteItem = null },
                onConfirm = {
                    boxItemsViewModel.deleteItem(item)
                    deleteItem = null
                }
            )
        }

        itemInfo?.let { item ->
            ItemInfoDialog(
                item = item,
                onDismiss = { itemInfo = null }
            )
        }

        if (showPackedBoxAddConfirmation && box != null) {
            PackedBoxAddConfirmationSheet(
                boxId = box.boxId,
                onDismiss = { showPackedBoxAddConfirmation = false },
                onConfirm = {
                    showPackedBoxAddConfirmation = false
                    onAddItem(box.boxUuid, box.boxId)
                }
            )
        }
    }
}

@Composable
private fun ItemInfoDialog(
    item: Item,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Item Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = "${item.name} image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = LightSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = LightSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextPrimary
                )

                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary
                )

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )
                }

                Text(
                    text = if (item.isFragile) "Fragile" else "Not fragile",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isFragile) Accent else LightTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                MoveOnPillButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    background = Primary
                )
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteItemConfirmationSheet(
    item: Item,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val description = "This will permanently remove ${item.name} from this box."

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
                        text = "Delete Item?",
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
                MoveOnPillButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    background = LightSurface,
                    textColor = LightTextPrimary
                )
                MoveOnPillButton(
                    text = "Delete",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    background = ErrorDeep
                )
            }
        }
    }
}
