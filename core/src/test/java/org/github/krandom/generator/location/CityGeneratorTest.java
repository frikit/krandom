/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CityGenerator")
class CityGeneratorTest {

    @Test
    @DisplayName("default config uses US locale with US city names")
    void defaultConfig() {
        CityGenerator gen = new CityGenerator(GeneratorConfig.defaults());

        assertEquals(Locale.US, gen.getLocale());
        assertTrue(gen.getCityCount() > 0);
    }

    @Test
    @DisplayName("generate() returns non-null, non-empty city name")
    void generateNotEmpty() {
        CityGenerator gen = new CityGenerator(Locale.US);
        String city = gen.generate();

        assertNotNull(city);
        assertFalse(city.isEmpty());
    }

    @Test
    @DisplayName("US locale returns US city names")
    void usLocaleUSCities() {
        CityGenerator gen = new CityGenerator(Locale.US);

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            cities.add(gen.generate());
        }

        // Check for some major US cities
        long usCities = cities.stream()
                .filter(c -> c.equals("New York") || c.equals("Los Angeles") || 
                             c.equals("Chicago") || c.equals("Houston") || c.equals("Phoenix"))
                .count();
        assertTrue(usCities > 0, "Expected to find at least one major US city");
        assertTrue(gen.getCityCount() > 50);
    }

    @Test
    @DisplayName("UK locale returns UK city names")
    void ukLocale() {
        CityGenerator gen = new CityGenerator(Locale.UK);

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            cities.add(gen.generate());
        }

        // Check for some major UK cities
        long ukCities = cities.stream()
                .filter(c -> c.equals("London") || c.equals("Manchester") || 
                             c.equals("Birmingham") || c.equals("Glasgow") || c.equals("Edinburgh"))
                .count();
        assertTrue(ukCities > 0, "Expected to find at least one major UK city");
        assertTrue(gen.getCityCount() > 50);
    }

    @Test
    @DisplayName("Australian locale returns Australian city names")
    void australianLocale() {
        CityGenerator gen = new CityGenerator(Locale.of("en", "AU"));

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            cities.add(gen.generate());
        }

        // Check for some major Australian cities
        long auCities = cities.stream()
                .filter(c -> c.equals("Sydney") || c.equals("Melbourne") || 
                             c.equals("Brisbane") || c.equals("Perth") || c.equals("Adelaide"))
                .count();
        assertTrue(auCities > 0, "Expected to find at least one major Australian city");
        assertTrue(gen.getCityCount() > 50);
    }

    @Test
    @DisplayName("German locale returns German city names")
    void germanLocale() {
        CityGenerator gen = new CityGenerator(Locale.GERMANY);

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            cities.add(gen.generate());
        }

        // Check for German cities with proper German spelling
        long germanCities = cities.stream()
                .filter(c -> c.equals("Berlin") || c.equals("München") || 
                             c.equals("Hamburg") || c.equals("Köln") || c.equals("Frankfurt am Main"))
                .count();
        assertTrue(germanCities > 0, "Expected to find at least one major German city");
        assertTrue(gen.getCityCount() > 50);
    }

    @Test
    @DisplayName("Japanese locale returns Japanese city names")
    void japaneseLocale() {
        CityGenerator gen = new CityGenerator(Locale.JAPAN);

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            cities.add(gen.generate());
        }

        // Japanese script
        long japaneseCities = cities.stream()
                .filter(c -> c.contains("東京") || c.contains("大阪") || 
                             c.contains("京都") || c.contains("横浜") || c.contains("名古屋"))
                .count();
        assertTrue(japaneseCities > 0, "Expected to find at least one major Japanese city");
    }

    @Test
    @DisplayName("French locale returns French city names")
    void frenchLocale() {
        CityGenerator gen = new CityGenerator(Locale.FRANCE);

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            cities.add(gen.generate());
        }

        long frenchCities = cities.stream()
                .filter(c -> c.equals("Paris") || c.equals("Lyon") || 
                             c.equals("Marseille") || c.equals("Toulouse") || c.equals("Nice"))
                .count();
        assertTrue(frenchCities > 0, "Expected to find at least one major French city");
    }

    @Test
    @DisplayName("Chinese locale returns Chinese city names")
    void chineseLocale() {
        CityGenerator gen = new CityGenerator(Locale.of("zh", "CN"));

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            cities.add(gen.generate());
        }

        // Chinese characters
        long chineseCities = cities.stream()
                .filter(c -> c.contains("北京") || c.contains("上海") || 
                             c.contains("广州") || c.contains("深圳") || c.contains("成都"))
                .count();
        assertTrue(chineseCities > 0, "Expected to find at least one major Chinese city");
    }

    @Test
    @DisplayName("Spanish locale returns Spanish city names")
    void spanishLocale() {
        CityGenerator gen = new CityGenerator(Locale.of("es", "ES"));

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            cities.add(gen.generate());
        }

        long spanishCities = cities.stream()
                .filter(c -> c.equals("Madrid") || c.equals("Barcelona") || 
                             c.equals("Valencia") || c.equals("Sevilla") || c.equals("Zaragoza"))
                .count();
        assertTrue(spanishCities > 0, "Expected to find at least one major Spanish city");
    }

    @Test
    @DisplayName("Italian locale returns Italian city names")
    void italianLocale() {
        CityGenerator gen = new CityGenerator(Locale.of("it", "IT"));

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            cities.add(gen.generate());
        }

        long italianCities = cities.stream()
                .filter(c -> c.equals("Roma") || c.equals("Milano") || 
                             c.equals("Napoli") || c.equals("Torino") || c.equals("Firenze"))
                .count();
        assertTrue(italianCities > 0, "Expected to find at least one major Italian city");
    }

    @Test
    @DisplayName("Brazilian Portuguese locale returns Brazilian city names")
    void brazilianPortugueseLocale() {
        CityGenerator gen = new CityGenerator(Locale.of("pt", "BR"));

        Set<String> cities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            cities.add(gen.generate());
        }

        long brazilianCities = cities.stream()
                .filter(c -> c.equals("São Paulo") || c.equals("Rio de Janeiro") || 
                             c.equals("Brasília") || c.equals("Salvador") || c.equals("Fortaleza"))
                .count();
        assertTrue(brazilianCities > 0, "Expected to find at least one major Brazilian city");
    }

    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder().locale(Locale.GERMANY).seed(42L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().locale(Locale.GERMANY).seed(42L).build();

        CityGenerator gen1 = new CityGenerator(config1);
        CityGenerator gen2 = new CityGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder().seed(111L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().seed(222L).build();

        CityGenerator gen1 = new CityGenerator(config1);
        CityGenerator gen2 = new CityGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertNotEquals(list1, list2);
    }

    @Test
    @DisplayName("generateList() produces correct count")
    void generateListCount() {
        CityGenerator gen = new CityGenerator(Locale.US);
        List<String> cities = gen.generateList(20);

        assertEquals(20, cities.size());
        cities.forEach(c -> {
            assertNotNull(c);
            assertFalse(c.isEmpty());
        });
    }

    @Test
    @DisplayName("stream() generates continuous values")
    void streamGeneration() {
        CityGenerator gen = new CityGenerator(Locale.JAPAN);

        List<String> cities = gen.stream().limit(30).toList();

        assertEquals(30, cities.size());
        cities.forEach(c -> {
            assertNotNull(c);
            assertFalse(c.isEmpty());
        });
    }

    @Test
    @DisplayName("getLocale() returns the configured locale")
    void getLocale() {
        assertEquals(Locale.US, new CityGenerator(Locale.US).getLocale());
        assertEquals(Locale.GERMANY, new CityGenerator(Locale.GERMANY).getLocale());
        assertEquals(Locale.JAPAN, new CityGenerator(Locale.JAPAN).getLocale());
    }

    @Test
    @DisplayName("getCityCount() returns positive value for all built-in locales")
    void cityCountPositive() {
        for (LocaleCityData data : LocaleCityData.values()) {
            CityGenerator gen = new CityGenerator(data.getLocale());
            assertTrue(gen.getCityCount() > 0,
                    "Locale " + data.getLocale() + " should have cities");
        }
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for all built-in locales")
    void localeSupported() {
        assertTrue(new CityGenerator(Locale.US).isLocaleExplicitlySupported());
        assertTrue(new CityGenerator(Locale.UK).isLocaleExplicitlySupported());
        assertTrue(new CityGenerator(Locale.GERMANY).isLocaleExplicitlySupported());
        assertTrue(new CityGenerator(Locale.JAPAN).isLocaleExplicitlySupported());
        assertTrue(new CityGenerator(Locale.FRANCE).isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new CityGenerator(Locale.of("xx", "YY"))
        );

        assertTrue(ex.getMessage().contains("not supported"));
        assertTrue(ex.getMessage().contains("xx_YY"));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class,
            () -> new CityGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        assertThrows(NullPointerException.class,
            () -> new CityGenerator((Locale) null));
    }

    @Test
    @DisplayName("generate() produces variety — not always the same value")
    void generateVariety() {
        CityGenerator gen = new CityGenerator(Locale.US);

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(gen.generate());
        }

        assertTrue(seen.size() > 10, "Expected variety of cities, got: " + seen.size());
    }

    // ── CityDataRegistry extensibility ────────────────────────────────────

    @Test
    @DisplayName("custom provider registered for new locale is used by CityGenerator")
    void customLocaleRegistration() {
        Locale korean = Locale.of("ko", "KR");
        String[] koreanCities = {"서울", "부산", "인천", "대구"};

        CityDataRegistry.register(new CityDataProvider() {
            @Override public Locale getLocale() { return korean; }
            @Override public String[] getCities() { return koreanCities; }
        });

        CityGenerator gen = new CityGenerator(korean);
        assertEquals(4, gen.getCityCount());

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(gen.generate());
        assertTrue(seen.containsAll(Arrays.asList(koreanCities)));
    }

    @Test
    @DisplayName("custom provider overrides built-in locale")
    void customProviderOverridesBuiltIn() {
        Locale us = Locale.US;
        String[] custom = {"Foo City", "Bar Town"};

        CityDataRegistry.register(new CityDataProvider() {
            @Override public Locale getLocale() { return us; }
            @Override public String[] getCities() { return custom; }
        });

        CityGenerator gen = new CityGenerator(us);
        assertEquals(2, gen.getCityCount());

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) seen.add(gen.generate());
        assertTrue(seen.containsAll(Arrays.asList(custom)));

        // Restore built-in US data so other tests are unaffected.
        CityDataRegistry.register(LocaleCityData.EN_US);
    }

    @Test
    @DisplayName("registered custom locale appears in registeredKeys()")
    void customLocaleAppearsInKeys() {
        Locale swahili = Locale.of("sw", "TZ");
        CityDataRegistry.register(new CityDataProvider() {
            @Override public Locale getLocale() { return swahili; }
            @Override public String[] getCities() { return new String[]{"Dar es Salaam", "Dodoma"}; }
        });

        assertTrue(CityDataRegistry.registeredKeys().contains("sw_TZ"));
        assertTrue(CityDataRegistry.isRegistered(swahili));
    }

    @Test
    @DisplayName("register rejects null provider")
    void registerRejectsNull() {
        assertThrows(NullPointerException.class, () -> CityDataRegistry.register(null));
    }

    @Test
    @DisplayName("isRegistered returns false for unregistered locale")
    void isRegisteredUnknownLocale() {
        assertFalse(CityDataRegistry.isRegistered(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for completely unknown locale")
    void forLocaleUnknownReturnsNull() {
        assertNull(CityDataRegistry.forLocale(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for null locale")
    void forLocaleNullReturnsNull() {
        assertNull(CityDataRegistry.forLocale(null));
    }

    @Test
    @DisplayName("isRegistered returns false for null locale")
    void isRegisteredNullReturnsFalse() {
        assertFalse(CityDataRegistry.isRegistered(null));
    }

    @Test
    @DisplayName("CityResourceLoader loads valid resource file")
    void resourceLoaderLoadsValidFile() {
        String[] cities = CityResourceLoader.load("krandom/cities/en_US_cities.txt");
        assertTrue(cities.length > 0);
        assertTrue(Arrays.asList(cities).contains("New York"));
    }

    @Test
    @DisplayName("CityResourceLoader filters comments and blank lines")
    void resourceLoaderFiltersCommentsAndBlanks() {
        String[] cities = CityResourceLoader.load("krandom/cities/test_cities.txt");
        assertEquals(3, cities.length);
        assertEquals("City One", cities[0]);
        assertEquals("City Two", cities[1]);
        assertEquals("City Three", cities[2]);
    }

    @Test
    @DisplayName("CityResourceLoader throws for missing resource")
    void resourceLoaderThrowsForMissingFile() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> CityResourceLoader.load("krandom/cities/nonexistent.txt")
        );
        assertTrue(ex.getMessage().contains("City resource not found"));
    }
}
