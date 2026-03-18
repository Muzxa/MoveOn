package com.example.moveon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class MoveOnCategory {
    LivingRoom,
    Bedroom,
    Kitchen,
    Bathroom,
    Storage,
    Office
}

data class CategoryIconSpec(
    val icon: ImageVector,
    val iconTint: Color,
    val containerColor: Color,
    val borderColor: Color
)

fun MoveOnCategory.iconSpec(): CategoryIconSpec {
    return when (this) {
        MoveOnCategory.LivingRoom -> CategoryIconSpec(
            icon = Icons.Outlined.HomeWork,
            iconTint = Color(0xFF1565C0),
            containerColor = Color(0x141565C0),
            borderColor = Color(0x331565C0)
        )

        MoveOnCategory.Bedroom -> CategoryIconSpec(
            icon = Icons.Outlined.Hotel,
            iconTint = Color(0xFF7C4DFF),
            containerColor = Color(0x147C4DFF),
            borderColor = Color(0x337C4DFF)
        )

        MoveOnCategory.Kitchen -> CategoryIconSpec(
            icon = Icons.Outlined.Kitchen,
            iconTint = Color(0xFFFF6F00),
            containerColor = Color(0x14FF6F00),
            borderColor = Color(0x33FF6F00)
        )

        MoveOnCategory.Bathroom -> CategoryIconSpec(
            icon = Icons.Outlined.Bathtub,
            iconTint = Color(0xFF26A69A),
            containerColor = Color(0x1426A69A),
            borderColor = Color(0x3326A69A)
        )

        MoveOnCategory.Storage -> CategoryIconSpec(
            icon = Icons.Outlined.Inventory2,
            iconTint = Color(0xFF2E7D32),
            containerColor = Color(0x142E7D32),
            borderColor = Color(0x332E7D32)
        )

        MoveOnCategory.Office -> CategoryIconSpec(
            icon = Icons.Outlined.WorkOutline,
            iconTint = Color(0xFF546E7A),
            containerColor = Color(0x14546E7A),
            borderColor = Color(0x33546E7A)
        )
    }
}

@Composable
fun CategoryIcon(
    category: MoveOnCategory,
    modifier: Modifier = Modifier,
    iconSize: Int = 24,
    rounded: Int = 12
) {
    val spec = category.iconSpec()
    Box(
        modifier = modifier
            .background(spec.containerColor, RoundedCornerShape(rounded.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = spec.icon,
            contentDescription = category.name,
            tint = spec.iconTint,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Composable
fun BoxIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Int = 24
) {
    Icon(
        imageVector = Icons.Outlined.Inventory2,
        contentDescription = "Box",
        tint = tint,
        modifier = modifier.size(iconSize.dp)
    )
}

@Composable
fun QrIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Int = 24
) {
    Icon(
        imageVector = Icons.Outlined.QrCode2,
        contentDescription = "QR",
        tint = tint,
        modifier = modifier.size(iconSize.dp)
    )
}
