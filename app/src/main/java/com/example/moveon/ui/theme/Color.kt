package com.example.moveon.ui.theme

import androidx.compose.ui.graphics.Color

// ============ SEMANTIC BRAND COLORS ============
// Primary Action Color (unified Deep Blue across app)
val Primary = Color(0xFF1565C0)           // Deep Blue - used throughout screens

// Secondary & Success
val Secondary = Color(0xFF10B981)         // Cyber Mint - verified states, success
val Tertiary = Color(0xFF7C4DFF)          // Purple - AI/automation features

// Accents
val Accent = Color(0xFFFF6F00)           // Orange - urgent actions, QR scanning
val Success = Color(0xFF2E7D32)           // Deep Green - success highlights
val Warning = Color(0xFFF59E0B)          // Amber - cautionary elements
val Error = Color(0xFFEF4444)            // Error Red - error states
val ErrorDeep = Color(0xFFD32F2F)        // Deep Red - destructive actions

// ============ LIGHT THEME COLORS ============
// Backgrounds & Surfaces
val LightBackground = Color(0xFFFAFAFA)  // Off-white - main background
val LightSurface = Color(0xFFFFFFFF)     // White - elevated surfaces (cards)
val LightSurfaceVariant = Color(0xFFF5F5F5) // Very light gray - search bars, inputs

// Text & Content
val LightTextPrimary = Color(0xFF1C1B1F)     // Dark gray - primary text (headlines, body)
val LightTextSecondary = Color(0xFF757575)   // Medium gray - secondary text, subtitles
val LightTextTertiary = Color(0xFFA0A0A0)    // Light gray - disabled/tertiary text

// Borders & Dividers
val LightBorder = Color(0xFFE0E0E0)       // Light gray - card borders, dividers
val LightBorderLight = Color(0xFFEDEDED) // Lighter gray - subtle dividers

// ============ DARK THEME COLORS ============
// Backgrounds & Surfaces
val DarkBackground = Color(0xFF0F172A)    // Deep Slate - primary background
val DarkSurface = Color(0xFF1E293B)       // Slightly raised slate - cards, elevated surfaces
val DarkSurfaceVariant = Color(0xFF334155) // Darker slate - alternative surfaces

// Text & Content
val DarkTextPrimary = Color(0xFFF8FAFC)    // Light gray - primary text on dark backgrounds
val DarkTextSecondary = Color(0xFF94A3B8)  // Slate - secondary text
val DarkTextTertiary = Color(0xFF64748B)   // Darker slate - disabled/tertiary text

// Borders & Dividers
val DarkBorder = Color(0xFF334155)        // Dark slate - borders on dark backgrounds
val DarkBorderSubtle = Color(0xFF1E293B)  // Very dark - subtle dividers

// ============ GLASSMORPHISM & EFFECTS ============
val GlassWhiteLight = Color(0x1AFFFFFF)   // 10% white - light glass overlay
val GlassWhiteMedium = Color(0x33FFFFFF)  // 20% white - medium glass overlay
val GlassWhiteStrong = Color(0x66FFFFFF)  // 40% white - strong glass overlay

// Glass overlays for dark theme
val GlassDarkLight = Color(0x1A1F2937)    // 10% dark - dark glass overlay
val GlassDarkMedium = Color(0x331F2937)   // 20% dark - medium dark glass overlay

// ============ CATEGORY TINT COLORS (for Onboarding, inventory categorization) ============
val BlueTint = Color(0xFF141565C0)        // 8% blue tint
val OrangeTint = Color(0xFF14FF6F00)      // 8% orange tint
val GreenTint = Color(0xFF142E7D32)       // 8% green tint
val PurpleTint = Color(0xFF147C4DFF)      // 8% purple tint

// ============ LEGACY COMPATIBILITY (kept for gradual migration) ============
val ElectricIndigo = Color(0xFF6366F1)    // Old primary (kept for reference)
val CyberMint = Color(0xFF10B981)         // Kept for compatibility
val DeepSlate = Color(0xFF0F172A)         // Kept for compatibility