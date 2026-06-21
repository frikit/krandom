/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PronounGenerator")
class PronounGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> EN_SETS =
        Set.of("he/him", "she/her", "they/them", "ze/zir", "xe/xem", "ey/em");
    private static final Set<String> EN_SUBJECTS = Set.of("he", "she", "they", "ze", "xe", "ey");
    private static final Set<String> EN_OBJECTS = Set.of("him", "her", "them", "zir", "xem", "em");

    private static final Set<String> RU_SETS = Set.of("он/его", "она/её", "они/их");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a valid English set")
    void generateValid() {
        assertTrue(EN_SETS.contains(new PronounGenerator().generate()));
    }

    @Test
    @DisplayName("subjective() and objective() return the two halves (English)")
    void subjectiveObjective() {
        PronounGenerator gen = new PronounGenerator();
        for (int i = 0; i < 200; i++) {
            assertTrue(EN_SUBJECTS.contains(gen.subjective()));
            assertTrue(EN_OBJECTS.contains(gen.objective()));
        }
    }

    @Test
    @DisplayName("Russian locale generates Russian pronoun sets")
    void russian() {
        PronounGenerator gen = new PronounGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_SETS.contains(gen.generate()));
        }
        assertTrue(Set.of("он", "она", "они").contains(gen.subjective()));
    }

    @Test
    @DisplayName("unmapped locale falls back to English sets")
    void unmappedFallsBackToEnglish() {
        assertTrue(EN_SETS.contains(new PronounGenerator(Locale.of("is", "IS")).generate()));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new PronounGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new PronounGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new PronounGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new PronounGenerator((Locale) null));
    }

    @Nested
    @DisplayName("PronounDataRegistry")
    class Registry {

        @Test
        @DisplayName("registry honors built-ins, custom providers, and rejects null/unknown")
        void registry() {
            assertTrue(PronounDataRegistry.isRegistered(RU));
            assertTrue(PronounDataRegistry.isRegistered(Locale.of("ru"))); // language-only
            assertFalse(PronounDataRegistry.isRegistered(Locale.of("is", "IS")));
            assertFalse(PronounDataRegistry.isRegistered(null));

            assertNotNull(PronounDataRegistry.forLocale(RU));
            assertNotNull(PronounDataRegistry.forLocale(Locale.of("ru", "XX"))); // language fallback
            assertNull(PronounDataRegistry.forLocale(Locale.of("is")));
            assertNull(PronounDataRegistry.forLocale(null));
            assertTrue(PronounDataRegistry.registeredKeys().contains("ru_RU"));

            PronounDataProvider custom = new PronounDataProvider() {
                @Override
                public Locale getLocale() {
                    return Locale.of("zz");
                }

                @Override
                public List<String> getPronounSets() {
                    return List.of("zz/zz");
                }
            };
            PronounDataRegistry.register(custom);
            assertEquals("zz/zz", new PronounGenerator(Locale.of("zz")).generate());
            assertThrows(NullPointerException.class, () -> PronounDataRegistry.register(null));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofPronoun (default / locale / config) produce valid sets")
        void facade() {
            assertTrue(EN_SETS.contains(Generators.ofPronoun().generate()));
            assertTrue(RU_SETS.contains(Generators.ofPronoun(RU).generate()));
            assertTrue(EN_SETS.contains(
                Generators.ofPronoun(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
