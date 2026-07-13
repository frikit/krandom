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

@DisplayName("HobbyGenerator")
class HobbyGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> RU_HOBBIES = Set.of(
        "Фотография", "Садоводство", "Туризм", "Живопись", "Кулинария", "Чтение", "Велоспорт",
        "Бег", "Йога", "Шахматы", "Рыбалка", "Танцы", "Плавание", "Путешествия");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a non-empty English hobby")
    void generateNotEmpty() {
        assertFalse(new HobbyGenerator().generate().isEmpty());
    }

    @Test
    @DisplayName("default generate() produces varied values over many draws")
    void generateVaried() {
        HobbyGenerator gen = new HobbyGenerator(GeneratorConfig.builder().seed(33L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(gen.generate());
        }
        assertTrue(seen.size() >= 20, "expected variety, saw " + seen.size());
    }

    @Test
    @DisplayName("Russian locale generates Russian hobby names")
    void russian() {
        HobbyGenerator gen = new HobbyGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_HOBBIES.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("unmapped locale falls back to English hobbies")
    void unmappedFallsBackToEnglish() {
        assertFalse(new HobbyGenerator(Locale.of("is", "IS")).generate().isEmpty());
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new HobbyGenerator(GeneratorConfig.builder().seed(7L).build()).generateList(30);
        List<String> b = new HobbyGenerator(GeneratorConfig.builder().seed(7L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new HobbyGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new HobbyGenerator((Locale) null));
    }

    @Nested
    @DisplayName("HobbyDataRegistry")
    class Registry {
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofHobby (default / locale / config) produce valid values")
        void facade() {
            assertFalse(Generators.ofHobby().generate().isEmpty());
            assertTrue(RU_HOBBIES.contains(Generators.ofHobby(RU).generate()));
            assertFalse(Generators.ofHobby(GeneratorConfig.builder().seed(1L).build()).generate().isEmpty());
        }
    }
}
