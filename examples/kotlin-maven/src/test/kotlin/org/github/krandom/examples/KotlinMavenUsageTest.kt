package org.github.krandom.examples

import org.github.krandom.kotlinapi.KRandom
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KotlinMavenUsageTest {

    @Test
    fun `kotlin api can generate fixture data`() {
        val fixture = UserFixture(
            name = KRandom.fullName().next(),
            email = KRandom.email().next(),
            country = KRandom.fromType(String::class.java).next()
        )

        assertTrue(fixture.email.contains("@"))
        assertFalse(fixture.country.isBlank())
    }
}
