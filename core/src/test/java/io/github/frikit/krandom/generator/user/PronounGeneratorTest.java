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

@DisplayName("PronounGenerator")
class PronounGeneratorTest {

    private static final Set<String> SETS =
        Set.of("he/him", "she/her", "they/them", "ze/zir", "xe/xem", "ey/em");
    private static final Set<String> SUBJECTS = Set.of("he", "she", "they", "ze", "xe", "ey");
    private static final Set<String> OBJECTS = Set.of("him", "her", "them", "zir", "xem", "em");

    @RepeatedTest(200)
    @DisplayName("generate() returns a valid pronoun set")
    void generateValid() {
        assertTrue(SETS.contains(new PronounGenerator().generate()));
    }

    @Test
    @DisplayName("generate() can surface every set over many draws")
    void generateCoversAll() {
        PronounGenerator gen = new PronounGenerator(GeneratorConfig.builder().seed(13L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(SETS, seen);
    }

    @Test
    @DisplayName("subjective() and objective() return the two halves")
    void subjectiveObjective() {
        PronounGenerator gen = new PronounGenerator();
        for (int i = 0; i < 200; i++) {
            assertTrue(SUBJECTS.contains(gen.subjective()));
            assertTrue(OBJECTS.contains(gen.objective()));
        }
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new PronounGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new PronounGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new PronounGenerator(null));
    }

    @Test
    @DisplayName("facade ofPronoun produces valid sets")
    void facade() {
        assertTrue(SETS.contains(Generators.ofPronoun().generate()));
        assertTrue(SETS.contains(
            Generators.ofPronoun(GeneratorConfig.builder().seed(1L).build()).generate()));
    }
}
