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

import java.util.Collections;
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

@DisplayName("FirstNameGenerator")
class FirstNameGeneratorTest {

    // ── Default / US ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses Locale.US")
    void defaultConstructorUsesUsLocale() {
        FirstNameGenerator gen = new FirstNameGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("generate() returns a non-null, non-empty string")
    void generateReturnsNonEmpty() {
        FirstNameGenerator gen = new FirstNameGenerator();
        String name = gen.generate();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test
    @DisplayName("generate() returns a value from the configured locale's name lists")
    void generateReturnsKnownName() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.US);
        FirstNameDataProvider provider = new BuiltInFirstNameDataProvider(SupportedLocale.EN_US);
        Set<String> allUS = new HashSet<>();
        Collections.addAll(allUS, provider.getMaleFirstNames());
        Collections.addAll(allUS, provider.getFemaleFirstNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(allUS.contains(gen.generate()),
                       "Generated name not in EN_US list: " + gen.generate());
        }
    }

    // ── Gender-specific generation ─────────────────────────────────────────

    @Test
    @DisplayName("generate(MALE) returns only male names")
    void generateMaleReturnsOnlyMaleNames() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.US);
        Set<String> maleNames = Set.of(new BuiltInFirstNameDataProvider(SupportedLocale.EN_US).getMaleFirstNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(maleNames.contains(gen.generate(Gender.MALE)),
                       "Generated name not in male list");
        }
    }

    @Test
    @DisplayName("generate(FEMALE) returns only female names")
    void generateFemaleReturnsOnlyFemaleNames() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.US);
        Set<String> femaleNames = Set.of(new BuiltInFirstNameDataProvider(SupportedLocale.EN_US).getFemaleFirstNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(femaleNames.contains(gen.generate(Gender.FEMALE)),
                       "Generated name not in female list");
        }
    }

    @Test
    @DisplayName("generate(null) throws NullPointerException")
    void generateNullGenderThrows() {
        FirstNameGenerator gen = new FirstNameGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate(null));
    }

    // ── Locale variety ────────────────────────────────────────────────────────

    @Test
    @DisplayName("German locale produces German names")
    void germanNames() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.GERMANY);
        Set<String> allDE = new HashSet<>();
        FirstNameDataProvider deProvider = new BuiltInFirstNameDataProvider(SupportedLocale.DE_DE);
        Collections.addAll(allDE, deProvider.getMaleFirstNames());
        Collections.addAll(allDE, deProvider.getFemaleFirstNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(allDE.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("Japanese locale produces Japanese names")
    void japaneseNames() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.JAPAN);
        Set<String> allJA = new HashSet<>();
        FirstNameDataProvider jaProvider = new BuiltInFirstNameDataProvider(SupportedLocale.JA_JP);
        Collections.addAll(allJA, jaProvider.getMaleFirstNames());
        Collections.addAll(allJA, jaProvider.getFemaleFirstNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(allJA.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("French locale produces French names")
    void frenchNames() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.FRANCE);
        Set<String> allFR = new HashSet<>();
        FirstNameDataProvider frProvider = new BuiltInFirstNameDataProvider(SupportedLocale.FR_FR);
        Collections.addAll(allFR, frProvider.getMaleFirstNames());
        Collections.addAll(allFR, frProvider.getFemaleFirstNames());
        for (int i = 0; i < 50; i++) {
            assertTrue(allFR.contains(gen.generate()));
        }
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("seeded generator produces identical sequences")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        FirstNameGenerator a = new FirstNameGenerator(cfg);
        FirstNameGenerator b = new FirstNameGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("seeded gender-specific generation is reproducible")
    void seededGenderReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(99L).build();
        FirstNameGenerator a = new FirstNameGenerator(cfg);
        FirstNameGenerator b = new FirstNameGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(Gender.MALE), b.generate(Gender.MALE));
            assertEquals(a.generate(Gender.FEMALE), b.generate(Gender.FEMALE));
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLocale() returns the configured locale")
    void getLocale() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.UK);
        assertEquals(Locale.of("en", "GB"), gen.getLocale());
    }

    @Test
    @DisplayName("getMaleNameCount() matches the locale's male array length")
    void getMaleNameCount() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.US);
        assertEquals(new BuiltInFirstNameDataProvider(SupportedLocale.EN_US).getMaleFirstNames().length, gen.getMaleNameCount());
    }

    @Test
    @DisplayName("getFemaleNameCount() matches the locale's female array length")
    void getFemaleNameCount() {
        FirstNameGenerator gen = new FirstNameGenerator(Locale.US);
        assertEquals(new BuiltInFirstNameDataProvider(SupportedLocale.EN_US).getFemaleFirstNames().length, gen.getFemaleNameCount());
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for a registered locale")
    void isLocaleExplicitlySupported() {
        assertTrue(new FirstNameGenerator(Locale.US).isLocaleExplicitlySupported());
    }

    // ── Unsupported locale ────────────────────────────────────────────────────

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        assertThrows(UnsupportedOperationException.class,
                     () -> new FirstNameGenerator(Locale.of("xx", "YY")));
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns the requested number of names")
    void generateList() {
        List<String> names = new FirstNameGenerator().generateList(10);
        assertEquals(10, names.size());
        names.forEach(n -> assertFalse(n.isEmpty()));
    }

    @Test
    @DisplayName("stream() produces on-demand values")
    void streamProducesValues() {
        List<String> names = new FirstNameGenerator().stream().limit(15).toList();
        assertEquals(15, names.size());
    }

    // ── Registry extensibility ────────────────────────────────────────────────

    @Test
    @DisplayName("LocaleTextResourceLoader throws when resource path does not exist")
    void nameResourceLoaderThrowsForMissingResource() {
        assertThrows(IllegalStateException.class,
                     () -> LocaleTextResourceLoader.load("krandom/names/nonexistent_locale.txt"));
    }

    // ── LocaleTextResourceLoader ──────────────────────────────────────────────

    @Test
    @DisplayName("all 10 built-in locales produce non-empty names")
    void allBuiltInLocalesProduceValues() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            FirstNameGenerator gen = new FirstNameGenerator(supportedLocale.locale());
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
            FirstNameGenerator gen = new FirstNameGenerator(supportedLocale.locale());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 200; i++) seen.add(gen.generate());
            assertTrue(seen.size() > 1, "No variety for " + supportedLocale);
        }
    }

    @Test
    @DisplayName("all built-in locales produce both male and female names")
    void allBuiltInLocalesProduceBothGenders() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            FirstNameGenerator gen = new FirstNameGenerator(supportedLocale.locale());
            String male = gen.generate(Gender.MALE);
            String female = gen.generate(Gender.FEMALE);
            assertNotNull(male, "Null male for " + supportedLocale);
            assertNotNull(female, "Null female for " + supportedLocale);
            assertFalse(male.isEmpty(), "Empty male for " + supportedLocale);
            assertFalse(female.isEmpty(), "Empty female for " + supportedLocale);
        }
    }


    @Nested
    @DisplayName("FirstNameDataRegistry extensibility")
    class RegistryTest {

        @Test
        @DisplayName("isRegistered(null) returns false")
        void isRegisteredNullReturnsFalse() {
            assertFalse(FirstNameDataRegistry.isRegistered(null));
        }

        @Test
        @DisplayName("forLocale(null) returns null")
        void forLocaleNullReturnsNull() {
            assertNull(FirstNameDataRegistry.forLocale(null));
        }

        @Test
        @DisplayName("isRegistered returns false for unknown locale")
        void isRegisteredUnknownLocale() {
            assertFalse(FirstNameDataRegistry.isRegistered(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale returns null for unknown locale")
        void forLocaleUnknownReturnsNull() {
            assertNull(FirstNameDataRegistry.forLocale(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale falls back to language-level entry for unknown country")
        void forLocaleLanguageFallback() {
            FirstNameDataProvider provider = FirstNameDataRegistry.forLocale(Locale.of("en", "ZZ"));
            assertNotNull(provider);
        }

        @Test
        @DisplayName("isRegistered with language-only locale checks language-level entry")
        void isRegisteredWithLanguageOnlyLocale() {
            assertTrue(FirstNameDataRegistry.isRegistered(Locale.of("en")));
        }

        @Test
        @DisplayName("forLocale with language-only locale returns language-level provider")
        void forLocaleWithLanguageOnlyLocale() {
            assertNotNull(FirstNameDataRegistry.forLocale(Locale.of("en")));
        }
    }
}
