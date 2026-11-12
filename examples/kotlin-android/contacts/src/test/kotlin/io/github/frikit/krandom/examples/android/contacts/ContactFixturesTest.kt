/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.android.contacts

import android.net.Uri
import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.Krandom
import io.github.frikit.krandom.generator.`object`.exception.ObjectGenerationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

/** JUnit 4 local unit tests, the Android default, compiled for JVM target 17. */
class ContactFixturesTest {
    @Test
    fun `a seeded session gives the same values again`() {
        val first = Krandom.seeded("contacts")
        val second = Krandom.seeded("contacts")

        assertEquals(first.fullName(), second.fullName())
        assertEquals(first.email(), second.email())
    }

    @Test
    fun `generator factories work in a local unit test`() {
        val email = Generators.ofEmail().generate()

        assertTrue(email.contains('@'))
    }

    @Test
    fun `the Kotlin DSL builds a data class whose Uri comes from a rule`() {
        val photo = mock(Uri::class.java)

        val contact =
            krandom<Contact> {
                config { seed(42L) }
                ruleForType<Uri> { photo }
            }

        assertSame(photo, contact.photo)
        assertTrue(contact.email.contains('@'))
    }

    @Test
    fun `a Uri without a rule fails and names its type`() {
        val failure = assertThrows(ObjectGenerationException::class.java) { krandom<Contact>() }

        assertTrue(failure.message.orEmpty().contains("declared type android.net.Uri"))
    }
}
