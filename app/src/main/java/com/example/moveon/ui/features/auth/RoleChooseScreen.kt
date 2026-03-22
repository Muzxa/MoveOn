package com.example.moveon.ui.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moveon.domain.model.UserRole
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary

@Composable
fun RoleChooseScreen(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Text(
                text = "Choose Your Role",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "How will you be using MoveOn?",
                color = LightTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            RoleCard(
                title = "I need to move",
                subtitle = "Customer",
                description = "Book verified transport, track your move, and manage inventory digitally.",
                isSelected = selectedRole == UserRole.USER,
                accentColor = Primary,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                onClick = { onRoleSelected(UserRole.USER) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            RoleCard(
                title = "I provide transport",
                subtitle = "Service Provider",
                description = "Join our verified driver network and grow your moving business.",
                isSelected = selectedRole == UserRole.PROVIDER,
                accentColor = Color(0xFFFF7A00),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFFFF7A00)
                    )
                },
                onClick = { onRoleSelected(UserRole.PROVIDER) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Back", color = Color(0xFF202124))
                }

                Button(
                    onClick = onNext,
                    enabled = selectedRole != null,
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.45f)
                    )
                ) {
                    Text("Next >", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = LightSurface,
        border = BorderStroke(if (isSelected) 1.3.dp else 1.dp, if (isSelected) accentColor else LightBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontWeight = FontWeight.SemiBold)
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(text = subtitle, fontSize = 12.sp, color = accentColor)
                }
                Text(
                    text = description,
                    color = LightTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

