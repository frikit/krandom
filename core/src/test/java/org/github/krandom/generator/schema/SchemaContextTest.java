/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SchemaContext")
class SchemaContextTest {

    @Test
    @DisplayName("stores locale random and index")
    void basic() {
        Random random = new Random(1L);
        SchemaContext context = new SchemaContext(Locale.US, random, 3);
        assertEquals(Locale.US, context.locale());
        assertEquals(random, context.random());
        assertEquals(3, context.recordIndex());
    }

    @Test
    @DisplayName("validates null and negative values")
    void validation() {
        assertThrows(NullPointerException.class, () -> new SchemaContext(null, new Random(), 0));
        assertThrows(NullPointerException.class, () -> new SchemaContext(Locale.US, null, 0));
        assertThrows(IllegalArgumentException.class, () -> new SchemaContext(Locale.US, new Random(), -1));
    }
}
