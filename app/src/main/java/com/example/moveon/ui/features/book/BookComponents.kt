package com.example.moveon.ui.features.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.moveon.ui.theme.Success

data class BookServiceCardUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val capacityLabel: String,
    val etaLabel: String,
    val baseRateLabel: String,
    val icon: ImageVector = Icons.Outlined.LocalShipping
)

data class BookProviderCardUi(
    val id: String,
    val name: String,
    val ratingLabel: String,
    val fleetLabel: String,
    val priceLabel: String,
    val etaLabel: String
)

@Composable
fun BookStepHeader(
    title: String,
    subtitle: String,
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSteps <= 0) 0f else step.toFloat() / totalSteps.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = LightTextPrimary
            )
            Text(
                text = "Step $step/$totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = LightTextSecondary
            )
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LightTextSecondary
        )

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Primary,
            trackColor = LightSurfaceVariant
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
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Primary else LightBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = Primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextPrimary
                )
                Text(
                    text = service.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookPillLabel(text = service.capacityLabel, background = LightSurfaceVariant)
                    BookPillLabel(text = service.etaLabel, background = LightSurfaceVariant)
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = service.baseRateLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold
                )

                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Success
                    )
                }
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
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Primary else LightBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = LightTextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = provider.ratingLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextSecondary
                            )
                        }
                    }
                }

                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Success
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BookPillLabel(text = provider.fleetLabel, background = LightSurfaceVariant)
                BookPillLabel(text = provider.etaLabel, background = LightSurfaceVariant)
            }

            Text(
                text = provider.priceLabel,
                style = MaterialTheme.typography.titleSmall,
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
