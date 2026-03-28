/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IdentifierMaskGenerator")
class IdentifierMaskGeneratorTest {

    @Test
    @DisplayName("default generate uses mixed placeholder mask")
    void generateDefault() {
        String value = new IdentifierMaskGenerator().generate();
        assertTrue(value.matches("[A-Z]{2}\\d{2}[A-Z]{2}\\d{2}"), "Unexpected format: " + value);
    }

    @Test
    @DisplayName("generate(mask) replaces alpha and numeric placeholders")
    void generateWithMask() {
        IdentifierMaskGenerator generator = new IdentifierMaskGenerator();
        String value = generator.generate("ID-??-###");
        assertTrue(value.matches("ID-[A-Z]{2}-\\d{3}"), "Unexpected format: " + value);
    }

    @Test
    @DisplayName("generateNumeric(mask) only replaces numeric placeholders")
    void generateNumeric() {
        IdentifierMaskGenerator generator = new IdentifierMaskGenerator();
        String value = generator.generateNumeric("AB-###-??");
        assertTrue(value.matches("AB-\\d{3}-\\?\\?"), "Unexpected format: " + value);
    }

    @Test
    @DisplayName("generateAlphaNumeric(mask) keeps literal characters")
    void generateAlphaNumeric() {
        IdentifierMaskGenerator generator = new IdentifierMaskGenerator();
        String value = generator.generateAlphaNumeric("X?#-Z");
        assertTrue(value.matches("X[A-Z]\\d-Z"), "Unexpected format: " + value);
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        IdentifierMaskGenerator a = new IdentifierMaskGenerator(config);
        IdentifierMaskGenerator b = new IdentifierMaskGenerator(config);
        assertEquals(a.generate("??##"), b.generate("??##"));
    }

    @Test
    @DisplayName("constructor and mask validation")
    void validation() {
        assertThrows(NullPointerException.class, () -> new IdentifierMaskGenerator(null));
        IdentifierMaskGenerator generator = new IdentifierMaskGenerator();
        assertThrows(NullPointerException.class, () -> generator.generate(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(" "));
        assertThrows(NullPointerException.class, () -> generator.generateNumeric(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateNumeric(""));
        assertThrows(NullPointerException.class, () -> generator.generateAlphaNumeric(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateAlphaNumeric("   "));
    }
}
