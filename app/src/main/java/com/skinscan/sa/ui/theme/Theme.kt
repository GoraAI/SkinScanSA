package com.skinscan.sa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = Color.White,
    primaryContainer = Teal50,
    onPrimaryContainer = Teal700,

    secondary = DeepPurple700,
    onSecondary = Color.White,
    secondaryContainer = DeepPurple50,
    onSecondaryContainer = DeepPurple800,

    tertiary = Coral400,
    onTertiary = Color.White,
    tertiaryContainer = Coral50,
    onTertiaryContainer = Coral500,

    error = Red600,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red700,

    background = WarmOffWhite,
    onBackground = TextPrimary,

    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,

    outline = TextDisabled,
    outlineVariant = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal500,
    onPrimary = Color.Black,
    primaryContainer = Teal700,
    onPrimaryContainer = Teal50,

    secondary = DeepPurple700,
    onSecondary = Color.Black,
    secondaryContainer = DeepPurple800,
    onSecondaryContainer = DeepPurple50,

    tertiary = Coral400,
    onTertiary = Color.Black,
    tertiaryContainer = Coral500,
    onTertiaryContainer = Coral50,

    error = Red600,
    onError = Color.Black,
    errorContainer = Red700,
    onErrorContainer = Red50,

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = TextSecondaryDark,

    outline = Color(0xFF6E6E6E),
    outlineVariant = Color(0xFF3E3E3E)
)

@Composable
fun SkinScanSATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Use dynamic color on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
