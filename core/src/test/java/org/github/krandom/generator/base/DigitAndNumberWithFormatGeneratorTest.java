/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Digit and NumberWithFormat generators")
class DigitAndNumberWithFormatGeneratorTest {

    @Test
    void digitGenerator() {
        DigitGenerator gen = new DigitGenerator(GeneratorConfig.builder().seed(42L).build());
        for (int i = 0; i < 100; i++) {
            assertTrue(gen.generate().matches("\\d"));
            assertTrue(gen.generateNonZero().matches("[1-9]"));
        }
        assertThrows(NullPointerException.class, () -> new DigitGenerator(null));
    }

    @Test
    void numberWithFormatGenerator() {
        NumberWithFormatGenerator gen = new NumberWithFormatGenerator("##-##", GeneratorConfig.builder().seed(7L).build());
        assertTrue(gen.generate().matches("\\d\\d-\\d\\d"));
        assertTrue(gen.generate("###/##").matches("\\d\\d\\d/\\d\\d"));

        assertThrows(NullPointerException.class, () -> new NumberWithFormatGenerator((String) null));
        assertThrows(IllegalArgumentException.class, () -> new NumberWithFormatGenerator(""));
        assertThrows(IllegalArgumentException.class, () -> new NumberWithFormatGenerator("abcd"));
        assertThrows(NullPointerException.class, () -> new NumberWithFormatGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> gen.generate(null));
        assertThrows(IllegalArgumentException.class, () -> gen.generate("____"));
    }
}
