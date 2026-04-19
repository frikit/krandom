/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator semantic modes")
class ObjectGeneratorSemanticModeTest {

    @Test
    @DisplayName("relaxed mode lets @Randomizer override semantic field matching")
    void relaxedModeLetsRandomizerWin() {
        AnnotatedEmailHolder value = new ObjectGenerator<>(AnnotatedEmailHolder.class).generate();
        assertEquals("ANNOTATED", value.email);
    }

    @Test
    @DisplayName("strict mode lets semantic field matching override @Randomizer")
    void strictModeLetsSemanticWin() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                .build();
        AnnotatedEmailHolder value = new ObjectGenerator<>(AnnotatedEmailHolder.class, config).generate();
        assertNotEquals("ANNOTATED", value.email);
        assertTrue(value.email.contains("@"));
    }

    @Test
    @DisplayName("structural-only mode disables semantic field matching")
    void structuralOnlyModeDisablesSemanticMatching() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .build();
        PlainEmailHolder value = new ObjectGenerator<>(PlainEmailHolder.class, config).generate();
        assertFalse(value.email.contains("@"));
    }

    static class AnnotatedEmailHolder {

        @Randomizer(AnnotatedValueGenerator.class)
        String email;
    }

    static class PlainEmailHolder {

        String email;
    }

    public static class AnnotatedValueGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "ANNOTATED";
        }
    }
}
