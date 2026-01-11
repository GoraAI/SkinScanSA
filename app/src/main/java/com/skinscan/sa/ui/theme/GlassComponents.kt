package com.skinscan.sa.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================
// GLASSMORPHISM COMPONENTS
// ============================================

/**
 * Glass card with semi-transparent background and subtle border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .glassSurface(cornerRadius)
            .padding(Spacing.m),
        content = content
    )
}

/**
 * Glass surface modifier for glassmorphism effect
 */
fun Modifier.glassSurface(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.6f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(GlassSurface.copy(alpha = alpha))
    .border(
        width = 1.dp,
        color = GlassBorder.copy(alpha = 0.1f),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Glass button surface modifier
 */
fun Modifier.glassButton(
    cornerRadius: Dp = 12.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(GlassSurface.copy(alpha = 0.4f))
    .border(
        width = 1.dp,
        color = GlassBorder.copy(alpha = 0.15f),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Circular glass surface for buttons like "Start Scan"
 */
fun Modifier.glassCircle(
    alpha: Float = 0.6f
): Modifier = this
    .clip(RoundedCornerShape(percent = 50))
    .background(GlassSurface.copy(alpha = alpha))
    .border(
        width = 2.dp,
        color = TealAccent.copy(alpha = 0.5f),
        shape = RoundedCornerShape(percent = 50)
    )

/**
 * Glass surface with teal accent border
 */
fun Modifier.glassSurfaceAccent(
    cornerRadius: Dp = 16.dp,
    accentColor: Color = TealAccent
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(GlassSurface.copy(alpha = 0.6f))
    .border(
        width = 1.dp,
        color = accentColor.copy(alpha = 0.3f),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Full-width glass card
 */
@Composable
fun GlassCardFull(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(cornerRadius)
            .padding(Spacing.m),
        content = content
    )
}
