/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class KRandomTest {

    @Test
    fun `seeded int generator is deterministic`() {
        val a = KRandom.int(1, 100, 99L)
        val b = KRandom.int(1, 100, 99L)
        assertEquals(a.next(), b.next())
        assertEquals(a.next(), b.next())
    }

    @Test
    fun `take and sequence APIs work`() {
        val g = KRandom.int(10, 20)
        val list = g.take(5)
        assertEquals(5, list.size)
        assertTrue(list.all { it in 10..20 })

        val seq = g.asSequence().take(3).toList()
        assertEquals(3, seq.size)
    }

    @Test
    fun `kotlin config locale wiring works`() {
        val name = KRandom.fullName(KConfig(seed = 7L, locale = Locale.GERMANY)).next()
        assertNotNull(name)
        assertFalse(name.isBlank())
    }

    @Test
    fun `fromType delegates to java forType`() {
        val g = KRandom.fromType(java.lang.Integer::class.java)
        assertNotNull(g.next())
    }
}
