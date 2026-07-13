/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

@DisplayName("LastNameGenerator")
class LastNameGeneratorTest {

    // ── Default / US ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses Locale.US")
    void defaultConstructorUsesUsLocale() {
        LastNameGenerator gen = new LastNameGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("generate() returns a non-null, non-empty string")
    void generateReturnsNonEmpty() {
        LastNameGenerator gen = new LastNameGenerator();
        String name = gen.generate();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test
    @DisplayName("generate() returns a value from the configured locale's name list")
    void generateReturnsKnownName() {
        LastNameGenerator gen = new LastNameGenerator(Locale.US);
        Set<String> usExpected = Set.of(new BuiltInLastNameDataProvider(SupportedLocale.EN_US).getLastNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(usExpected.contains(gen.generate()),
                       "Generated name not in EN_US list: " + gen.generate());
        }
    }

    // ── Locale variety ────────────────────────────────────────────────────────

    @Test
    @DisplayName("German locale produces German last names")
    void germanLastNames() {
        LastNameGenerator gen = new LastNameGenerator(Locale.GERMANY);
        Set<String> deExpected = Set.of(new BuiltInLastNameDataProvider(SupportedLocale.DE_DE).getLastNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(deExpected.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("Japanese locale produces Japanese last names")
    void japaneseLastNames() {
        LastNameGenerator gen = new LastNameGenerator(Locale.JAPAN);
        Set<String> jaExpected = Set.of(new BuiltInLastNameDataProvider(SupportedLocale.JA_JP).getLastNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(jaExpected.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("French locale produces French last names")
    void frenchLastNames() {
        LastNameGenerator gen = new LastNameGenerator(Locale.FRANCE);
        Set<String> frExpected = Set.of(new BuiltInLastNameDataProvider(SupportedLocale.FR_FR).getLastNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(frExpected.contains(gen.generate()));
        }
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("seeded generator produces identical sequences")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        LastNameGenerator a = new LastNameGenerator(cfg);
        LastNameGenerator b = new LastNameGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLocale() returns the configured locale")
    void getLocale() {
        LastNameGenerator gen = new LastNameGenerator(Locale.UK);
        assertEquals(Locale.of("en", "GB"), gen.getLocale());
    }

    @Test
    @DisplayName("getLastNameCount() matches the locale's array length")
    void getLastNameCount() {
        LastNameGenerator gen = new LastNameGenerator(Locale.US);
        assertEquals(new BuiltInLastNameDataProvider(SupportedLocale.EN_US).getLastNames().length, gen.getLastNameCount());
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for a registered locale")
    void isLocaleExplicitlySupported() {
        assertTrue(new LastNameGenerator(Locale.US).isLocaleExplicitlySupported());
    }

    // ── Unsupported locale ────────────────────────────────────────────────────

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        assertThrows(UnsupportedOperationException.class,
                     () -> new LastNameGenerator(Locale.of("xx", "YY")));
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns the requested number of last names")
    void generateList() {
        List<String> names = new LastNameGenerator().generateList(10);
        assertEquals(10, names.size());
        names.forEach(n -> assertFalse(n.isEmpty()));
    }

    @Test
    @DisplayName("stream() produces on-demand values")
    void streamProducesValues() {
        List<String> names = new LastNameGenerator().stream().limit(15).toList();
        assertEquals(15, names.size());
    }

    // ── Registry extensibility ────────────────────────────────────────────────

    @Test
    @DisplayName("all 10 built-in locales produce non-empty last names")
    void allBuiltInLocalesProduceValues() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            LastNameGenerator gen = new LastNameGenerator(supportedLocale.locale());
            String name = gen.generate();
            assertNotNull(name, "Null for " + supportedLocale);
            assertFalse(name.isEmpty(), "Empty for " + supportedLocale);
        }
    }

    // ── All built-in locales covered ──────────────────────────────────────────

    @Test
    @DisplayName("all built-in locales produce variety of values over many samples")
    void allBuiltInLocalesProduceVariety() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            LastNameGenerator gen = new LastNameGenerator(supportedLocale.locale());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 200; i++) seen.add(gen.generate());
            assertTrue(seen.size() > 1, "No variety for " + supportedLocale);
        }
    }


    @Nested
    @DisplayName("LastNameDataRegistry extensibility")
    class RegistryTest {

        @Test
        @DisplayName("isRegistered(null) returns false")
        void isRegisteredNullReturnsFalse() {
            assertFalse(LastNameDataRegistry.isRegistered(null));
        }

        @Test
        @DisplayName("forLocale(null) returns null")
        void forLocaleNullReturnsNull() {
            assertNull(LastNameDataRegistry.forLocale(null));
        }

        @Test
        @DisplayName("isRegistered returns false for unknown locale")
        void isRegisteredUnknownLocale() {
            assertFalse(LastNameDataRegistry.isRegistered(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale returns null for unknown locale")
        void forLocaleUnknownReturnsNull() {
            assertNull(LastNameDataRegistry.forLocale(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale falls back to language-level entry for unknown country")
        void forLocaleLanguageFallback() {
            LastNameDataProvider provider = LastNameDataRegistry.forLocale(Locale.of("en", "ZZ"));
            assertNotNull(provider);
        }

        @Test
        @DisplayName("isRegistered with language-only locale checks language-level entry")
        void isRegisteredWithLanguageOnlyLocale() {
            assertTrue(LastNameDataRegistry.isRegistered(Locale.of("en")));
        }

        @Test
        @DisplayName("forLocale with language-only locale returns language-level provider")
        void forLocaleWithLanguageOnlyLocale() {
            assertNotNull(LastNameDataRegistry.forLocale(Locale.of("en")));
        }
    }
}
