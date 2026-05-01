package com.example.moveon.ui.features.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moveon.ui.theme.Accent
import com.example.moveon.ui.theme.LightBorder
import com.example.moveon.ui.theme.LightSurface
import com.example.moveon.ui.theme.LightSurfaceVariant
import com.example.moveon.ui.theme.LightTextPrimary
import com.example.moveon.ui.theme.LightTextSecondary
import com.example.moveon.ui.theme.Primary

data class BookServiceCardUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val recommended: Boolean = false
)

data class BookProviderCardUi(
    val id: String,
    val name: String,
    val initials: String,
    val rating: String,
    val ratingCount: String,
    val movesLabel: String,
    val etaLabel: String,
    val priceLabel: String
)

@Composable
fun BookStepHeader(
    title: String,
    subtitle: String,
    step: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LightSurface)
            .padding(top = 8.dp)
    ) {
        BookProgressStepper(currentStep = step)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LightTextPrimary
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextSecondary
                )
            }
        }
    }
}

@Composable
private fun BookProgressStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIndicator(step = 1, currentStep = currentStep, title = "Vehicle")
        StepConnector()
        StepIndicator(step = 2, currentStep = currentStep, title = "Provider")
        StepConnector()
        StepIndicator(step = 3, currentStep = currentStep, title = "Details")
    }
}

@Composable
private fun RowScope.StepConnector() {
    Spacer(
        modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(LightBorder)
    )
}

@Composable
private fun StepIndicator(step: Int, currentStep: Int, title: String) {
    val completed = currentStep > step
    val active = currentStep == step
    val bgColor = when {
        completed -> Primary
        active -> Primary
        else -> LightBorder
    }
    val contentColor = if (completed || active) Color.White else Color(0xFF666666)

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = LightTextPrimary,
            fontWeight = if (active || completed) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun BookServiceListCard(
    service: BookServiceCardUi,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFDDECF9) else LightSurface
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) Primary else LightBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFE8EDF3), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = service.iconEmoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Added slight spacing between the title row and subtitle
            ) {
                // Wrap Title and Recommended Pill in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = LightTextPrimary
                    )

                    if (service.recommended) {
                        Box(
                            modifier = Modifier
                                .background(Accent, RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Recommended",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = service.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextSecondary
                )
            }
        }
    }
}

@Composable
fun BookProviderListCard(
    provider: BookProviderCardUi,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) Primary else LightBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFFE8EDF3), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = provider.initials,
                            style = MaterialTheme.typography.titleSmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = LightTextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${provider.rating} (${provider.ratingCount})",
                                style = MaterialTheme.typography.titleMedium,
                                color = LightTextSecondary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalShipping,
                                    contentDescription = null,
                                    tint = LightTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = provider.movesLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WatchLater,
                                    contentDescription = null,
                                    tint = LightTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = provider.etaLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = provider.priceLabel,
                style = MaterialTheme.typography.labelLarge,
                color = Accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BookActionFooter(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (secondaryLabel != null && onSecondaryClick != null) {
                Text(
                    text = secondaryLabel,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(onClick = onSecondaryClick)
                        .padding(PaddingValues(vertical = 4.dp, horizontal = 6.dp)),
                    style = MaterialTheme.typography.labelLarge,
                    color = LightTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        color = if (enabled) Primary else LightBorder,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable(enabled = enabled, onClick = onPrimaryClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = primaryLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BookPillLabel(
    text: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = LightTextPrimary
        )
    }
}
