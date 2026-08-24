/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenerationContext")
class GenerationContextTest {

    @Test
    @DisplayName("legacy constructor remains useful without invented reflection metadata")
    void legacyConstructorCompatibility() {
        GenerationContext context = new GenerationContext("name", Fixture.class, 2);

        assertEquals("name", context.getFieldName());
        assertEquals(Fixture.class, context.getOwnerType());
        assertEquals(2, context.getDepth());
        assertEquals("Fixture.name", context.getPath());
        assertTrue(context.getDeclaredType().isEmpty());
        assertTrue(context.getDeclaration().isEmpty());
        assertTrue(context.getConfig().isEmpty());
    }

    @Test
    @DisplayName("metadata-complete constructor validates depth and path")
    void richConstructorValidation() {
        assertThrows(IllegalArgumentException.class,
                     () -> new GenerationContext("name", Fixture.class, -1, "Fixture.name",
                                                 String.class, null, GeneratorConfig.defaults()));
        assertThrows(IllegalArgumentException.class,
                     () -> new GenerationContext("name", Fixture.class, 0, " ",
                                                 String.class, null, GeneratorConfig.defaults()));
    }

    @Test
    @DisplayName("legacy path falls back to the binary name for anonymous owner types")
    void anonymousOwnerPath() {
        Object anonymous = new Object() { };

        GenerationContext context = new GenerationContext("name", anonymous.getClass(), 0);

        assertEquals(anonymous.getClass().getName() + ".name", context.getPath());
    }

    private static final class Fixture {
    }
}
