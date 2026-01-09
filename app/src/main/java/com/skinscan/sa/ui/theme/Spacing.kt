package com.skinscan.sa.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing system using 4dp base unit for SkinScan SA "Trusted Glow" theme
 */
object Spacing {
    val xs: Dp = 4.dp    // Extra small
    val s: Dp = 8.dp     // Small
    val m: Dp = 16.dp    // Medium
    val l: Dp = 24.dp    // Large
    val xl: Dp = 32.dp   // Extra large
    val xxl: Dp = 48.dp  // Extra extra large

    // Touch target minimum (WCAG 2.1 AA)
    val minTouchTarget: Dp = 48.dp
}
