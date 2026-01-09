package com.skinscan.sa

import com.skinscan.sa.ui.theme.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Test Trusted Glow theme colors match specification
 */
class ThemeTest {
    @Test
    fun `primary color is Teal 600`() {
        assertEquals(0xFF00897B.toInt(), Teal600.value.toLong().toInt())
    }

    @Test
    fun `secondary color is Deep Purple 700`() {
        assertEquals(0xFF512DA8.toInt(), DeepPurple700.value.toLong().toInt())
    }

    @Test
    fun `accent color is Coral 400`() {
        assertEquals(0xFFFF7043.toInt(), Coral400.value.toLong().toInt())
    }

    @Test
    fun `success color is Green 600`() {
        assertEquals(0xFF43A047.toInt(), Green600.value.toLong().toInt())
    }

    @Test
    fun `error color is Red 600`() {
        assertEquals(0xFFE53935.toInt(), Red600.value.toLong().toInt())
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
