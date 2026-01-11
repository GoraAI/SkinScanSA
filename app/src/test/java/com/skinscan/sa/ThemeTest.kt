package com.skinscan.sa

import androidx.compose.ui.graphics.Color
import com.skinscan.sa.ui.theme.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Test Glow Guide theme colors match specification
 */
class ThemeTest {
    @Test
    fun `primary color is TealAccent`() {
        assertEquals(Color(0xFF64FFDA), TealAccent)
    }

    @Test
    fun `secondary color is RoseGold`() {
        assertEquals(Color(0xFFE0BFB8), RoseGold)
    }

    @Test
    fun `dark background is correct`() {
        assertEquals(Color(0xFF121212), DarkBackground)
    }

    @Test
    fun `success color is SuccessGreen`() {
        assertEquals(Color(0xFF4CAF50), SuccessGreen)
    }

    @Test
    fun `error color is ErrorRed`() {
        assertEquals(Color(0xFFE53935), ErrorRed)
    }

    @Test
    fun `spacing XS is 4dp`() {
        assertEquals(4f, Spacing.xs.value)
    }

    @Test
    fun `spacing S is 8dp`() {
        assertEquals(8f, Spacing.s.value)
    }

    @Test
    fun `spacing M is 16dp`() {
        assertEquals(16f, Spacing.m.value)
    }

    @Test
    fun `spacing L is 24dp`() {
        assertEquals(24f, Spacing.l.value)
    }

    @Test
    fun `minimum touch target is 48dp`() {
        assertEquals(48f, Spacing.minTouchTarget.value)
    }

    @Test
    fun `typography display large is 57sp`() {
        assertEquals(57f, Typography.displayLarge.fontSize.value)
    }

    @Test
    fun `typography headline large is 32sp`() {
        assertEquals(32f, Typography.headlineLarge.fontSize.value)
    }

    @Test
    fun `typography body large is 16sp`() {
        assertEquals(16f, Typography.bodyLarge.fontSize.value)
    }
}
