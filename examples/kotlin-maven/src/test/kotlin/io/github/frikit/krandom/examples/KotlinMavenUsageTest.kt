package io.github.frikit.krandom.examples

import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import io.github.frikit.krandom.generator.`object`.exception.ObjectGenerationException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
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

    @Test
    fun `core names the Kotlin DSL when immutable Kotlin construction is unavailable`() {
        val failure = assertThrows(ObjectGenerationException::class.java) {
            ObjectGenerator(CoreOnlyImmutableFixture::class.java).generate()
        }

        assertTrue(failure.cause?.message?.contains("krandom-kotlin-dsl") == true)
    }
}

data class CoreOnlyImmutableFixture(val value: String)
