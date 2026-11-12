/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.android.junitplatform

import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Krandom
import io.github.frikit.krandom.junit.KrandomExtension
import io.github.frikit.krandom.junit.KrandomSeed
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** JUnit Jupiter local unit tests on the JUnit Platform, where krandom-junit seeds each test. */
@ExtendWith(KrandomExtension::class)
class KrandomExtensionTest {
    @Test
    fun `injects a seeded session`(fake: Krandom) {
        assertTrue(fake.email().contains('@'))
    }

    @Test
    @KrandomSeed(20261112L)
    fun `pins the seed of the test`(
        config: GeneratorConfig,
        fake: Krandom,
    ) {
        assertEquals(20261112L, config.seed.asLong)
        // A session built again from the test's configuration draws the injected session's values.
        assertEquals(Krandom.of(config).fullName(), fake.fullName())
    }

    @Test
    @KrandomSeed(20261112L)
    fun `seeds the Kotlin DSL with the seed of the test`(generatorConfig: GeneratorConfig) {
        val pinned = generatorConfig.seed.asLong

        val first = krandom<Badge> { config { seed(pinned) } }
        val second = krandom<Badge> { config { seed(pinned) } }

        assertEquals(first, second)
    }
}

data class Badge(
    val holder: String,
    val email: String,
)
