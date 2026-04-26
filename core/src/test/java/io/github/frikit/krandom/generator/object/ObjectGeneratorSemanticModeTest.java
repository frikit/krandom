/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.constraints.Negative;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
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

    @Test
    @DisplayName("relaxed mode lets Bean Validation override semantic typed defaults")
    void relaxedModeLetsBeanValidationWin() {
        NegativeAccountIdHolder value = new ObjectGenerator<>(NegativeAccountIdHolder.class).generate();
        assertTrue(value.accountId < 0);
    }

    @Test
    @DisplayName("strict mode lets semantic typed defaults override Bean Validation")
    void strictModeLetsSemanticTypedDefaultsWin() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                .build();
        NegativeAccountIdHolder value = new ObjectGenerator<>(NegativeAccountIdHolder.class, config).generate();
        assertTrue(value.accountId > 0);
    }

    static class AnnotatedEmailHolder {

        @Randomizer(AnnotatedValueGenerator.class)
        String email;
    }

    static class PlainEmailHolder {

        String email;
    }

    static class NegativeAccountIdHolder {

        @Negative
        long accountId;
    }

    public static class AnnotatedValueGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "ANNOTATED";
        }
    }
}
