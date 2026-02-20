/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGeneratorConfig")
class ObjectGeneratorConfigTest {

    @Test
    @DisplayName("defaults() returns config with default values")
    void defaultValues() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.defaults();
        assertEquals(ObjectGeneratorConfig.DEFAULT_MAX_DEPTH, c.getMaxDepth());
        assertFalse(c.isIgnoreErrors());
        assertTrue(c.getTypeOverride(String.class).isEmpty());
        assertTrue(c.getFieldOverride(String.class, "value").isEmpty());
    }

    @Test
    @DisplayName("maxDepth(3) stores the value")
    void maxDepthStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder().maxDepth(3).build();
        assertEquals(3, c.getMaxDepth());
    }

    @Test
    @DisplayName("maxDepth(0) throws IllegalArgumentException")
    void maxDepthZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ObjectGeneratorConfig.builder().maxDepth(0));
    }

    @Test
    @DisplayName("ignoreErrors(true) stores the flag")
    void ignoreErrorsStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder().ignoreErrors(true).build();
        assertTrue(c.isIgnoreErrors());
    }

    @Test
    @DisplayName("type-level override is stored and retrievable")
    void typeOverrideStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder()
                .override(String.class, () -> "fixed")
                .build();
        assertTrue(c.getTypeOverride(String.class).isPresent());
        assertEquals("fixed", c.getTypeOverride(String.class).get().generate());
    }

    @Test
    @DisplayName("field-level override is stored and retrievable")
    void fieldOverrideStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder()
                .override(String.class, "value", () -> "hello")
                .build();
        assertTrue(c.getFieldOverride(String.class, "value").isPresent());
        assertEquals("hello", c.getFieldOverride(String.class, "value").get().generate());
    }

    @Test
    @DisplayName("override(null type) throws NullPointerException")
    void typeOverrideNullTypeThrows() {
        assertThrows(NullPointerException.class,
                () -> ObjectGeneratorConfig.builder().override(null, () -> "x"));
    }

    @Test
    @DisplayName("override(null field name) throws NullPointerException")
    void fieldOverrideNullFieldThrows() {
        assertThrows(NullPointerException.class,
                () -> ObjectGeneratorConfig.builder().override(String.class, null, () -> "x"));
    }
}
