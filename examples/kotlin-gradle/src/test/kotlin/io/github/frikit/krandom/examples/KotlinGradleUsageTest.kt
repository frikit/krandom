package io.github.frikit.krandom.examples

import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.dsl.krandomList
import io.github.frikit.krandom.kotest.toArb
import io.kotest.property.arbitrary.take
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KotlinGradleUsageTest {

    @Test
    fun `core can generate fixture data from kotlin`() {
        val fixture = UserFixture(
            name = Generators.ofFullName().generate(),
            email = Generators.ofEmail().generate(),
            country = Generators.ofCountry().generate()
        )

        assertTrue(fixture.email.contains("@"))
        assertFalse(fixture.name.isBlank())
    }

    @Test
    fun `kotest extension is consumable from kotlin example`() {
        val samples = Generators.ofEmail().toArb().take(10).toList()

        assertEquals(10, samples.size)
        samples.forEach { assertTrue(it.contains("@")) }
    }

    @Test
    fun `kotlin dsl is consumable from kotlin example`() {
        val fixture = krandom<DslUserFixture> {
            rule("name") { "Ada Lovelace" }
            rule("email") { "ada@example.test" }
            rule("country") { "United Kingdom" }
        }
        val fixtures = krandomList<DslUserFixture>(2) {
            rule("name") { "Grace Hopper" }
            rule("email") { "grace@example.test" }
            rule("country") { "United States" }
        }

        assertEquals("Ada Lovelace", fixture.name)
        assertEquals(2, fixtures.size)
        fixtures.forEach { assertEquals("Grace Hopper", it.name) }
    }
}

class DslUserFixture {
    var name: String = ""
    var email: String = ""
    var country: String = ""
}
