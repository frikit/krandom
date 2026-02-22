/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnumGenerator")
class EnumGeneratorTest {

    private enum Colour { RED, GREEN, BLUE }

    /** An enum with no constants — triggers the length == 0 guard. */
    private enum Empty {}

    @Test
    @DisplayName("generate() returns a value from the enum constants")
    void generateReturnsEnumConstant() {
        EnumGenerator<Colour> gen = new EnumGenerator<>(Colour.class);
        Set<Colour> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(gen.generate());
        // At least one value from the enum must appear
        assertFalse(seen.isEmpty());
        seen.forEach(v -> assertNotNull(v));
    }

    @Test
    @DisplayName("generate() covers all constants over many samples")
    void generateCoversAllConstants() {
        EnumGenerator<Colour> gen = new EnumGenerator<>(Colour.class);
        Set<Colour> seen = new HashSet<>();
        for (int i = 0; i < 300; i++) seen.add(gen.generate());
        assertEquals(Set.of(Colour.values()), seen);
    }

    @Test
    @DisplayName("seeded constructor produces identical sequences")
    void seededReproducibility() {
        EnumGenerator<Colour> a = new EnumGenerator<>(Colour.class, 7L);
        EnumGenerator<Colour> b = new EnumGenerator<>(Colour.class, 7L);
        for (int i = 0; i < 50; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("null enum class throws NullPointerException")
    void nullClassThrows() {
        assertThrows(NullPointerException.class, () -> new EnumGenerator<>(null));
    }

    @Test
    @DisplayName("empty enum (no constants) throws IllegalArgumentException")
    void emptyEnumThrows() {
        assertThrows(IllegalArgumentException.class, () -> new EnumGenerator<>(Empty.class));
    }

    @Test
    @DisplayName("non-enum class (getEnumConstants returns null) throws IllegalArgumentException")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void nonEnumClassThrows() {
        Class nonEnum = String.class;
        assertThrows(IllegalArgumentException.class, () -> new EnumGenerator<>(nonEnum));
    }
}
