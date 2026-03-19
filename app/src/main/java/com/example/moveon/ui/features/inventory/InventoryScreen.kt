package com.example.moveon.ui.features.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moveon.ui.components.BoxIcon
import com.example.moveon.ui.components.CategoryIcon
import com.example.moveon.ui.components.DashboardTab
import com.example.moveon.ui.components.MoveOnBottomBar
import com.example.moveon.ui.components.MoveOnCategory
import com.example.moveon.ui.components.MoveOnPillButton
import com.example.moveon.ui.components.MoveOnStatCard
import com.example.moveon.ui.components.QrIcon
import com.example.moveon.ui.components.iconSpec

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
    onAddBoxClick: () -> Unit = {}
) {
    val packedBoxes = listOf(
        PackedBoxUi("Box LR-001", MoveOnCategory.LivingRoom, "15m³", 5, "2 hours ago"),
        PackedBoxUi("Box BR-001", MoveOnCategory.Bedroom, "15m³", 4, "3 hours ago"),
        PackedBoxUi("Box KT-001", MoveOnCategory.Kitchen, "15m³", 7, "5 hours ago")
    )

    val unpackedBoxes = listOf(
        UnpackedBoxUi("Box LR-002", MoveOnCategory.LivingRoom),
        UnpackedBoxUi("Box BR-002", MoveOnCategory.Bedroom),
        UnpackedBoxUi("Box BR-003", MoveOnCategory.Bedroom),
        UnpackedBoxUi("Box KT-002", MoveOnCategory.Kitchen),
        UnpackedBoxUi("Box ST-002", MoveOnCategory.Storage)
    )

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
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
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "Digital tracking for your move",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF757575)
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MoveOnStatCard(value = "8", label = "Boxes", modifier = Modifier.weight(1f))
                    MoveOnStatCard(value = "17", label = "Items", modifier = Modifier.weight(1f))
                    MoveOnStatCard(
                        value = "8",
                        label = "Fragile",
                        modifier = Modifier.weight(1f),
                        valueColor = Color(0xFFFF6F00)
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
                        background = Color(0xFFFF6F00)
                    )
                    MoveOnPillButton(
                        text = "Add Box",
                        onClick = onAddBoxClick,
                        modifier = Modifier.weight(1f),
                        background = Color(0xFF1565C0)
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Packed Boxes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1C1B1F)
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
                    color = Color(0xFF1C1B1F)
                )
            }

            items(unpackedBoxes.size) { index ->
                UnpackedBoxCard(box = unpackedBoxes[index])
            }

            item {
                Spacer(Modifier.height(8.dp))
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
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = Color(0xFF757575),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Search boxes or items...",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun PackedBoxCard(box: PackedBoxUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = "${toTitle(box.category)} • ${box.volume}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF757575)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = null,
                        tint = Color(0xFF757575),
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
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${box.itemCount} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1C1B1F)
                            )
                        }
                        Text(
                            text = box.updatedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF757575),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = toTitle(box.category),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF757575)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF757575),
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
