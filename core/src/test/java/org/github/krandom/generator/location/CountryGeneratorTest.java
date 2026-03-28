/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CountryGenerator")
class CountryGeneratorTest {

    @Test
    @DisplayName("default config uses US locale with English country names")
    void defaultConfig() {
        CountryGenerator gen = new CountryGenerator(GeneratorConfig.defaults());

        assertEquals(Locale.US, gen.getLocale());
        assertTrue(gen.getCountryCount() > 0);
    }

    @Test
    @DisplayName("generate() returns non-null, non-empty country name")
    void generateNotEmpty() {
        CountryGenerator gen = new CountryGenerator(Locale.US);
        String country = gen.generate();

        assertNotNull(country);
        assertFalse(country.isEmpty());
    }

    @Test
    @DisplayName("generateCode() returns ISO alpha-2 code")
    void generateCode() {
        CountryGenerator gen = new CountryGenerator(Locale.US);
        String code = gen.generateCode();
        assertNotNull(code);
        assertTrue(code.matches("[A-Z]{2}"));
        assertTrue(Arrays.asList(Locale.getISOCountries()).contains(code));
    }

    @Test
    @DisplayName("generateCodeAlpha3() returns ISO alpha-3 code")
    void generateCodeAlpha3() {
        CountryGenerator gen = new CountryGenerator(Locale.US);
        String code = gen.generateCodeAlpha3();
        assertNotNull(code);
        assertTrue(code.matches("[A-Z]{3}"));
    }

    @Test
    @DisplayName("generateCode(format) supports alpha2, alpha3, numeric")
    void generateCodeByFormat() {
        CountryGenerator gen = new CountryGenerator(Locale.US);
        assertTrue(gen.generateCode(CountryGenerator.CountryCodeFormat.ALPHA2).matches("[A-Z]{2}"));
        assertTrue(gen.generateCode(CountryGenerator.CountryCodeFormat.ALPHA3).matches("[A-Z]{3}"));
        assertTrue(gen.generateCode(CountryGenerator.CountryCodeFormat.NUMERIC).matches("\\d{3}"));
        assertThrows(NullPointerException.class, () -> gen.generateCode(null));
    }

    @Test
    @DisplayName("current-country helpers return locale-specific values")
    void currentCountryHelpers() {
        CountryGenerator us = new CountryGenerator(Locale.US);
        assertEquals("US", us.currentCountryCode());
        assertEquals("USA", us.currentCountryCodeAlpha3());
        assertEquals("840", us.currentCountryCodeNumeric());
        assertEquals("US", us.currentCountryCode(CountryGenerator.CountryCodeFormat.ALPHA2));
        assertEquals("USA", us.currentCountryCode(CountryGenerator.CountryCodeFormat.ALPHA3));
        assertEquals("840", us.currentCountryCode(CountryGenerator.CountryCodeFormat.NUMERIC));
        assertFalse(us.currentCountry().isBlank());
    }

    @Test
    @DisplayName("current-country helpers throw when locale has no country")
    void currentCountryHelpersNoCountry() {
        CountryGenerator languageOnly = new CountryGenerator(GeneratorConfig.builder().locale(Locale.ENGLISH).build());
        assertThrows(UnsupportedOperationException.class, languageOnly::currentCountry);
        assertThrows(UnsupportedOperationException.class, languageOnly::currentCountryCode);
        assertThrows(UnsupportedOperationException.class, languageOnly::currentCountryCodeAlpha3);
        assertThrows(UnsupportedOperationException.class, languageOnly::currentCountryCodeNumeric);
        assertThrows(UnsupportedOperationException.class, languageOnly::currentContinent);
        assertThrows(UnsupportedOperationException.class, languageOnly::currentCallingCode);
        assertThrows(UnsupportedOperationException.class, languageOnly::currentTimezone);
    }

