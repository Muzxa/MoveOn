package com.example.moveon.ui.features.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.window.Dialog
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

private data class PackedBoxUi(
    val code: String,
    val category: MoveOnCategory,
    val volume: String,
    val itemCount: Int,
    val updatedAt: String
)

private data class UnpackedBoxUi(
    val code: String,
    val category: MoveOnCategory
)

@Composable
fun InventoryScreen(
    onTabSelected: (DashboardTab) -> Unit = {},
    onScanBoxClick: () -> Unit = {},
    onAddBoxClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is InventoryUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val packedBoxes = listOf(
        PackedBoxUi("Box LR-001", MoveOnCategory.LivingRoom, "15m³", 5, "2 hours ago"),
        PackedBoxUi("Box BR-001", MoveOnCategory.Bedroom, "15m³", 4, "3 hours ago"),
        PackedBoxUi("Box KT-001", MoveOnCategory.Kitchen, "15m³", 7, "5 hours ago")
    )

    val unpackedBoxes = state.createdBoxes.map {
        UnpackedBoxUi(code = "Box ${it.id}", category = it.category)
    } + listOf(
        UnpackedBoxUi("Box LR-002", MoveOnCategory.LivingRoom),
        UnpackedBoxUi("Box BR-002", MoveOnCategory.Bedroom),
        UnpackedBoxUi("Box BR-003", MoveOnCategory.Bedroom),
        UnpackedBoxUi("Box KT-002", MoveOnCategory.Kitchen),
        UnpackedBoxUi("Box ST-002", MoveOnCategory.Storage)
    )

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
                        value = (8 + state.createdBoxes.size).toString(),
                        label = "Boxes",
                        modifier = Modifier.weight(1f)
                    )
                    MoveOnStatCard(value = "17", label = "Items", modifier = Modifier.weight(1f))
                    MoveOnStatCard(
                        value = "8",
                        label = "Fragile",
                        modifier = Modifier.weight(1f),
                        valueColor = Accent
                    )
                }

                Spacer(Modifier.height(12.dp))

                SearchBarPlaceholder()

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

            items(packedBoxes.size) { index ->
                PackedBoxCard(box = packedBoxes[index])
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Unpacked Boxes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LightTextPrimary
                )
            }

            items(unpackedBoxes.size) { index ->
                UnpackedBoxCard(box = unpackedBoxes[index])
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

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun SearchBarPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(LightSurfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = LightTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Search boxes or items...",
            style = MaterialTheme.typography.labelLarge,
            color = LightTextSecondary
        )
    }
}

@Composable
private fun PackedBoxCard(box: PackedBoxUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = null,
                        tint = LightTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
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
                        Text(
                            text = box.updatedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = LightTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnpackedBoxCard(box: UnpackedBoxUi) {
    val spec = box.category.iconSpec()
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = LightTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
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
