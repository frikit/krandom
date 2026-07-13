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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NationalityGenerator")
class NationalityGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> EN = Set.of(
        "American", "British", "French", "German", "Japanese", "Chinese",
        "Brazilian", "Italian", "Spanish", "Russian", "Indian", "Canadian");

    private static final Set<String> RU_NATIONALITIES = Set.of(
        "Американец", "Британец", "Француз", "Немец", "Японец", "Китаец",
        "Бразилец", "Итальянец", "Испанец", "Русский", "Индиец", "Канадец");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a valid English demonym")
    void generateValid() {
        assertTrue(EN.contains(new NationalityGenerator().generate()));
    }

    @Test
    @DisplayName("default generate() can surface every demonym over many draws")
    void generateCoversAll() {
        NationalityGenerator gen = new NationalityGenerator(GeneratorConfig.builder().seed(5L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(EN, seen);
    }

    @Test
    @DisplayName("Russian locale generates Russian demonyms")
    void russian() {
        NationalityGenerator gen = new NationalityGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_NATIONALITIES.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("unmapped locale falls back to English demonyms")
    void unmappedFallsBackToEnglish() {
        assertTrue(EN.contains(new NationalityGenerator(Locale.of("is", "IS")).generate()));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new NationalityGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new NationalityGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new NationalityGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new NationalityGenerator((Locale) null));
    }

    @Nested
    @DisplayName("NationalityDataRegistry")
    class Registry {
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofNationality (default / locale / config) produce valid demonyms")
        void facade() {
            assertTrue(EN.contains(Generators.ofNationality().generate()));
            assertTrue(RU_NATIONALITIES.contains(Generators.ofNationality(RU).generate()));
            assertTrue(EN.contains(
                Generators.ofNationality(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
