package com.example.moveon.ui.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPageData(
    val emoji: String,
    val badgeIcon: ImageVector,
    val badgeColor: Color,
    val bgTint: Color,
    val title: String,
    val subtitle: String,
    val description: String
)

private val onboardingPages = listOf(
    OnboardingPageData(
        emoji = "🚚",
        badgeIcon = Icons.Outlined.LocalShipping,
        badgeColor = Color(0xFF1565C0),
        bgTint = Color(0xFF1565C0).copy(alpha = 0.08f),
        title = "MoveOn",
        subtitle = "Your single pane of glass for moving",
        description = "Book verified transport and manage your entire move from one powerful app."
    ),
    OnboardingPageData(
        emoji = "📍",
        badgeIcon = Icons.Outlined.LocationOn,
        badgeColor = Color(0xFFFF6F00),
        bgTint = Color(0xFFFF6F00).copy(alpha = 0.08f),
        title = "Fixed-Rate Prices",
        subtitle = "Because who has time for haggling?",
        description = "Find the right truck for your move. Track your driver in real time with GPS and follow the journey stress-free."
    ),
    OnboardingPageData(
        emoji = "📦",
        badgeIcon = Icons.Outlined.QrCode2,
        badgeColor = Color(0xFF2E7D32),
        bgTint = Color(0xFF2E7D32).copy(alpha = 0.08f),
        title = "Smart Inventory System",
        subtitle = "Never lose track of your belongings",
        description = "Scan the QR codes on your boxes and let smart tech identify what's inside. Check your digital inventory anytime, anywhere."
    ),
    OnboardingPageData(
        emoji = "🤖",
        badgeIcon = Icons.Outlined.AutoAwesome,
        badgeColor = Color(0xFF7C4DFF),
        bgTint = Color(0xFF7C4DFF).copy(alpha = 0.08f),
        title = "AI-Powered Estimation",
        subtitle = "Know before you go",
        description = "Point your camera at the room and let smart tech estimate the boxes you'll need. We'll even suggest the right truck size."
    )
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val page = onboardingPages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Skip button — top right
        Text(
            text = "Skip",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onGetStarted() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        // Center content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustration: emoji square + circular badge overlapping bottom-center
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(168.dp)
            ) {
                // Emoji tinted background square (128×128dp)
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(page.bgTint)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = page.emoji, fontSize = 60.sp)
                }

                // Colored badge circle (64×64dp) centered, overlapping bottom of square
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.BottomCenter)
                        .clip(CircleShape)
                        .background(page.badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.badgeIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Title — 36sp Bold
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle — 12sp SemiBold
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Description — 12sp Regular gray
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }

        // Bottom controls: indicator dots + action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page indicator dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { i ->
                    if (i > 0) Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (i == currentPage) 32.dp else 8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (i == currentPage) Color(0xFF1565C0) else Color(0xFFE0E0E0)
                            )
                    )
                }
            }

            // Page 0: full-width Next button only
            if (currentPage == 0) {
                Button(
                    onClick = { currentPage = 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Next  \u203A",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            } else {
                // Pages 1–3: Back + Next (or Get Started on last page)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        modifier = Modifier
                            .width(116.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFAFAFA),
                            contentColor = Color(0xFF1C1B1F)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        onClick = {
                            if (currentPage < 3) currentPage++
                            else onGetStarted()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (currentPage < 3) "Next  \u203A" else "Get Started  \u203A",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
