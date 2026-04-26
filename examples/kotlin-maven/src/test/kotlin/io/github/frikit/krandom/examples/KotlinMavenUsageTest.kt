package io.github.frikit.krandom.examples

import io.github.frikit.krandom.generator.Generators
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KotlinMavenUsageTest {

    @Test
    fun `core can generate fixture data from kotlin`() {
        val fixture = UserFixture(
            name = Generators.ofFullName().generate(),
            email = Generators.ofEmail().generate(),
            country = Generators.ofCountry().generate()
        )

        assertTrue(fixture.email.contains("@"))
        assertFalse(fixture.country.isBlank())
    }
}
