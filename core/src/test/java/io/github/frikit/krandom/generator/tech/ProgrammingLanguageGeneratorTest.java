/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.tech;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProgrammingLanguageGenerator")
class ProgrammingLanguageGeneratorTest {

    @RepeatedTest(200)
    @DisplayName("generate() returns a non-empty language name")
    void generateNotEmpty() {
        assertFalse(new ProgrammingLanguageGenerator().generate().isEmpty());
    }

    @Test
    @DisplayName("generate() produces varied values over many draws")
    void generateVaried() {
        ProgrammingLanguageGenerator gen =
            new ProgrammingLanguageGenerator(GeneratorConfig.builder().seed(44L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(gen.generate());
        }
        assertTrue(seen.size() >= 20, "expected variety, saw " + seen.size());
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a =
            new ProgrammingLanguageGenerator(GeneratorConfig.builder().seed(9L).build()).generateList(30);
        List<String> b =
            new ProgrammingLanguageGenerator(GeneratorConfig.builder().seed(9L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new ProgrammingLanguageGenerator(null));
    }

    @Test
    @DisplayName("facade ofProgrammingLanguage produces non-empty values")
    void facade() {
        assertFalse(Generators.ofProgrammingLanguage().generate().isEmpty());
        assertFalse(Generators.ofProgrammingLanguage(GeneratorConfig.builder().seed(1L).build())
                              .generate().isEmpty());
    }
}
