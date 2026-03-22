package com.example.moveon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DashboardTab(
    val label: String,
    val route: String,
    val icon: ImageVector
) {
    Home(label = "Home", route = "home", icon = Icons.Outlined.Home),
    Book(label = "Book", route = "book", icon = Icons.Outlined.LocalShipping),
    Inventory(label = "Inventory", route = "inventory", icon = Icons.Outlined.Inventory2),
    Profile(label = "Profile", route = "profile", icon = Icons.Outlined.PersonOutline)
}

@Composable
fun MoveOnBottomBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(Color.White)
    ) {
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val color = if (selected) Color(0xFF1565C0) else Color(0xFF757575)

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
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = tab.label,
                        color = color,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(4.dp)
                                .background(Color(0xFF1565C0), CircleShape)
                        )
                    }
                }
            }
        }
    }
}
