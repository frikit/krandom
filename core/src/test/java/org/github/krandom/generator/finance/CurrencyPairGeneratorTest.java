/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrencyPairGenerator")
class CurrencyPairGeneratorTest {

    // ── Constructors ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor creates generator")
    void defaultConstructor() {
        assertNotNull(new CurrencyPairGenerator());
    }

    @Test
    @DisplayName("config constructor accepts valid config")
    void configConstructor() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        CurrencyPairGenerator gen = new CurrencyPairGenerator(cfg);
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new CurrencyPairGenerator(null));
    }

    // ── generate() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("generate() returns BASE/QUOTE formatted string")
    void generateFormat() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 50; i++) {
            String pair = gen.generate();
            assertTrue(pair.matches("[A-Z]{3}/[A-Z]{3}"),
                    "Expected BASE/QUOTE format, got: " + pair);
        }
    }

    @Test
    @DisplayName("generate() base and quote are always different")
    void generateBaseDifferentFromQuote() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 200; i++) {
            String pair = gen.generate();
            String[] parts = pair.split("/");
            assertNotEquals(parts[0], parts[1], "Base and quote must differ: " + pair);
        }
    }

    @Test
    @DisplayName("generate() produces variety of pairs")
    void generateVariety() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        Set<String> pairs = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            pairs.add(gen.generate());
        }
        assertTrue(pairs.size() > 20, "Should produce many different pairs");
    }

    // ── generate(Locale) ─────────────────────────────────────────────────────

    @Test
    @DisplayName("generate(Locale.US) base is always USD")
    void generateLocaleUsBase() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 100; i++) {
            String pair = gen.generate(Locale.US);
            assertTrue(pair.startsWith("USD/"), "Expected USD as base for en_US, got: " + pair);
        }
    }

    @Test
    @DisplayName("generate(Locale.JAPAN) base is always JPY")
    void generateLocaleJapanBase() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 50; i++) {
            String pair = gen.generate(Locale.JAPAN);
            assertTrue(pair.startsWith("JPY/"), "Expected JPY as base, got: " + pair);
        }
    }

    @Test
    @DisplayName("generate(Locale.GERMANY) base is always EUR")
    void generateLocaleGermanyBase() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 50; i++) {
            String pair = gen.generate(Locale.GERMANY);
            assertTrue(pair.startsWith("EUR/"), "Expected EUR as base for de_DE, got: " + pair);
        }
    }

    @Test
    @DisplayName("generate(Locale.UK) base is always GBP")
    void generateLocaleUkBase() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 50; i++) {
            assertTrue(gen.generate(Locale.UK).startsWith("GBP/"));
        }
    }

    @Test
    @DisplayName("generate(unknown locale) falls back to random pair")
    void generateUnknownLocaleFallback() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 30; i++) {
            String pair = gen.generate(Locale.of("xx", "XX"));
            assertTrue(pair.matches("[A-Z]{3}/[A-Z]{3}"), "Should still be valid pair");
            String[] parts = pair.split("/");
            assertNotEquals(parts[0], parts[1]);
        }
    }

    @Test
    @DisplayName("generate(null locale) falls back to random pair")
    void generateNullLocaleFallback() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 30; i++) {
            String pair = gen.generate(null);
            assertTrue(pair.matches("[A-Z]{3}/[A-Z]{3}"));
            String[] parts = pair.split("/");
            assertNotEquals(parts[0], parts[1]);
        }
    }

    // ── generateWithInfo() ────────────────────────────────────────────────────

    @Test
    @DisplayName("generateWithInfo() returns non-null CurrencyPair")
    void generateWithInfoNotNull() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        CurrencyPair pair = gen.generateWithInfo();
        assertNotNull(pair);
        assertNotNull(pair.base());
        assertNotNull(pair.quote());
    }

    @Test
    @DisplayName("generateWithInfo() base and quote have valid ISO codes")
    void generateWithInfoValidCodes() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 50; i++) {
            CurrencyPair pair = gen.generateWithInfo();
            assertTrue(pair.base().code().matches("[A-Z]{3}"));
            assertTrue(pair.quote().code().matches("[A-Z]{3}"));
        }
    }

    @Test
    @DisplayName("generateWithInfo() toPairString matches generate()")
    void generateWithInfoMatchesGenerate() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(1L).build();
        CurrencyPairGenerator gen1 = new CurrencyPairGenerator(cfg);
        CurrencyPairGenerator gen2 = new CurrencyPairGenerator(
                GeneratorConfig.builder().seed(1L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.generate(), gen2.generateWithInfo().toPairString());
        }
    }

    @Test
    @DisplayName("generateWithInfo() includes full currency details")
    void generateWithInfoFullDetails() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        CurrencyPair pair = gen.generateWithInfo();
        assertFalse(pair.base().name().isEmpty());
        assertFalse(pair.base().symbol().isEmpty());
        assertFalse(pair.base().numericCode().isEmpty());
        assertFalse(pair.quote().name().isEmpty());
        assertFalse(pair.quote().symbol().isEmpty());
    }

    // ── generateWithInfo(Locale) ──────────────────────────────────────────────

    @Test
    @DisplayName("generateWithInfo(Locale.US) base is USD")
    void generateWithInfoLocaleUsBase() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 30; i++) {
            CurrencyPair pair = gen.generateWithInfo(Locale.US);
            assertEquals("USD", pair.base().code());
            assertNotEquals("USD", pair.quote().code());
        }
    }

    @Test
    @DisplayName("generateWithInfo(Locale.FRANCE) base is EUR, quote differs")
    void generateWithInfoLocaleFranceBase() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 30; i++) {
            CurrencyPair pair = gen.generateWithInfo(Locale.FRANCE);
            assertEquals("EUR", pair.base().code());
            assertNotEquals("EUR", pair.quote().code());
        }
    }

    @Test
    @DisplayName("generateWithInfo(null) returns random pair")
    void generateWithInfoNullLocale() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        CurrencyPair pair = gen.generateWithInfo(null);
        assertNotNull(pair);
        assertNotEquals(pair.base().code(), pair.quote().code());
    }

    // ── CurrencyPair record ───────────────────────────────────────────────────

    @Test
    @DisplayName("CurrencyPair.toPairString() returns BASE/QUOTE")
    void currencyPairToPairString() {
        CurrencyInfo usd = Currency.USD.toInfo();
        CurrencyInfo eur = Currency.EUR.toInfo();
        CurrencyPair pair = new CurrencyPair(usd, eur);
        assertEquals("USD/EUR", pair.toPairString());
    }

    @Test
    @DisplayName("CurrencyPair rejects null base")
    void currencyPairNullBaseThrows() {
        assertThrows(NullPointerException.class,
                () -> new CurrencyPair(null, Currency.USD.toInfo()));
    }

    @Test
    @DisplayName("CurrencyPair rejects null quote")
    void currencyPairNullQuoteThrows() {
        assertThrows(NullPointerException.class,
                () -> new CurrencyPair(Currency.USD.toInfo(), null));
    }

    @Test
    @DisplayName("CurrencyPair rejects same base and quote")
    void currencyPairSameBaseAndQuoteThrows() {
        CurrencyInfo usd = Currency.USD.toInfo();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new CurrencyPair(usd, usd));
        assertTrue(ex.getMessage().contains("USD"));
    }

    // ── do-while loop retry branch ────────────────────────────────────────────

    @Test
    @DisplayName("retry loop branch is covered — both quote==base and quote!=base occur")
    void retryLoopBothBranches() {
        // With only 44 currencies, over 2000 calls the collision branch is near-certain.
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (int i = 0; i < 2000; i++) {
            CurrencyPair pair = gen.generateWithInfo();
            assertNotEquals(pair.base().code(), pair.quote().code());
        }
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("seeded generators produce identical pair sequences")
    void seededReproducibility() {
        GeneratorConfig cfg1 = GeneratorConfig.builder().seed(77L).build();
        GeneratorConfig cfg2 = GeneratorConfig.builder().seed(77L).build();
        CurrencyPairGenerator gen1 = new CurrencyPairGenerator(cfg1);
        CurrencyPairGenerator gen2 = new CurrencyPairGenerator(cfg2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different pair sequences")
    void differentSeeds() {
        CurrencyPairGenerator gen1 = new CurrencyPairGenerator(
                GeneratorConfig.builder().seed(1L).build());
        CurrencyPairGenerator gen2 = new CurrencyPairGenerator(
                GeneratorConfig.builder().seed(2L).build());
        assertNotEquals(gen1.generateList(50), gen2.generateList(50));
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns correct count of valid pairs")
    void generateListCount() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        List<String> list = gen.generateList(25);
        assertEquals(25, list.size());
        list.forEach(p -> assertTrue(p.matches("[A-Z]{3}/[A-Z]{3}")));
    }

    @Test
    @DisplayName("stream generates continuous valid pairs")
    void streamGeneration() {
        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        List<String> list = gen.stream().limit(20).toList();
        assertEquals(20, list.size());
        list.forEach(p -> assertTrue(p.matches("[A-Z]{3}/[A-Z]{3}")));
    }

    // ── All 10 locales as base ────────────────────────────────────────────────

    @Test
    @DisplayName("all 10 supported locales produce pairs with locale's currency as base")
    void allLocalesProduceCorrectBase() {
        record LocalePair(Locale locale, String expectedBase) {}
        List<LocalePair> cases = List.of(
                new LocalePair(Locale.US,                    "USD"),
                new LocalePair(Locale.UK,                    "GBP"),
                new LocalePair(Locale.of("en", "AU"),        "AUD"),
                new LocalePair(Locale.GERMANY,               "EUR"),
                new LocalePair(Locale.FRANCE,                "EUR"),
                new LocalePair(Locale.of("es", "ES"),        "EUR"),
                new LocalePair(Locale.ITALY,                 "EUR"),
                new LocalePair(Locale.of("pt", "BR"),        "BRL"),
                new LocalePair(Locale.JAPAN,                 "JPY"),
                new LocalePair(Locale.of("zh", "CN"),        "CNY")
        );

        CurrencyPairGenerator gen = new CurrencyPairGenerator();
        for (LocalePair lp : cases) {
            CurrencyPair pair = gen.generateWithInfo(lp.locale());
            assertEquals(lp.expectedBase(), pair.base().code(),
                    "Wrong base for locale " + lp.locale());
            assertNotEquals(lp.expectedBase(), pair.quote().code(),
                    "Quote must differ from base for locale " + lp.locale());
        }
    }
}