    @Test
    @DisplayName("currentCountryCodeAlpha3 throws for unsupported country code")
    void currentCountryCodeAlpha3UnsupportedCountry() {
        Locale locale = Locale.of("en", "ZZ");
        CountryDataRegistry.register(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[]{"Nowhere"};
            }
        });
        CountryGenerator generator = new CountryGenerator(locale);
        assertThrows(UnsupportedOperationException.class, generator::currentCountryCodeAlpha3);
        assertThrows(UnsupportedOperationException.class, generator::currentCountryCodeNumeric);
        assertThrows(UnsupportedOperationException.class, generator::currentContinent);
        assertThrows(UnsupportedOperationException.class, generator::currentCallingCode);
        assertThrows(UnsupportedOperationException.class, generator::currentTimezone);
    }

    @Test
    @DisplayName("continent, calling code and timezone helpers are available")
    void continentCallingTimezone() {
        CountryGenerator us = new CountryGenerator(Locale.US);
        assertEquals("North America", us.currentContinent());
        assertEquals("+1", us.currentCallingCode());
        assertEquals("America/New_York", us.currentTimezone());

        assertFalse(us.generateContinent().isBlank());
        assertTrue(us.generateCallingCode().startsWith("+"));
        assertTrue(us.generateTimezone().contains("/"));
    }

    @Test
    @DisplayName("US locale returns English country names")
    void usLocaleEnglish() {
        CountryGenerator gen = new CountryGenerator(Locale.US);

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        assertTrue(countries.contains("Germany") || countries.contains("France") || countries.contains("Japan"));
        assertTrue(gen.getCountryCount() > 50);
    }

    @Test
    @DisplayName("German locale returns German country names")
    void germanLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.GERMANY);

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        assertTrue(countries.contains("Deutschland") || countries.contains("Frankreich") || countries.contains("Japan"));
        assertTrue(gen.getCountryCount() > 50);
    }

    @Test
    @DisplayName("Japanese locale returns Japanese country names")
    void japaneseLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.JAPAN);

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        // Japanese script
        assertTrue(countries.stream().anyMatch(c -> c.contains("ドイツ") || c.contains("日本") || c.contains("フランス")));
    }

    @Test
    @DisplayName("French locale returns French country names")
    void frenchLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.FRANCE);

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        assertTrue(countries.contains("Allemagne") || countries.contains("France") || countries.contains("Japon"));
    }

    @Test
    @DisplayName("Chinese locale returns Chinese country names")
    void chineseLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.of("zh", "CN"));

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        // Chinese characters
        assertTrue(countries.stream().anyMatch(c -> c.contains("德国") || c.contains("法国") || c.contains("日本")));
    }

    @Test
    @DisplayName("Spanish locale returns Spanish country names")
    void spanishLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.of("es", "ES"));

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        assertTrue(countries.contains("Alemania") || countries.contains("Francia") || countries.contains("España"));
    }

    @Test
    @DisplayName("Italian locale returns Italian country names")
    void italianLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.of("it", "IT"));

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        assertTrue(countries.contains("Germania") || countries.contains("Francia") || countries.contains("Italia"));
    }

    @Test
    @DisplayName("Brazilian Portuguese locale returns Portuguese country names")
    void brazilianPortugueseLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.of("pt", "BR"));

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            countries.add(gen.generate());
        }

        assertTrue(countries.contains("Alemanha") || countries.contains("França") || countries.contains("Japão"));
    }

    @Test
    @DisplayName("en_GB locale has its own country data")
    void ukLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.UK);

        assertNotNull(gen.generate());
        assertTrue(gen.getCountryCount() > 50);
    }

    @Test
    @DisplayName("en_AU locale has its own country data")
    void australianLocale() {
        CountryGenerator gen = new CountryGenerator(Locale.of("en", "AU"));

        assertNotNull(gen.generate());
        assertTrue(gen.getCountryCount() > 50);
    }

    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder().locale(Locale.GERMANY).seed(42L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().locale(Locale.GERMANY).seed(42L).build();

        CountryGenerator gen1 = new CountryGenerator(config1);
        CountryGenerator gen2 = new CountryGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder().seed(111L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().seed(222L).build();

        CountryGenerator gen1 = new CountryGenerator(config1);
        CountryGenerator gen2 = new CountryGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertNotEquals(list1, list2);
    }

    @Test
    @DisplayName("generateList() produces correct count")
    void generateListCount() {
        CountryGenerator gen = new CountryGenerator(Locale.US);
        List<String> countries = gen.generateList(20);

        assertEquals(20, countries.size());
        countries.forEach(c -> {
            assertNotNull(c);
            assertFalse(c.isEmpty());
        });
    }

    @Test
    @DisplayName("stream() generates continuous values")
    void streamGeneration() {
        CountryGenerator gen = new CountryGenerator(Locale.JAPAN);

        List<String> countries = gen.stream().limit(30).toList();

        assertEquals(30, countries.size());
        countries.forEach(c -> {
            assertNotNull(c);
            assertFalse(c.isEmpty());
        });
    }

    @Test
    @DisplayName("getLocale() returns the configured locale")
    void getLocale() {
        assertEquals(Locale.US, new CountryGenerator(Locale.US).getLocale());
        assertEquals(Locale.GERMANY, new CountryGenerator(Locale.GERMANY).getLocale());
        assertEquals(Locale.JAPAN, new CountryGenerator(Locale.JAPAN).getLocale());
    }

    @Test
    @DisplayName("getCountryCount() returns positive value for all built-in locales")
    void countryCountPositive() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            CountryGenerator gen = new CountryGenerator(supportedLocale.locale());
            assertTrue(gen.getCountryCount() > 0,
                    "Locale " + supportedLocale.locale() + " should have countries");
        }
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for all built-in locales")
    void localeSupported() {
        assertTrue(new CountryGenerator(Locale.US).isLocaleExplicitlySupported());
        assertTrue(new CountryGenerator(Locale.UK).isLocaleExplicitlySupported());
        assertTrue(new CountryGenerator(Locale.GERMANY).isLocaleExplicitlySupported());
        assertTrue(new CountryGenerator(Locale.JAPAN).isLocaleExplicitlySupported());
        assertTrue(new CountryGenerator(Locale.FRANCE).isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new CountryGenerator(Locale.of("xx", "YY"))
        );

        assertTrue(ex.getMessage().contains("not supported"));
        assertTrue(ex.getMessage().contains("xx_YY"));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class,
            () -> new CountryGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        assertThrows(NullPointerException.class,
            () -> new CountryGenerator((Locale) null));
    }

    @Test
    @DisplayName("language-only locale falls back to language entry (en -> en_US data)")
    void languageOnlyLocaleFallback() {
        CountryGenerator gen = new CountryGenerator(Locale.of("en"));

        String country = gen.generate();
        assertNotNull(country);
        assertTrue(gen.getCountryCount() > 50);
    }

    @Test
    @DisplayName("generate() produces variety — not always the same value")
    void generateVariety() {
        CountryGenerator gen = new CountryGenerator(Locale.US);

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(gen.generate());
        }

        assertTrue(seen.size() > 10, "Expected variety of countries, got: " + seen.size());
    }

    // ── CountryDataRegistry extensibility ────────────────────────────────────

    @Test
    @DisplayName("custom provider registered for new locale is used by CountryGenerator")
    void customLocaleRegistration() {
        Locale korean = Locale.of("ko", "KR");
        String[] koreanCountries = {"미국", "독일", "프랑스", "일본"};

        CountryDataRegistry.register(new CountryDataProvider() {
            @Override public Locale getLocale() { return korean; }
            @Override public String[] getCountries() { return koreanCountries; }
        });

        CountryGenerator gen = new CountryGenerator(korean);
        assertEquals(4, gen.getCountryCount());

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(gen.generate());
        assertTrue(seen.containsAll(Arrays.asList(koreanCountries)));
    }

    @Test
    @DisplayName("custom provider overrides built-in locale")
    void customProviderOverridesBuiltIn() {
        Locale us = Locale.US;
        String[] custom = {"Foo Land", "Bar Republic"};

        CountryDataRegistry.register(new CountryDataProvider() {
            @Override public Locale getLocale() { return us; }
            @Override public String[] getCountries() { return custom; }
        });

        CountryGenerator gen = new CountryGenerator(us);
        assertEquals(2, gen.getCountryCount());

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) seen.add(gen.generate());
        assertTrue(seen.containsAll(Arrays.asList(custom)));

        // Restore built-in US data so other tests are unaffected.
        CountryDataRegistry.register(new BuiltInCountryDataProvider(SupportedLocale.EN_US));
    }

    @Test
    @DisplayName("registered custom locale appears in registeredKeys()")
    void customLocaleAppearsInKeys() {
        Locale swahili = Locale.of("sw", "TZ");
        CountryDataRegistry.register(new CountryDataProvider() {
            @Override public Locale getLocale() { return swahili; }
            @Override public String[] getCountries() { return new String[]{"Marekani", "Ujerumani"}; }
        });

        assertTrue(CountryDataRegistry.registeredKeys().contains("sw_TZ"));
        assertTrue(CountryDataRegistry.isRegistered(swahili));
    }

    @Test
    @DisplayName("language-only registration serves as fallback for any country variant")
    void languageOnlyFallback() {
        Locale arabic = Locale.of("ar");
        CountryDataRegistry.register(new CountryDataProvider() {
            @Override public Locale getLocale() { return arabic; }
            @Override public String[] getCountries() { return new String[]{"الولايات المتحدة", "ألمانيا"}; }
        });

        // ar_EG should fall back to the "ar" language entry.
        Locale arabicEgypt = Locale.of("ar", "EG");
        assertTrue(CountryDataRegistry.isRegistered(arabicEgypt));

        CountryGenerator gen = new CountryGenerator(arabicEgypt);
        assertEquals(2, gen.getCountryCount());
    }

    @Test
    @DisplayName("register rejects null provider")
    void registerRejectsNull() {
        assertThrows(NullPointerException.class, () -> CountryDataRegistry.register(null));
    }

    @Test
    @DisplayName("isRegistered returns false for null locale")
    void isRegisteredNullReturnsFalse() {
        assertFalse(CountryDataRegistry.isRegistered(null));
    }

    @Test
    @DisplayName("forLocale returns null for null locale")
    void forLocaleNullReturnsNull() {
        assertNull(CountryDataRegistry.forLocale(null));
    }

    @Test
    @DisplayName("isRegistered returns false for unregistered locale")
    void isRegisteredUnknownLocale() {
        assertFalse(CountryDataRegistry.isRegistered(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for completely unknown locale")
    void forLocaleUnknownReturnsNull() {
        assertNull(CountryDataRegistry.forLocale(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns language-level entry when no exact country match exists")
    void forLocaleLanguageFallbackInRegistry() {
        // "en" is registered (language-level entry seeded from EN_US).
        // "en_ZZ" has no exact entry but shares the "en" language key.
        CountryDataProvider provider = CountryDataRegistry.forLocale(Locale.of("en", "ZZ"));
        assertNotNull(provider);
        assertTrue(provider.getCountries().length > 0);
    }

    @Test
    @DisplayName("language-only registration (register) updates language fallback key")
    void registerLanguageOnlyLocale() {
        Locale langOnly = Locale.of("en");
        String[] custom = {"Testland"};

        CountryDataRegistry.register(new CountryDataProvider() {
            @Override public Locale getLocale() { return langOnly; }
            @Override public String[] getCountries() { return custom; }
        });

        CountryDataProvider found = CountryDataRegistry.forLocale(langOnly);
        assertNotNull(found);
        assertArrayEquals(custom, found.getCountries());

        // Restore the language-level "en" fallback.
        CountryDataRegistry.register(new CountryDataProvider() {
            @Override public Locale getLocale() { return Locale.of("en"); }
            @Override public String[] getCountries() { return new BuiltInCountryDataProvider(SupportedLocale.EN_US).getCountries(); }
        });
    }

    @Test
    @DisplayName("isRegistered returns true for language-only locale when language is registered")
    void isRegisteredLanguageOnlyLocale() {
        Locale langOnly = Locale.of("de");
        assertTrue(CountryDataRegistry.isRegistered(langOnly));
    }

    @Test
    @DisplayName("forLocale returns provider for language-only locale")
    void forLocaleLanguageOnlyLocale() {
        CountryDataProvider provider = CountryDataRegistry.forLocale(Locale.of("de"));
        assertNotNull(provider);
        assertTrue(provider.getCountries().length > 0);
    }

    @Test
    @DisplayName("CountryResourceLoader loads valid resource file")
    void resourceLoaderLoadsValidFile() {
        String[] countries = CountryResourceLoader.load("krandom/countries/en_US_countries.txt");
        assertEquals(195, countries.length);
        assertTrue(Arrays.asList(countries).contains("United States"));
    }

    @Test
    @DisplayName("CountryResourceLoader filters comments and blank lines")
    void resourceLoaderFiltersCommentsAndBlanks() {
        String[] countries = CountryResourceLoader.load("krandom/countries/test_countries.txt");
        assertEquals(3, countries.length);
        assertEquals("Country One", countries[0]);
        assertEquals("Country Two", countries[1]);
        assertEquals("Country Three", countries[2]);
    }

    @Test
    @DisplayName("CountryResourceLoader throws for missing resource")
    void resourceLoaderThrowsForMissingFile() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> CountryResourceLoader.load("krandom/countries/nonexistent.txt")
        );
        assertTrue(ex.getMessage().contains("Country resource not found"));
    }
}
