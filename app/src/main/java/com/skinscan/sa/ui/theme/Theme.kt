package com.skinscan.sa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Glow Guide Theme
 *
 * Premium dark-mode-first design with rose gold accents
 * and glassmorphism effects for a luxury skincare app experience.
 *
 * Design System:
 * - Primary: Rose Gold (elegant, premium)
 * - Accent: Teal (progress, success indicators)
 * - Background: Dark (#121212) with glassmorphism surfaces
 * - Text: High contrast white/gray on dark
 */

// Glow Guide Dark Color Scheme (Primary)
private val GlowGuideDarkColorScheme = darkColorScheme(
    // Primary - Rose Gold
    primary = RoseGold,
    onPrimary = DarkBackground,
    primaryContainer = RoseGoldDark,
    onPrimaryContainer = TextWhite,

    // Secondary - Champagne
    secondary = Champagne,
    onSecondary = DarkBackground,
    secondaryContainer = ChampagneDark,
    onSecondaryContainer = TextWhite,

    // Tertiary - Teal Accent (for progress/success)
    tertiary = TealAccent,
    onTertiary = DarkBackground,
    tertiaryContainer = TealAccentDark,
    onTertiaryContainer = TextWhite,

    // Error
    error = ErrorRed,
    onError = DarkBackground,
    errorContainer = Red700,
    onErrorContainer = Red50,

    // Backgrounds
    background = DarkBackground,
    onBackground = TextWhite,

    // Surfaces - for glassmorphism cards
    surface = SurfaceBlack,
    onSurface = TextWhite,
    surfaceVariant = SurfaceDark3,
    onSurfaceVariant = TextSecondary,

    // Outlines - subtle glass borders
    outline = GlassBorder,
    outlineVariant = GlassBorderLight,

    // Inverse colors
    inverseSurface = TextWhite,
    inverseOnSurface = DarkBackground,
    inversePrimary = RoseGoldDark
)

// Glow Guide Light Color Scheme (Secondary/Optional)
private val GlowGuideLightColorScheme = lightColorScheme(
    // Primary - Rose Gold (darker for contrast)
    primary = RoseGoldDark,
    onPrimary = Color.White,
    primaryContainer = RoseGoldLight,
    onPrimaryContainer = RoseGoldDark,

    // Secondary - Champagne
    secondary = ChampagneDark,
    onSecondary = DarkBackground,
    secondaryContainer = ChampagneLight,
    onSecondaryContainer = ChampagneDark,

    // Tertiary - Teal Accent
    tertiary = TealAccentDark,
    onTertiary = Color.White,
    tertiaryContainer = Teal50,
    onTertiaryContainer = Teal700,

    // Error
    error = Red600,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red700,

    // Backgrounds
    background = WarmOffWhite,
    onBackground = TextPrimary,

    // Surfaces
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,

    // Outlines
    outline = TextDisabled,
    outlineVariant = Color(0xFFE0E0E0)
)

/**
 * Glow Guide App Theme
 *
 * @param darkTheme Force dark theme (default: true for Glow Guide)
 * @param dynamicColor Disable dynamic color to preserve brand colors
 * @param content Composable content
 */
@Composable
fun SkinScanSATheme(
    darkTheme: Boolean = true, // Glow Guide is dark-mode-first
    dynamicColor: Boolean = false, // Disable to preserve brand colors
    content: @Composable () -> Unit
) {
    // Always use our custom Glow Guide color scheme
    // Dynamic color is disabled to maintain brand consistency
    val colorScheme = if (darkTheme) {
        GlowGuideDarkColorScheme
    } else {
        GlowGuideLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Alias for the theme (for clearer naming)
 */
@Composable
fun GlowGuideTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    SkinScanSATheme(
        darkTheme = darkTheme,
        dynamicColor = false,
        content = content
    )
}
