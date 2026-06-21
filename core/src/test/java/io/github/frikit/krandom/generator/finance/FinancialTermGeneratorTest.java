/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

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

@DisplayName("FinancialTermGenerator")
class FinancialTermGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> EN = Set.of(
        "Asset", "Liability", "Equity", "Revenue", "Profit", "Loss",
        "Dividend", "Interest", "Inflation", "Budget", "Capital", "Debt");

    private static final Set<String> RU_TERMS = Set.of(
        "Актив", "Обязательство", "Капитал", "Выручка", "Прибыль", "Убыток",
        "Дивиденд", "Процент", "Инфляция", "Бюджет", "Долг");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a valid English term")
    void generateValid() {
        assertTrue(EN.contains(new FinancialTermGenerator().generate()));
    }

    @Test
    @DisplayName("default generate() can surface every term over many draws")
    void generateCoversAll() {
        FinancialTermGenerator gen = new FinancialTermGenerator(GeneratorConfig.builder().seed(5L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(EN, seen);
    }

    @Test
    @DisplayName("Russian locale generates Russian term names")
    void russian() {
        FinancialTermGenerator gen = new FinancialTermGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_TERMS.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("unmapped locale falls back to English terms")
    void unmappedFallsBackToEnglish() {
        assertTrue(EN.contains(new FinancialTermGenerator(Locale.of("is", "IS")).generate()));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new FinancialTermGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new FinancialTermGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new FinancialTermGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new FinancialTermGenerator((Locale) null));
    }

    @Nested
    @DisplayName("FinancialTermDataRegistry")
    class Registry {

        @Test
        @DisplayName("registry honors built-ins, custom providers, and rejects null/unknown")
        void registry() {
            assertTrue(FinancialTermDataRegistry.isRegistered(RU));
            assertTrue(FinancialTermDataRegistry.isRegistered(Locale.of("ru"))); // language-only
            assertFalse(FinancialTermDataRegistry.isRegistered(Locale.of("is", "IS")));
            assertFalse(FinancialTermDataRegistry.isRegistered(null));

            assertNotNull(FinancialTermDataRegistry.forLocale(RU));
            assertNotNull(FinancialTermDataRegistry.forLocale(Locale.of("ru", "XX"))); // language fallback
            assertNull(FinancialTermDataRegistry.forLocale(Locale.of("is")));
            assertNull(FinancialTermDataRegistry.forLocale(null));
            assertTrue(FinancialTermDataRegistry.registeredKeys().contains("ru_RU"));

            FinancialTermDataProvider custom = new FinancialTermDataProvider() {
                @Override
                public Locale getLocale() {
                    return Locale.of("zz");
                }

                @Override
                public List<String> getTerms() {
                    return List.of("Arbitrage");
                }
            };
            FinancialTermDataRegistry.register(custom);
            assertEquals("Arbitrage", new FinancialTermGenerator(Locale.of("zz")).generate());
            assertThrows(NullPointerException.class, () -> FinancialTermDataRegistry.register(null));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofFinancialTerm (default / locale / config) produce valid terms")
        void facade() {
            assertTrue(EN.contains(Generators.ofFinancialTerm().generate()));
            assertTrue(RU_TERMS.contains(Generators.ofFinancialTerm(RU).generate()));
            assertTrue(EN.contains(
                Generators.ofFinancialTerm(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
