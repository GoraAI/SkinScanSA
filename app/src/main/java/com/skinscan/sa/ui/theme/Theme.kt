package com.skinscan.sa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ============================================
// GLOW GUIDE THEME
// ============================================

private val GlowGuideColorScheme = darkColorScheme(
    primary = RoseGold,
    onPrimary = DarkBackground,
    primaryContainer = RoseGoldDark,
    onPrimaryContainer = TextWhite,

    secondary = TealAccent,
    onSecondary = DarkBackground,
    secondaryContainer = TealAccent.copy(alpha = 0.3f),
    onSecondaryContainer = TextWhite,

    tertiary = Champagne,
    onTertiary = DarkBackground,
    tertiaryContainer = Champagne.copy(alpha = 0.3f),
    onTertiaryContainer = TextWhite,

    error = ErrorRed,
    onError = TextWhite,
    errorContainer = ErrorRed.copy(alpha = 0.3f),
    onErrorContainer = TextWhite,

    background = DarkBackground,
    onBackground = TextWhite,

    surface = SurfaceBlack,
    onSurface = TextWhite,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondary,

    outline = GlassBorder.copy(alpha = 0.2f),
    outlineVariant = GlassBorder.copy(alpha = 0.1f)
)

@Composable
fun GlowGuideTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlowGuideColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep old theme name for backwards compatibility during migration
@Composable
fun SkinScanSATheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GlowGuideTheme(content = content)
}
