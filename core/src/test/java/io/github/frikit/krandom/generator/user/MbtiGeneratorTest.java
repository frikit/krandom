/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MbtiGenerator")
class MbtiGeneratorTest {

    private static final Set<String> TYPES = Set.of(
        "ISTJ", "ISFJ", "INFJ", "INTJ", "ISTP", "ISFP", "INFP", "INTP",
        "ESTP", "ESFP", "ENFP", "ENTP", "ESTJ", "ESFJ", "ENFJ", "ENTJ");

    @RepeatedTest(200)
    @DisplayName("generate() returns a valid four-letter type")
    void generateValid() {
        assertTrue(TYPES.contains(new MbtiGenerator().generate()));
    }

    @Test
    @DisplayName("generate() can surface every type over many draws")
    void generateCoversAll() {
        MbtiGenerator gen = new MbtiGenerator(GeneratorConfig.builder().seed(21L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 4000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(TYPES, seen);
    }

    @RepeatedTest(100)
    @DisplayName("withNickname() pairs a valid type with a non-empty nickname")
    void withNickname() {
        String full = new MbtiGenerator().withNickname();
        assertTrue(full.matches("[EI][NS][FT][JP] \\(\\w+\\)"), full);
        assertTrue(TYPES.contains(full.substring(0, 4)), full);
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new MbtiGenerator(GeneratorConfig.builder().seed(2L).build()).generateList(30);
        List<String> b = new MbtiGenerator(GeneratorConfig.builder().seed(2L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new MbtiGenerator(null));
    }

    @Test
    @DisplayName("facade ofMbti produces valid types")
    void facade() {
        assertTrue(TYPES.contains(Generators.ofMbti().generate()));
        assertTrue(TYPES.contains(
            Generators.ofMbti(GeneratorConfig.builder().seed(1L).build()).generate()));
    }
}
