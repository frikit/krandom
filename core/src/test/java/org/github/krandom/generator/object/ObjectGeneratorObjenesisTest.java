/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.AllArgsOnly;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — Objenesis fallback for no-no-arg-constructor classes")
class ObjectGeneratorObjenesisTest {

    private static final int SAMPLES = 50;

    @Test
    @DisplayName("generates a non-null instance for a class with only an all-args constructor")
    void generatesNonNull() {
        AllArgsOnly result = new ObjectGenerator<>(AllArgsOnly.class).generate();
        assertNotNull(result);
    }

    @Test
    @DisplayName("name field is populated (non-null)")
    void namePopulated() {
        AllArgsOnly result = new ObjectGenerator<>(AllArgsOnly.class).generate();
        assertNotNull(result.getName());
    }

    @Test
    @DisplayName("age field is populated (non-zero across 50 samples)")
    void agePopulatedStatistically() {
        boolean sawNonZero = false;
        for (int i = 0; i < SAMPLES; i++) {
            if (new ObjectGenerator<>(AllArgsOnly.class).generate().getAge() != 0) {
                sawNonZero = true;
                break;
            }
        }
        assertTrue(sawNonZero, "age was never non-zero across " + SAMPLES + " samples");
    }
}
