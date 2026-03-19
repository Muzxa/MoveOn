package com.example.moveon.ui.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.theme.GlassWhiteLight
import com.example.moveon.ui.theme.GlassWhiteMedium
import com.example.moveon.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onResolveSession: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onResolveSession()
    }

    val dotTransition = rememberInfiniteTransition(label = "SplashDotTransition")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary, Primary.copy(alpha = 0.75f))
                )
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glass icon container
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassWhiteLight)
                    .border(
                        width = 1.dp,
                        color = GlassWhiteMedium,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "MoveOn",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(Modifier.height(32.dp))

            // Three dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val scale = dotTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 2000
                                0.9f at (index * 120)
                                1.2f at (250 + index * 120)
                                0.9f at (550 + index * 120)
                            },
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "SplashDotScale$index"
                    )

                    val alpha = dotTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 2000
                                0.35f at (index * 120)
                                0.9f at (250 + index * 120)
                                0.35f at (550 + index * 120)
                            },
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "SplashDotAlpha$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(scale.value)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = alpha.value), CircleShape)
                    )
                }
            }
        }

        // Version text at bottom
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
