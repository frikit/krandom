/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MoneyGenerator")
class MoneyGeneratorTest {

    // ── Constructors ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses Locale.US")
    void defaultConstructorUsesUS() {
        MoneyGenerator gen = new MoneyGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("locale constructor stores locale")
    void localeConstructor() {
        MoneyGenerator gen = new MoneyGenerator(Locale.GERMANY);
        assertEquals(Locale.GERMANY, gen.getLocale());
    }

    @Test
    @DisplayName("config constructor stores locale from config")
    void configConstructor() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.JAPAN).seed(42L).build();
        MoneyGenerator gen = new MoneyGenerator(config);
        assertEquals(Locale.JAPAN, gen.getLocale());
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new MoneyGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        assertThrows(NullPointerException.class, () -> new MoneyGenerator((Locale) null));
    }

    // ── generate() — locale-aware ─────────────────────────────────────────────

    @Test
    @DisplayName("generate() returns non-null, non-empty string")
    void generateNotEmpty() {
        MoneyGenerator gen = new MoneyGenerator();
        String result = gen.generate();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("generate() for en_US starts with dollar sign")
    void generateUsStartsWithDollar() {
        MoneyGenerator gen = new MoneyGenerator();
        for (int i = 0; i < 50; i++) {
            assertTrue(gen.generate().startsWith("$"), "Expected $ prefix for en_US");
        }
    }

    @Test
    @DisplayName("generate() for de_DE contains euro symbol")
    void generateGermanyContainsEuro() {
        MoneyGenerator gen = new MoneyGenerator(Locale.GERMANY);
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generate().contains("€"), "Expected € for de_DE");
        }
    }

    @Test
    @DisplayName("generate() for fr_FR contains euro symbol")
    void generateFranceContainsEuro() {
        MoneyGenerator gen = new MoneyGenerator(Locale.FRANCE);
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generate().contains("€"), "Expected € for fr_FR");
        }
    }

    @Test
    @DisplayName("generate() for ja_JP contains fullwidth yen symbol and no decimal point")
    void generateJapanYen() {
        MoneyGenerator gen = new MoneyGenerator(Locale.JAPAN);
        for (int i = 0; i < 20; i++) {
            String result = gen.generate();
            // Java's NumberFormat uses U+FFE5 (fullwidth yen ￥) for ja_JP, not U+00A5 (¥)
            assertTrue(result.contains("\uFFE5"), "Expected ￥ (U+FFE5) for ja_JP, got: " + result);
            assertFalse(result.contains("."), "JPY should have no decimal point");
        }
    }

    @Test
    @DisplayName("generate() for pt_BR contains BRL symbol")
    void generateBrazilBrl() {
        MoneyGenerator gen = new MoneyGenerator(new Locale("pt", "BR"));
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generate().contains("R$"), "Expected R$ for pt_BR");
        }
    }

    @Test
    @DisplayName("generate() for zh_CN contains yuan symbol")
    void generateChinaYuan() {
        MoneyGenerator gen = new MoneyGenerator(new Locale("zh", "CN"));
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generate().contains("¥"), "Expected ¥ for zh_CN");
        }
    }

    @Test
    @DisplayName("generate() for en_GB contains pound symbol")
    void generateGbPound() {
        MoneyGenerator gen = new MoneyGenerator(Locale.UK);
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generate().contains("£"), "Expected £ for en_GB");
        }
    }

    // ── generate(double max) ──────────────────────────────────────────────────

    @Test
    @DisplayName("generate(max) returns value within max")
    void generateWithMaxInRange() {
        MoneyGenerator gen =
                new MoneyGenerator(GeneratorConfig.builder().seed(1L).build());
        for (int i = 0; i < 100; i++) {
            String result = gen.generate(100.0);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    @Test
    @DisplayName("an amount that the currency's rounding would show at max is shown below max")
    void amountRoundingUpToMaxStaysBelowIt() {
        MoneyGenerator dollars = drawing(0.999, Locale.US);
        MoneyGenerator yen = drawing(0.6, Locale.JAPAN);
        MoneyGenerator largest = drawing(0.9999996, Locale.US);

        assertEquals("$0.99", dollars.generateDollar(1));
        assertEquals("$0.99", dollars.generate(1));
        assertEquals("$0.99", dollars.generatePrice(Locale.US, 1));
        assertEquals(shown(Locale.FRANCE, "EUR", 0.99), dollars.generateEuro(1));
        assertEquals(shown(Locale.JAPAN, "JPY", 0), yen.generate(1));
        assertEquals("$9,999.99", largest.generateDollar());
        assertEquals("$9,999.99", largest.generate());
        assertEquals(shown(Locale.FRANCE, "EUR", 9_999.99), largest.generateEuro());
        assertEquals("$0.00", drawing(0.999, Locale.US).generateDollar(0.005));
    }

    @Test
    @DisplayName("an amount that the currency's rounding shows below max is shown as before")
    void amountRoundingBelowMaxIsShownAsBefore() {
        assertEquals("$0.56", drawing(0.556, Locale.US).generateDollar(1));
        assertEquals("$0.99", drawing(0.994, Locale.US).generateDollar(1));
        assertEquals("$1.00", drawing(0.999, Locale.US).generateDollar(1.001));
        assertEquals(shown(Locale.JAPAN, "JPY", 1), drawing(0.6, Locale.JAPAN).generate(2));
        assertEquals(
                shown(Locale.US, "USD", Double.POSITIVE_INFINITY),
                drawing(0.5, Locale.US).generateDollar(Double.POSITIVE_INFINITY));
    }

    /** A generator whose every draw is {@code fraction} of the maximum. */
    private static MoneyGenerator drawing(double fraction, Locale locale) {
        java.util.Random fixed = new java.util.Random() {
            private static final long serialVersionUID = 1L;

            @Override
            public double nextDouble() {
                return fraction;
            }
        };
        return new MoneyGenerator(
                GeneratorConfig.builder().random(fixed).locale(locale).build());
    }

    private static String shown(Locale locale, String currency, double amount) {
        java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(locale);
        format.setCurrency(java.util.Currency.getInstance(currency));
        return format.format(amount);
    }

    @Test
    @DisplayName("generate(0.0) always returns zero amount")
    void generateWithZeroMax() {
        MoneyGenerator gen = new MoneyGenerator();
        // $0.00 for US locale
        String result = gen.generate(0.0);
        assertTrue(result.contains("0"), "Zero max should produce a zero amount");
    }

    @Test
    @DisplayName("generate(negative max) throws IllegalArgumentException")
    void generateNegativeMaxThrows() {
        MoneyGenerator gen = new MoneyGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1.0));
    }

    // ── generate(Locale) ──────────────────────────────────────────────────────

    @Test
    @DisplayName("generate(Locale.US) produces dollar formatting")
    void generateWithLocaleUsDollar() {
        MoneyGenerator gen = new MoneyGenerator(Locale.GERMANY); // configured as EUR
        // but override to US
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generate(Locale.US).startsWith("$"), "Override locale should win");
        }
    }

    @Test
    @DisplayName("generate(Locale.JAPAN) produces fullwidth yen formatting")
    void generateWithLocaleJapan() {
        MoneyGenerator gen = new MoneyGenerator(); // configured as USD
        for (int i = 0; i < 20; i++) {
            // Java NumberFormat uses U+FFE5 (fullwidth yen ￥) for ja_JP
            assertTrue(gen.generate(Locale.JAPAN).contains("\uFFE5"));
        }
    }

    @Test
    @DisplayName("generate(null locale) throws NullPointerException")
    void generateNullLocaleThrows() {
        MoneyGenerator gen = new MoneyGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate((Locale) null));
    }

    @Test
    @DisplayName("generate(unknown locale) falls back to dollar formatting")
    void generateUnknownLocaleFallsBackToDollar() {
        MoneyGenerator gen = new MoneyGenerator();
        String result = gen.generate(new Locale("xx", "XX"));
        assertTrue(result.startsWith("$"), "Unknown locale should fall back to USD");
    }

    // ── generate(Locale, double max) ──────────────────────────────────────────

    @Test
    @DisplayName("generate(Locale, max) applies locale and max")
    void generateLocaleAndMax() {
        MoneyGenerator gen = new MoneyGenerator();
        for (int i = 0; i < 20; i++) {
            String result = gen.generate(Locale.GERMANY, 500.0);
            assertTrue(result.contains("€"), "Expected € with Germany locale");
        }
    }

    @Test
    @DisplayName("generate(null locale, max) throws NullPointerException")
    void generateNullLocaleWithMaxThrows() {
        MoneyGenerator gen = new MoneyGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate(null, 100.0));
    }

    @Test
    @DisplayName("generate(locale, negative max) throws IllegalArgumentException")
    void generateLocaleNegativeMaxThrows() {
        MoneyGenerator gen = new MoneyGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(Locale.US, -5.0));
    }

    @Test
    @DisplayName("generatePrice aliases delegate to generate variants")
    void generatePriceAliases() {
        GeneratorConfig cfg =
                GeneratorConfig.builder().seed(121L).locale(Locale.US).build();
        MoneyGenerator a = new MoneyGenerator(cfg);
        MoneyGenerator b = new MoneyGenerator(cfg);

        assertEquals(a.generate(), b.generatePrice());
        assertEquals(a.generate(77.0), b.generatePrice(77.0));
        assertEquals(a.generate(Locale.GERMANY), b.generatePrice(Locale.GERMANY));
        assertEquals(a.generate(Locale.FRANCE, 20.0), b.generatePrice(Locale.FRANCE, 20.0));
    }

    // ── generateDollar() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("generateDollar() starts with dollar sign")
    void generateDollarStartsWithDollar() {
        MoneyGenerator gen = new MoneyGenerator();
        for (int i = 0; i < 50; i++) {
            assertTrue(gen.generateDollar().startsWith("$"), "Expected $ prefix");
        }
    }

    @Test
    @DisplayName("generateDollar() always USD even when locale is de_DE")
    void generateDollarIgnoresLocale() {
        MoneyGenerator gen = new MoneyGenerator(Locale.GERMANY);
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generateDollar().startsWith("$"), "generateDollar() ignores locale");
        }
    }

    @Test
    @DisplayName("generateDollar(max) produces dollar-formatted value")
    void generateDollarWithMax() {
        MoneyGenerator gen = new MoneyGenerator();
        for (int i = 0; i < 30; i++) {
            assertTrue(gen.generateDollar(50.0).startsWith("$"));
        }
    }

    @Test
    @DisplayName("generateDollar(0.0) produces $0.00")
    void generateDollarZeroMax() {
        MoneyGenerator gen = new MoneyGenerator();
        assertEquals("$0.00", gen.generateDollar(0.0));
    }

    @Test
    @DisplayName("generateDollar(negative max) throws IllegalArgumentException")
    void generateDollarNegativeMaxThrows() {
        MoneyGenerator gen = new MoneyGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateDollar(-10.0));
    }

    // ── generateEuro() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateEuro() contains euro symbol")
    void generateEuroContainsEuroSymbol() {
        MoneyGenerator gen = new MoneyGenerator();
        for (int i = 0; i < 50; i++) {
            assertTrue(gen.generateEuro().contains("€"), "Expected € in euro output");
        }
    }

    @Test
    @DisplayName("generateEuro() always EUR even when locale is en_US")
    void generateEuroIgnoresLocale() {
        MoneyGenerator gen = new MoneyGenerator(Locale.US);
        for (int i = 0; i < 20; i++) {
            assertTrue(gen.generateEuro().contains("€"), "generateEuro() ignores locale");
        }
    }

    @Test
    @DisplayName("generateEuro(max) produces euro-formatted value")
    void generateEuroWithMax() {
        MoneyGenerator gen = new MoneyGenerator();
        for (int i = 0; i < 30; i++) {
            assertTrue(gen.generateEuro(500.0).contains("€"));
        }
    }

    @Test
    @DisplayName("generateEuro(0.0) produces zero euro amount")
    void generateEuroZeroMax() {
        MoneyGenerator gen = new MoneyGenerator();
        String result = gen.generateEuro(0.0);
        assertTrue(result.contains("€") && result.contains("0"), "Zero euro amount expected");
    }

    @Test
    @DisplayName("generateEuro(negative max) throws IllegalArgumentException")
    void generateEuroNegativeMaxThrows() {
        MoneyGenerator gen = new MoneyGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateEuro(-10.0));
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("seeded generators produce identical sequences")
    void seededReproducibility() {
        GeneratorConfig cfg1 = GeneratorConfig.builder().seed(12345L).build();
        GeneratorConfig cfg2 = GeneratorConfig.builder().seed(12345L).build();
        MoneyGenerator gen1 = new MoneyGenerator(cfg1);
        MoneyGenerator gen2 = new MoneyGenerator(cfg2);

        List<String> list1 = gen1.generateList(30);
        List<String> list2 = gen2.generateList(30);
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        MoneyGenerator gen1 =
                new MoneyGenerator(GeneratorConfig.builder().seed(1L).build());
        MoneyGenerator gen2 =
                new MoneyGenerator(GeneratorConfig.builder().seed(2L).build());
        assertNotEquals(gen1.generateList(30), gen2.generateList(30));
    }

    @Test
    @DisplayName("seeded dollar generation is reproducible")
    void seededDollarReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(99L).build();
        MoneyGenerator gen1 = new MoneyGenerator(cfg);
        MoneyGenerator gen2 =
                new MoneyGenerator(GeneratorConfig.builder().seed(99L).build());
        assertEquals(gen1.generateDollar(), gen2.generateDollar());
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns correct count")
    void generateListCount() {
        MoneyGenerator gen = new MoneyGenerator();
        List<String> list = gen.generateList(15);
        assertEquals(15, list.size());
        list.forEach(s -> assertTrue(s.startsWith("$")));
    }

    @Test
    @DisplayName("stream generates continuous values")
    void streamGeneration() {
        MoneyGenerator gen = new MoneyGenerator(Locale.GERMANY);
        List<String> list = gen.stream().limit(10).toList();
        assertEquals(10, list.size());
        list.forEach(s -> assertTrue(s.contains("€")));
    }

    // ── All 10 supported locales ──────────────────────────────────────────────

    @Test
    @DisplayName("all 10 supported locales produce non-null formatted amounts")
    void allSupportedLocales() {
        Locale[] locales = {
            Locale.US,
            Locale.UK,
            new Locale("en", "AU"),
            Locale.GERMANY,
            Locale.FRANCE,
            new Locale("es", "ES"),
            Locale.ITALY,
            new Locale("pt", "BR"),
            Locale.JAPAN,
            new Locale("zh", "CN")
        };
        for (Locale locale : locales) {
            MoneyGenerator gen = new MoneyGenerator(locale);
            String result = gen.generate();
            assertNotNull(result, "Should produce value for " + locale);
            assertFalse(result.isEmpty(), "Should produce non-empty value for " + locale);
        }
    }
}
