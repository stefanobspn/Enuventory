package dev.stefano.enuventory.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AssetIdGeneratorTest {

    private val validFormat = Regex("^HW-[23456789ABCDEFGHJKMNPQRSTVWXYZ]{5}$")

    @Test
    fun `generated id matches HW- prefix + 5 char safe alphabet format`() {
        repeat(100) {
            val id = AssetIdGenerator.generate()
            assertTrue("'$id' does not match expected format", validFormat.matches(id))
        }
    }

    @Test
    fun `generated id never contains ambiguous characters (0, O, 1, I, L, U)`() {
        repeat(200) {
            val id = AssetIdGenerator.generate()
            val suffix = id.removePrefix("HW-")
            for (ambiguous in listOf('0', 'O', '1', 'I', 'L', 'U')) {
                assertTrue(
                    "id '$id' contains ambiguous character '$ambiguous'",
                    ambiguous !in suffix
                )
            }
        }
    }

    @Test
    fun `is deterministic given the same Random seed (useful for tests)`() {
        val first = AssetIdGenerator.generate(random = Random(seed = 42))
        val second = AssetIdGenerator.generate(random = Random(seed = 42))
        assertEquals(first, second)
    }

    @Test
    fun `produces distinct ids across many calls (statistical collision check)`() {
        val generated = (1..1000).map { AssetIdGenerator.generate() }.toSet()
        // Dengan alphabet 30^5 (~24 juta kombinasi), 1000 sample harusnya semuanya unik.
        assertEquals(1000, generated.size)
    }
}
