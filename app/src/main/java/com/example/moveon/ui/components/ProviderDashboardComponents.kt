package com.example.moveon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary

internal enum class ProviderDashboardTab(
    val label: String,
    val icon: ImageVector
) {
    Dashboard("Dashboard", Icons.Outlined.Home),
    Vehicles("Vehicles", Icons.Outlined.LocalShipping),
    Jobs("Jobs", Icons.Outlined.Inventory2),
    Profile("Profile", Icons.Outlined.PersonOutline)
}

@Composable
internal fun ProviderBottomBar(
    selectedTab: ProviderDashboardTab,
    onTabSelected: (ProviderDashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LightSurface)
    ) {
        HorizontalDivider(color = LightBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProviderDashboardTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val color = if (selected) Primary else LightTextSecondary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = color
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(4.dp)
                                .background(Primary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProviderQuickActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LightBackground),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = LightTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
internal fun ProviderGlassStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
internal fun ProviderSectionHeader(
    title: String,
    trailingText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = LightTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
internal fun ProviderMetricCard(
    icon: ImageVector,
    amount: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = LightTextSecondary
            )
        }
    }
}

@Composable
internal fun ProviderKpiCard(
    value: String,
    label: String,
    valueColor: Color = LightTextPrimary,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (leadingIcon != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = valueColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = valueColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = LightTextSecondary
            )
        }
    }
}

@Composable
internal fun ProviderTag(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun ProviderToBeImplemented(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "to be implemented",
            style = MaterialTheme.typography.titleMedium,
            color = LightTextSecondary
        )
    }
}

@Composable
internal fun ProviderHeaderGradient(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, Primary.copy(alpha = 0.9f))
                )
            )
    ) {
        content()
    }
}

@Composable
internal fun ProviderRoutePoint(
    text: String,
    isPickup: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isPickup) 10.dp else 12.dp)
                .background(
                    color = if (isPickup) Primary else Color(0xFF4CAF50),
                    shape = CircleShape
                )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextPrimary,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
internal fun ProviderTrailingChevron(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Outlined.ChevronRight,
        contentDescription = null,
        tint = LightTextSecondary,
        modifier = modifier.size(16.dp)
    )
}
