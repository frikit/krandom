/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.identifier;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("UpcGenerator")
class UpcGeneratorTest {

    @Test
    @DisplayName("generator outputs valid UPC-A shape and checksum")
    void generate() {
        UpcGenerator generator = new UpcGenerator();
        String upc = generator.generate();
        assertTrue(upc.matches("\\d{12}"));

        int expectedCheck = UpcGenerator.computeCheckDigit(upc.substring(0, 11));
        assertEquals(expectedCheck, upc.charAt(11) - '0');
    }

    @Test
    @DisplayName("seeded generators are deterministic")
    void seeded() {
        GeneratorConfig config = GeneratorConfig.builder().seed(919L).build();
        UpcGenerator a = new UpcGenerator(config);
        UpcGenerator b = new UpcGenerator(config);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new UpcGenerator(null));
    }
}
