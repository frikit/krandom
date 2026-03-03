/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.Locale

class KConfigTest {

    @Test
    fun `toJava and fromJava retain config values`() {
        val config = KConfig(
            seed = 42L,
            charset = StandardCharsets.UTF_8,
            minStringLength = 3,
            maxStringLength = 12,
            minCollectionSize = 0,
            maxCollectionSize = 7,
            locale = Locale.JAPAN
        )

        val java = config.toJava()
        assertTrue(java.seed.isPresent)
        assertEquals(42L, java.seed.asLong)
        assertEquals(StandardCharsets.UTF_8, java.charset)
        assertEquals(3, java.minStringLength)
        assertEquals(12, java.maxStringLength)
        assertEquals(0, java.minCollectionSize)
        assertEquals(7, java.maxCollectionSize)
        assertEquals(Locale.JAPAN, java.locale)

        assertEquals(config, KConfig.fromJava(java))
    }
}
