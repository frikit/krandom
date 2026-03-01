/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 2 base generators")
class Phase2BaseGeneratorsTest {

    @Test
    @DisplayName("nullable boolean produces true/false/null")
    void nullableBoolean() {
        NullableBooleanGenerator generator = new NullableBooleanGenerator();
        boolean sawNull = false;
        boolean sawTrue = false;
        boolean sawFalse = false;
        for (int i = 0; i < 500 && !(sawNull && sawTrue && sawFalse); i++) {
            Boolean value = generator.generate();
            if (value == null) {
                sawNull = true;
            } else if (value) {
                sawTrue = true;
            } else {
                sawFalse = true;
            }
        }
        assertTrue(sawNull && sawTrue && sawFalse);
        assertThrows(NullPointerException.class, () -> new NullableBooleanGenerator(null));
    }

    @Test
    @DisplayName("nullable boolean supports seeded reproducibility")
    void nullableBooleanSeeded() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(77L).build();
        NullableBooleanGenerator a = new NullableBooleanGenerator(cfg);
        NullableBooleanGenerator b = new NullableBooleanGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("pydecimal generator supports scale and seeded reproducibility")
    void pydecimal() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        PyDecimalGenerator a = new PyDecimalGenerator(cfg);
        PyDecimalGenerator b = new PyDecimalGenerator(cfg);

        BigDecimal v1 = a.generate(5, 3);
        BigDecimal v2 = b.generate(5, 3);
        assertEquals(v1, v2);
        assertEquals(3, v1.scale());
        assertEquals(0, a.generate(4, 0).scale());
    }

    @Test
    @DisplayName("pydecimal invalid bounds throw")
    void pydecimalInvalid() {
        PyDecimalGenerator generator = new PyDecimalGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0, 2));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(3, -1));
        assertThrows(NullPointerException.class, () -> new PyDecimalGenerator(null));
    }
}
