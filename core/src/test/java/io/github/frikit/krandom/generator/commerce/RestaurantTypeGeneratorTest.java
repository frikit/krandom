/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

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

@DisplayName("RestaurantTypeGenerator")
class RestaurantTypeGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> EN = Set.of(
        "Italian", "Chinese", "Japanese", "Mexican", "Indian", "French",
        "Thai", "Greek", "Steakhouse", "Seafood", "Bistro", "Cafe");

    private static final Set<String> RU_TYPES = Set.of(
        "Итальянский", "Китайский", "Японский", "Мексиканский", "Индийский", "Французский",
        "Тайский", "Греческий", "Стейк-хаус", "Морепродукты", "Бистро", "Кафе");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a valid English type")
    void generateValid() {
        assertTrue(EN.contains(new RestaurantTypeGenerator().generate()));
    }

    @Test
    @DisplayName("default generate() can surface every type over many draws")
    void generateCoversAll() {
        RestaurantTypeGenerator gen = new RestaurantTypeGenerator(GeneratorConfig.builder().seed(5L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(EN, seen);
    }

    @Test
    @DisplayName("Russian locale generates Russian type names")
    void russian() {
        RestaurantTypeGenerator gen = new RestaurantTypeGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_TYPES.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("unmapped locale falls back to English types")
    void unmappedFallsBackToEnglish() {
        assertTrue(EN.contains(new RestaurantTypeGenerator(Locale.of("is", "IS")).generate()));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new RestaurantTypeGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new RestaurantTypeGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new RestaurantTypeGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new RestaurantTypeGenerator((Locale) null));
    }

    @Nested
    @DisplayName("RestaurantTypeDataRegistry")
    class Registry {

        @Test
        @DisplayName("registry honors built-ins, custom providers, and rejects null/unknown")
        void registry() {
            assertTrue(RestaurantTypeDataRegistry.isRegistered(RU));
            assertTrue(RestaurantTypeDataRegistry.isRegistered(Locale.of("ru"))); // language-only
            assertFalse(RestaurantTypeDataRegistry.isRegistered(Locale.of("is", "IS")));
            assertFalse(RestaurantTypeDataRegistry.isRegistered(null));

            assertNotNull(RestaurantTypeDataRegistry.forLocale(RU));
            assertNotNull(RestaurantTypeDataRegistry.forLocale(Locale.of("ru", "XX"))); // language fallback
            assertNull(RestaurantTypeDataRegistry.forLocale(Locale.of("is")));
            assertNull(RestaurantTypeDataRegistry.forLocale(null));
            assertTrue(RestaurantTypeDataRegistry.registeredKeys().contains("ru_RU"));

            RestaurantTypeDataProvider custom = new RestaurantTypeDataProvider() {
                @Override
                public Locale getLocale() {
                    return Locale.of("zz");
                }

                @Override
                public List<String> getTypes() {
                    return List.of("Gastropub");
                }
            };
            RestaurantTypeDataRegistry.register(custom);
            assertEquals("Gastropub", new RestaurantTypeGenerator(Locale.of("zz")).generate());
            assertThrows(NullPointerException.class, () -> RestaurantTypeDataRegistry.register(null));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofRestaurantType (default / locale / config) produce valid types")
        void facade() {
            assertTrue(EN.contains(Generators.ofRestaurantType().generate()));
            assertTrue(RU_TYPES.contains(Generators.ofRestaurantType(RU).generate()));
            assertTrue(EN.contains(
                Generators.ofRestaurantType(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
