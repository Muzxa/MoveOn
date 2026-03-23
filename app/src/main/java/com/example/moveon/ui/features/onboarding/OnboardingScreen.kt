package com.example.moveon.ui.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.BlueTint
import com.example.moveon.ui.theme.GreenTint
import com.example.moveon.ui.theme.LightBackground
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.OrangeTint
import com.example.moveon.ui.theme.Primary
import com.example.moveon.ui.theme.PurpleTint
import com.example.moveon.ui.theme.Success
import com.example.moveon.ui.theme.Tertiary

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
        badgeColor = Primary,
        bgTint = BlueTint,
        title = "MoveOn",
        subtitle = "Your single pane of glass for moving",
        description = "Book verified transport and manage your entire move from one powerful app."
    ),
    OnboardingPageData(
        emoji = "📍",
        badgeIcon = Icons.Outlined.LocationOn,
        badgeColor = Accent,
        bgTint = OrangeTint,
        title = "Fixed-Rate Prices",
        subtitle = "Because who has time for haggling?",
        description = "Find the right truck for your move. Track your driver in real time with GPS and follow the journey stress-free."
    ),
    OnboardingPageData(
        emoji = "📦",
        badgeIcon = Icons.Outlined.QrCode2,
        badgeColor = Success,
        bgTint = GreenTint,
        title = "Smart Inventory System",
        subtitle = "Never lose track of your belongings",
        description = "Scan the QR codes on your boxes and let smart tech identify what's inside. Check your digital inventory anytime, anywhere."
    ),
    OnboardingPageData(
        emoji = "🤖",
        badgeIcon = Icons.Outlined.AutoAwesome,
        badgeColor = Tertiary,
        bgTint = PurpleTint,
        title = "AI-Powered Estimation",
        subtitle = "Know before you go",
        description = "Point your camera at the room and let smart tech estimate the boxes you'll need. We'll even suggest the right truck size."
    )
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val page = onboardingPages[currentPage]

    val heroFloatTransition = rememberInfiniteTransition(label = "OnboardingHeroFloat")
    val heroFloatOffset by heroFloatTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                -2f at 0
                2f at 900
                -2f at 1800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "OnboardingHeroOffset"
    )

    fun changePage(targetPage: Int) {
        currentPage = targetPage.coerceIn(0, onboardingPages.lastIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Text(
            text = "Skip",
            style = MaterialTheme.typography.labelMedium,
            color = LightTextSecondary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onGetStarted() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 3 } + fadeOut())
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            label = "OnboardingContentTransition"
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .height(168.dp)
                        .padding(top = (heroFloatOffset + 2f).dp)
                ) {
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

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.BottomCenter)
                            .scale(1f + (heroFloatOffset / 30f))
                            .clip(CircleShape)
                            .background(page.badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.badgeIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = LightTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = LightTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { i ->
                    if (i > 0) Spacer(Modifier.width(8.dp))
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (i == currentPage) 32.dp else 8.dp,
                        label = "IndicatorWidth$i"
                    )
                    val indicatorColor by animateColorAsState(
                        targetValue = if (i == currentPage) Primary else LightBorder,
                        label = "IndicatorColor$i"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(indicatorWidth)
                            .clip(RoundedCornerShape(100.dp))
                            .background(indicatorColor)
                    )
                }
            }

            if (currentPage == 0) {
                Button(
                    onClick = { changePage(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Next  ›",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { changePage(currentPage - 1) },
                        modifier = Modifier
                            .width(116.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LightBackground,
                            contentColor = LightTextPrimary
                        ),
                        border = BorderStroke(1.dp, LightBorder),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        onClick = {
                            if (currentPage < onboardingPages.lastIndex) {
                                changePage(currentPage + 1)
                            } else {
                                onGetStarted()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (currentPage < onboardingPages.lastIndex) "Next  ›" else "Get Started  ›",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
