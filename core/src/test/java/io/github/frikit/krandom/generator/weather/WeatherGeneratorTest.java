/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.weather;

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

@DisplayName("WeatherGenerator")
class WeatherGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> EN = Set.of(
        "Sunny", "Clear", "Cloudy", "Overcast", "Rainy", "Drizzle",
        "Thunderstorm", "Snowy", "Windy", "Foggy", "Humid", "Stormy");

    private static final Set<String> RU_CONDITIONS = Set.of(
        "Солнечно", "Ясно", "Облачно", "Пасмурно", "Дождливо", "Морось",
        "Гроза", "Снежно", "Ветрено", "Туманно", "Влажно", "Штормово");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a valid English condition")
    void generateValid() {
        assertTrue(EN.contains(new WeatherGenerator().generate()));
    }

    @Test
    @DisplayName("default generate() can surface every condition over many draws")
    void generateCoversAll() {
        WeatherGenerator gen = new WeatherGenerator(GeneratorConfig.builder().seed(5L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(EN, seen);
    }

    @Test
    @DisplayName("Russian locale generates Russian condition names")
    void russian() {
        WeatherGenerator gen = new WeatherGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_CONDITIONS.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("unmapped locale falls back to English conditions")
    void unmappedFallsBackToEnglish() {
        assertTrue(EN.contains(new WeatherGenerator(Locale.of("is", "IS")).generate()));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new WeatherGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new WeatherGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new WeatherGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new WeatherGenerator((Locale) null));
    }

    @Nested
    @DisplayName("WeatherDataRegistry")
    class Registry {

        @Test
        @DisplayName("registry honors built-ins, custom providers, and rejects null/unknown")
        void registry() {
            assertTrue(WeatherDataRegistry.isRegistered(RU));
            assertTrue(WeatherDataRegistry.isRegistered(Locale.of("ru"))); // language-only
            assertFalse(WeatherDataRegistry.isRegistered(Locale.of("is", "IS")));
            assertFalse(WeatherDataRegistry.isRegistered(null));

            assertNotNull(WeatherDataRegistry.forLocale(RU));
            assertNotNull(WeatherDataRegistry.forLocale(Locale.of("ru", "XX"))); // language fallback
            assertNull(WeatherDataRegistry.forLocale(Locale.of("is")));
            assertNull(WeatherDataRegistry.forLocale(null));
            assertTrue(WeatherDataRegistry.registeredKeys().contains("ru_RU"));

            WeatherDataProvider custom = new WeatherDataProvider() {
                @Override
                public Locale getLocale() {
                    return Locale.of("zz");
                }

                @Override
                public List<String> getConditions() {
                    return List.of("Zephyr");
                }
            };
            WeatherDataRegistry.register(custom);
            assertEquals("Zephyr", new WeatherGenerator(Locale.of("zz")).generate());
            assertThrows(NullPointerException.class, () -> WeatherDataRegistry.register(null));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofWeather (default / locale / config) produce valid conditions")
        void facade() {
            assertTrue(EN.contains(Generators.ofWeather().generate()));
            assertTrue(RU_CONDITIONS.contains(Generators.ofWeather(RU).generate()));
            assertTrue(EN.contains(
                Generators.ofWeather(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
