package com.skinscan.sa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF009688), // Teal 600 placeholder
    secondary = androidx.compose.ui.graphics.Color(0xFFFF7043), // Coral placeholder
    background = androidx.compose.ui.graphics.Color(0xFFFAF8F6) // Warm off-white placeholder
)

@Composable
fun SkinScanSATheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
