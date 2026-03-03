/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.github.krandom.generator.provider.ConflictPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class KSchemaAndProviderHubTest {

    @Test
    fun `schema wrapper generates expected keys`() {
        val field = KRandom.field(Locale.US)
        val schema = KRandom.schema(
            Locale.US,
            mapOf(
                "name" to field.bind("person.full_name"),
                "email" to field.bind("person.email"),
                "country" to field.bind("address.country")
            )
        )

        val row = schema.next()
        assertTrue(row.containsKey("name"))
        assertTrue(row.containsKey("email"))
        assertTrue(row.containsKey("country"))

        val rows = schema.take(3)
        assertEquals(3, rows.size)
    }

    @Test
    fun `provider hub supports custom provider and alias`() {
        val hub = KRandom.providerHub(Locale.US)
        assertTrue(hub.has("person"))
        assertTrue(hub.providerNames().contains("person"))

        hub.register("custom") { "value" }
        assertEquals("value", hub.get("custom"))

        hub.alias("c", "custom")
        assertEquals("value", hub.get("c"))

        hub.register("custom", { "replacement" }, ConflictPolicy.REPLACE)
        assertEquals("replacement", hub.get("custom"))
    }
}
