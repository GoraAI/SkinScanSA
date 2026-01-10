package com.skinscan.sa.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Glow Guide Color Palette
 *
 * Premium dark mode design with rose gold accents
 * Designed for melanin-rich skin care app
 */

// ===========================================
// PRIMARY BRAND COLORS - Rose Gold
// ===========================================
val RoseGold = Color(0xFFE0BFB8)
val RoseGoldDark = Color(0xFFC8A29A)
val RoseGoldLight = Color(0xFFF0D5D0)
val RoseGold20 = Color(0xFFE0BFB8).copy(alpha = 0.2f)
val RoseGold30 = Color(0xFFE0BFB8).copy(alpha = 0.3f)

// ===========================================
// SECONDARY BRAND COLORS - Champagne
// ===========================================
val Champagne = Color(0xFFF3E5AB)
val ChampagneDark = Color(0xFFE5D49A)
val ChampagneLight = Color(0xFFFFF8DC)

// ===========================================
// ACCENT COLORS - Teal (for progress/success)
// ===========================================
val TealAccent = Color(0xFF64FFDA)
val TealAccentDark = Color(0xFF4DB6AC)
val TealAccent20 = Color(0xFF64FFDA).copy(alpha = 0.2f)

// Legacy Teal (kept for backwards compatibility)
val Teal600 = Color(0xFF00897B)
val Teal700 = Color(0xFF00796B)
val Teal500 = Color(0xFF009688)
val Teal50 = Color(0xFFE0F2F1)

// ===========================================
// DARK MODE BACKGROUNDS
// ===========================================
val DarkBackground = Color(0xFF121212)
val SurfaceBlack = Color(0xFF1E1E1E)
val DarkSurface = Color(0xFF1E1E1E)
val SurfaceDark2 = Color(0xFF242424)
val SurfaceDark3 = Color(0xFF2C2C2C)

// ===========================================
// GLASSMORPHISM COLORS
// ===========================================
val GlassSurface = Color(0xFF2C2C2C).copy(alpha = 0.6f)
val GlassSurfaceLight = Color(0xFF3C3C3C).copy(alpha = 0.5f)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.1f)
val GlassBorderLight = Color(0xFFFFFFFF).copy(alpha = 0.15f)

// ===========================================
// TEXT COLORS
// ===========================================
val TextWhite = Color(0xFFF5F5F5)
val TextPrimaryDark = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB3B3B3)
val TextSecondaryDark = Color(0xFFB3B3B3)
val TextDisabled = Color(0xFF6E6E6E)
val TextPrimary = Color(0xFF212121)

// ===========================================
// FUNCTIONAL COLORS - Success
// ===========================================
val Green600 = Color(0xFF43A047)
val Green700 = Color(0xFF388E3C)
val Green50 = Color(0xFFE8F5E9)
val SuccessGreen = Color(0xFF4CAF50)

// ===========================================
// FUNCTIONAL COLORS - Error/Warning
// ===========================================
val Red600 = Color(0xFFE53935)
val Red700 = Color(0xFFD32F2F)
val Red50 = Color(0xFFFFEBEE)
val ErrorRed = Color(0xFFCF6679)
val WarningAmber = Color(0xFFFFB74D)

// ===========================================
// LEGACY COLORS (for backwards compatibility)
// ===========================================
val DeepPurple700 = Color(0xFF512DA8)
val DeepPurple800 = Color(0xFF4527A0)
val DeepPurple50 = Color(0xFFEDE7F6)

val Coral400 = Color(0xFFFF7043)
val Coral500 = Color(0xFFFF5722)
val Coral50 = Color(0xFFFFE5E0)

val WarmOffWhite = Color(0xFFFAF8F6)
val SurfaceGray = Color(0xFFF5F5F5)

// ===========================================
// GRADIENT HELPERS
// ===========================================
val GradientRoseGoldStart = RoseGold.copy(alpha = 0.3f)
val GradientRoseGoldEnd = Color.Transparent
val GradientTealStart = TealAccent.copy(alpha = 0.15f)
val GradientTealEnd = Color.Transparent
