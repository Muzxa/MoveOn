package com.example.moveon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.moveon.ui.theme.Typography

// ============ LIGHT COLOR SCHEME ============
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBEDEFF),
    onPrimaryContainer = Color(0xFF001D3B),
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA5F0D6),
    onSecondaryContainer = Color(0xFF002619),
    
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF21005E),
    
    background = LightBackground,
    onBackground = LightTextPrimary,
    
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFDDADA),
    onErrorContainer = Color(0xFF410E0B),
    
    outline = LightBorder,
    outlineVariant = LightBorderLight
)

// ============ DARK COLOR SCHEME ============
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFBEDEFF),
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF084C3E),
    onSecondaryContainer = Color(0xFFA5F0D6),
    
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5D3FA0),
    onTertiaryContainer = Color(0xFFEADDFF),
    
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDADA),
    
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle
)

// ============ MOVE ON THEME COMPOSABLE ============
@Composable
fun MoveOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}