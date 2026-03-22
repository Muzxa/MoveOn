package com.example.moveon.ui.features.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
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

        if (box == null) {
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
        } else {
            if (itemsState.items.isEmpty()) {
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
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(itemsState.items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LightSurface),
                            border = BorderStroke(1.dp, LightBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                        style = MaterialTheme.typography.labelLarge,
                                        color = LightTextSecondary
                                    )
                                }

                                if (item.description.isNotBlank()) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightTextSecondary
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

        Spacer(modifier = Modifier.weight(1f))

        if (box != null) {
            MoveOnPillButton(
                text = if (itemsState.isSaving) "Saving..." else "Add Item",
                onClick = { onAddItem(box.boxUuid, box.boxId) },
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
    }
}
