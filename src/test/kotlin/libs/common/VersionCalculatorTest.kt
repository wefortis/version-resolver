package libs.common

import kotlin.test.Test
import kotlin.test.assertEquals

class VersionCalculatorTest {
    @Test
    fun `substitutes commits after tag for x`() {
        assertEquals("1.20.10", VersionCalculator.calculate("1.20.x-dev", 10))
        assertEquals("1.20.0", VersionCalculator.calculate("1.20.x-dev", 0))
    }

    @Test
    fun `falls back when tag is missing or has unexpected format`() {
        assertEquals(VersionCalculator.FALLBACK_VERSION, VersionCalculator.calculate(null, 5))
        assertEquals(VersionCalculator.FALLBACK_VERSION, VersionCalculator.calculate("1.8.106-release", 5))
        assertEquals(VersionCalculator.FALLBACK_VERSION, VersionCalculator.calculate("v1.20.x-dev", 5))
    }

    @Test
    fun `falls back when commit count is unknown`() {
        assertEquals(VersionCalculator.FALLBACK_VERSION, VersionCalculator.calculate("1.20.x-dev", null))
        assertEquals(VersionCalculator.FALLBACK_VERSION, VersionCalculator.calculate("1.20.x-dev", -1))
    }
}
