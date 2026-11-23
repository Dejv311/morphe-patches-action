package app.action.patches.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class DarkPaletteTest {
    private val colors = actionDarkPalette.associate { it.field to it.argb }

    @Test
    fun paletteHasUniqueOpaqueTokensAndCompleteAccentFamilies() {
        assertEquals(actionDarkPalette.size, colors.size)
        for ((field, color) in colors) assertEquals("Alpha for $field", 255L, color ushr 24)
        for (family in listOf(
            listOf("p", "q", "r", "s", "t", "u"),
            listOf("v", "w", "x", "y", "z", "A", "B"),
            listOf("C", "D", "E", "F", "G", "H"),
            listOf("J", "K", "L", "M", "N", "O"),
            listOf("P", "Q", "R", "S", "T", "U"),
            listOf("V", "W", "X", "Y", "Z", "a0"),
            listOf("b0", "c0", "d0", "e0", "f0", "g0"),
            listOf("h0", "i0", "j0", "k0", "l0", "m0")
        )) for (field in family) assertTrue("Missing shade $field", field in colors)
    }

    @Test
    fun neutralTextAndControlOutlinesStayReadableOnAllSurfaces() {
        for (background in listOf("a", "c", "b", "A0")) {
            for (foreground in listOf("h", "g", "e")) {
                assertContrast(foreground, background, 4.5)
            }
            assertContrast("d", background, 3.0)
        }
    }

    @Test
    fun oledUsesBlackAndNeutralSurfacesWithSaturatedAccents() {
        assertEquals(0xFF000000L, colors.getValue("a"))
        for (field in listOf("a", "c", "b", "f", "d", "e", "g", "h", "A0",
            "i", "j", "k", "J", "K", "L", "P", "Q", "R", "p", "q", "r",
            "v", "w", "x", "C", "D", "E", "V", "W", "X", "Y", "Z", "a0",
            "b0", "c0", "d0", "h0", "i0", "j0", "n0", "t0", "q0", "w0", "x0")) {
            val color = colors.getValue(field)
            val red = (color shr 16) and 255L
            val green = (color shr 8) and 255L
            val blue = color and 255L
            assertEquals("Neutral red/green for $field", red, green)
            assertEquals("Neutral green/blue for $field", green, blue)
        }
        for (field in listOf("l", "M", "S", "s", "y", "F", "e0", "k0", "o0", "u0", "r0", "y0")) {
            val color = colors.getValue(field)
            val channels = listOf((color shr 16) and 255L, (color shr 8) and 255L, color and 255L)
            assertTrue("Saturated accent $field", channels.max() - channels.min() >= 128L)
        }
    }

    @Test
    fun coloredTextStaysReadableOnItsTintedContainers() {
        for ((background, foreground) in listOf(
            "i" to "l", "j" to "o", "k" to "o",
            "p" to "s", "q" to "t", "r" to "u",
            "v" to "y", "w" to "A", "x" to "B",
            "C" to "F", "D" to "G", "E" to "H",
            "J" to "M", "K" to "N", "L" to "O",
            "P" to "S", "Q" to "T", "R" to "U",
            "V" to "Y", "W" to "Z", "X" to "a0",
            "b0" to "e0", "c0" to "f0", "d0" to "g0",
            "h0" to "k0", "i0" to "l0", "j0" to "m0",
            "n0" to "o0", "n0" to "p0", "t0" to "u0", "t0" to "v0",
            "q0" to "r0", "q0" to "s0", "w0" to "y0", "x0" to "z0"
        )) assertContrast(foreground, background, 4.5)
    }

    private fun assertContrast(foreground: String, background: String, minimum: Double) {
        val first = luminance(colors.getValue(foreground))
        val second = luminance(colors.getValue(background))
        val ratio = (max(first, second) + 0.05) / (min(first, second) + 0.05)
        assertTrue("Contrast $foreground on $background is $ratio, needs $minimum", ratio >= minimum)
    }

    private fun luminance(color: Long): Double {
        fun channel(shift: Int): Double {
            val value = ((color shr shift) and 255L) / 255.0
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
