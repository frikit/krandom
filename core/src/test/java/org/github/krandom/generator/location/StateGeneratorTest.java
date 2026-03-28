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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StateGenerator")
class StateGeneratorTest {

    @Test
    @DisplayName("default config uses US locale with US state names")
    void defaultConfig() {
        StateGenerator gen = new StateGenerator(GeneratorConfig.defaults());

        assertEquals(Locale.US, gen.getLocale());
        assertTrue(gen.getStateCount() > 0);
    }

    @Test
    @DisplayName("generate() returns non-null, non-empty state name")
    void generateNotEmpty() {
        StateGenerator gen = new StateGenerator(Locale.US);
        String state = gen.generate();

        assertNotNull(state);
        assertFalse(state.isEmpty());
    }

    @Test
    @DisplayName("abbreviation mode falls back to full state when abbreviation missing")
    void abbreviationFallbackWhenMissing() {
        Locale locale = Locale.of("zz", "ZZ");
        StateDataRegistry.register(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[]{"Alpha", "Beta"};
            }

            @Override
            public String[] getAbbreviations() {
                return new String[]{"AA"};
            }
        });

        StateGenerator gen = new StateGenerator(GeneratorConfig.builder().locale(locale).seed(1L).build());
        assertDoesNotThrow(() -> {
            Field randomField = StateGenerator.class.getDeclaredField("random");
            randomField.setAccessible(true);
            randomField.set(gen, new Random(0L) {
                @Override
                public int nextInt(int bound) {
                    return 1;
                }
            });
        });
        assertEquals("Beta", gen.generate(true));
    }

    @Test
    @DisplayName("abbreviation mode falls back when locale has no abbreviation list")
    void abbreviationFallbackWhenNoAbbreviationList() {
        Locale locale = Locale.of("zy", "ZY");
        StateDataRegistry.register(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[]{"OnlyState"};
            }

            @Override
            public String[] getAbbreviations() {
                return new String[0];
            }
        });

        StateGenerator gen = new StateGenerator(GeneratorConfig.builder().locale(locale).seed(2L).build());
        assertEquals("OnlyState", gen.generate(true));
    }

    @Test
    @DisplayName("US locale returns US state names")
    void usLocaleUSStates() {
        StateGenerator gen = new StateGenerator(Locale.US);

        Set<String> states = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            states.add(gen.generate());
        }

        // Check for some major US states
        long usStates = states.stream()
                .filter(s -> s.equals("California") || s.equals("Texas") || 
                             s.equals("New York") || s.equals("Florida") || s.equals("Illinois"))
                .count();
        assertTrue(usStates > 0, "Expected to find at least one major US state");
        assertEquals(51, gen.getStateCount()); // 50 states + DC
    }

    @Test
    @DisplayName("US locale with abbreviation returns state abbreviations")
    void usLocaleAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.US);

        Set<String> abbreviations = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            abbreviations.add(gen.generate(true));
        }

        // Check for some major US state abbreviations
        long usAbbrevs = abbreviations.stream()
                .filter(s -> s.equals("CA") || s.equals("TX") || 
                             s.equals("NY") || s.equals("FL") || s.equals("IL"))
                .count();
        assertTrue(usAbbrevs > 0, "Expected to find at least one US state abbreviation");
        
        // All should be 2 characters (standard US state abbreviations)
        assertTrue(abbreviations.stream().allMatch(a -> a.length() <= 2),
                "US abbreviations should be 2 characters or less");
    }

    @Test
    @DisplayName("Australian locale returns Australian state names")
    void australianLocale() {
        StateGenerator gen = new StateGenerator(Locale.of("en", "AU"));

        Set<String> states = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            states.add(gen.generate());
        }

        // Check for some major Australian states
        long auStates = states.stream()
                .filter(s -> s.equals("New South Wales") || s.equals("Victoria") || 
                             s.equals("Queensland") || s.equals("South Australia") || 
                             s.equals("Western Australia"))
                .count();
        assertTrue(auStates > 0, "Expected to find at least one major Australian state");
        assertEquals(8, gen.getStateCount()); // 6 states + 2 territories
    }

    @Test
    @DisplayName("Australian locale with abbreviation returns state abbreviations")
    void australianLocaleAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.of("en", "AU"));

        Set<String> abbreviations = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            abbreviations.add(gen.generate(true));
        }

        // Check for some Australian state abbreviations
        long auAbbrevs = abbreviations.stream()
                .filter(a -> a.equals("NSW") || a.equals("VIC") || 
                             a.equals("QLD") || a.equals("SA") || a.equals("WA"))
                .count();
        assertTrue(auAbbrevs > 0, "Expected to find at least one Australian state abbreviation");
    }

    @Test
    @DisplayName("German locale returns German state names")
    void germanLocale() {
        StateGenerator gen = new StateGenerator(Locale.GERMANY);

        Set<String> states = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            states.add(gen.generate());
        }

        // Check for German states with proper German spelling
        long germanStates = states.stream()
                .filter(s -> s.equals("Bayern") || s.equals("Nordrhein-Westfalen") || 
                             s.equals("Baden-Württemberg") || s.equals("Hessen") || 
                             s.equals("Berlin"))
                .count();
        assertTrue(germanStates > 0, "Expected to find at least one major German state");
        assertEquals(16, gen.getStateCount());
    }

    @Test
    @DisplayName("German locale with abbreviation returns full names (no abbreviations)")
    void germanLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.GERMANY);

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            results.add(gen.generate(true));
        }

        // Even with abbreviation flag, should return full names since German states don't have standard abbreviations
        long germanStates = results.stream()
                .filter(s -> s.equals("Bayern") || s.equals("Berlin") || 
                             s.equals("Hessen"))
                .count();
        assertTrue(germanStates > 0, "Expected full state names even with abbreviation flag");
    }

    @Test
    @DisplayName("Italian locale returns Italian region names")
    void italianLocale() {
        StateGenerator gen = new StateGenerator(Locale.of("it", "IT"));

        Set<String> regions = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            regions.add(gen.generate());
        }

        // Check for some Italian regions
        long itRegions = regions.stream()
                .filter(r -> r.equals("Toscana") || r.equals("Lombardia") || 
                             r.equals("Lazio") || r.equals("Campania") || r.equals("Sicilia"))
                .count();
        assertTrue(itRegions > 0, "Expected to find at least one major Italian region");
        assertEquals(20, gen.getStateCount());
    }

    @Test
    @DisplayName("Italian locale with abbreviation returns full names (no abbreviations)")
    void italianLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.of("it", "IT"));

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            results.add(gen.generate(true));
        }

        // Should return full names since Italian regions don't have standard abbreviations in our data
        long itRegions = results.stream()
                .filter(r -> r.equals("Toscana") || r.equals("Lombardia") || r.equals("Lazio"))
                .count();
        assertTrue(itRegions > 0, "Expected full region names even with abbreviation flag");
    }

    @Test
    @DisplayName("UK locale returns UK country names")
    void ukLocale() {
        StateGenerator gen = new StateGenerator(Locale.UK);

        Set<String> countries = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            countries.add(gen.generate());
        }

        // Check for UK countries
        long ukCountries = countries.stream()
                .filter(c -> c.equals("England") || c.equals("Scotland") || 
                             c.equals("Wales") || c.equals("Northern Ireland"))
                .count();
        assertTrue(ukCountries > 0, "Expected to find at least one UK country");
        assertEquals(4, gen.getStateCount());
    }

    @Test
    @DisplayName("UK locale with abbreviation returns full names (no abbreviations)")
    void ukLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.UK);

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            results.add(gen.generate(true));
        }

        // Should return full names since UK countries don't have abbreviations
        long ukCountries = results.stream()
                .filter(c -> c.equals("England") || c.equals("Scotland"))
                .count();
        assertTrue(ukCountries > 0, "Expected full country names even with abbreviation flag");
    }

    @Test
    @DisplayName("French locale returns French region names")
    void frenchLocale() {
        StateGenerator gen = new StateGenerator(Locale.FRANCE);

        Set<String> regions = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            regions.add(gen.generate());
        }

        // Check for some French regions
        long frRegions = regions.stream()
                .filter(r -> r.equals("Île-de-France") || r.equals("Auvergne-Rhône-Alpes") || 
                             r.equals("Nouvelle-Aquitaine") || r.equals("Provence-Alpes-Côte d'Azur") || 
                             r.equals("Bretagne"))
                .count();
        assertTrue(frRegions > 0, "Expected to find at least one major French region");
        assertEquals(18, gen.getStateCount());
    }

    @Test
    @DisplayName("French locale with abbreviation returns full names (no abbreviations)")
    void frenchLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.FRANCE);

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            results.add(gen.generate(true));
        }

        // Should return full names since French regions don't have abbreviations
        long frRegions = results.stream()
                .filter(r -> r.equals("Île-de-France") || r.equals("Bretagne"))
                .count();
        assertTrue(frRegions > 0, "Expected full region names even with abbreviation flag");
    }

    @Test
    @DisplayName("Spanish (Spain) locale returns Spanish autonomous community names")
    void spanishSpainLocale() {
        StateGenerator gen = new StateGenerator(Locale.of("es", "ES"));

        Set<String> communities = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            communities.add(gen.generate());
        }

        // Check for some Spanish autonomous communities
        long esRegions = communities.stream()
                .filter(c -> c.equals("Andalucía") || c.equals("Cataluña") || 
                             c.equals("Comunidad de Madrid") || c.equals("País Vasco") || 
                             c.equals("Galicia"))
                .count();
        assertTrue(esRegions > 0, "Expected to find at least one Spanish autonomous community");
        assertEquals(19, gen.getStateCount());
    }

    @Test
    @DisplayName("Spanish (Spain) locale with abbreviation returns full names (no abbreviations)")
    void spanishSpainLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.of("es", "ES"));

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            results.add(gen.generate(true));
        }

        // Should return full names since Spanish autonomous communities don't have abbreviations
        long esRegions = results.stream()
                .filter(c -> c.equals("Andalucía") || c.equals("Cataluña"))
                .count();
        assertTrue(esRegions > 0, "Expected full community names even with abbreviation flag");
    }

    @Test
    @DisplayName("Brazilian locale returns Brazilian state names")
    void brazilianLocale() {
        StateGenerator gen = new StateGenerator(Locale.of("pt", "BR"));

        Set<String> states = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            states.add(gen.generate());
        }

        // Check for some major Brazilian states
        long brStates = states.stream()
                .filter(s -> s.equals("São Paulo") || s.equals("Rio de Janeiro") || 
                             s.equals("Minas Gerais") || s.equals("Bahia") || 
                             s.equals("Paraná"))
                .count();
        assertTrue(brStates > 0, "Expected to find at least one major Brazilian state");
        assertEquals(27, gen.getStateCount()); // 26 states + 1 federal district
    }

    @Test
    @DisplayName("Brazilian locale with abbreviation returns state abbreviations")
    void brazilianLocaleAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.of("pt", "BR"));

        Set<String> abbreviations = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            abbreviations.add(gen.generate(true));
        }

        // Check for some Brazilian state abbreviations
        long brAbbrevs = abbreviations.stream()
                .filter(a -> a.equals("SP") || a.equals("RJ") || 
                             a.equals("MG") || a.equals("BA") || a.equals("PR"))
                .count();
        assertTrue(brAbbrevs > 0, "Expected to find at least one Brazilian state abbreviation");
    }

    @Test
    @DisplayName("Japanese locale returns Japanese prefecture names")
    void japaneseLocale() {
        StateGenerator gen = new StateGenerator(Locale.JAPAN);

        Set<String> prefectures = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            prefectures.add(gen.generate());
        }

        // Check for some major Japanese prefectures
        long jpPrefectures = prefectures.stream()
                .filter(p -> p.equals("東京都") || p.equals("大阪府") || 
                             p.equals("京都府") || p.equals("北海道") || 
                             p.equals("神奈川県"))
                .count();
        assertTrue(jpPrefectures > 0, "Expected to find at least one major Japanese prefecture");
        assertEquals(47, gen.getStateCount());
    }

    @Test
    @DisplayName("Japanese locale with abbreviation returns full names (no abbreviations)")
    void japaneseLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.JAPAN);

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            results.add(gen.generate(true));
        }

        // Should return full names since Japanese prefectures don't have abbreviations
        long jpPrefectures = results.stream()
                .filter(p -> p.equals("東京都") || p.equals("大阪府"))
                .count();
        assertTrue(jpPrefectures > 0, "Expected full prefecture names even with abbreviation flag");
    }

    @Test
    @DisplayName("Chinese locale returns Chinese province names")
    void chineseLocale() {
        StateGenerator gen = new StateGenerator(Locale.CHINA);

        Set<String> provinces = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            provinces.add(gen.generate());
        }

        // Check for some major Chinese provinces
        long cnProvinces = provinces.stream()
                .filter(p -> p.equals("北京市") || p.equals("上海市") || 
                             p.equals("广东省") || p.equals("四川省") || 
                             p.equals("江苏省"))
                .count();
        assertTrue(cnProvinces > 0, "Expected to find at least one major Chinese province");
        assertEquals(34, gen.getStateCount());
    }

    @Test
    @DisplayName("Chinese locale with abbreviation returns full names (no abbreviations)")
    void chineseLocaleNoAbbreviations() {
        StateGenerator gen = new StateGenerator(Locale.CHINA);

        Set<String> results = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            results.add(gen.generate(true));
        }

        // Should return full names since Chinese provinces don't have abbreviations
        long cnProvinces = results.stream()
                .filter(p -> p.equals("北京市") || p.equals("上海市"))
                .count();
        assertTrue(cnProvinces > 0, "Expected full province names even with abbreviation flag");
    }

    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();

        StateGenerator gen1 = new StateGenerator(config1);
        StateGenerator gen2 = new StateGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("seeded generator with abbreviations produces reproducible results")
    void seededReproducibilityWithAbbreviations() {
        GeneratorConfig config1 = GeneratorConfig.builder().locale(Locale.US).seed(123L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().locale(Locale.US).seed(123L).build();

        StateGenerator gen1 = new StateGenerator(config1);
        StateGenerator gen2 = new StateGenerator(config2);

        List<String> list1 = new java.util.ArrayList<>();
        List<String> list2 = new java.util.ArrayList<>();
        
        for (int i = 0; i < 50; i++) {
            list1.add(gen1.generate(true));
            list2.add(gen2.generate(true));
        }

        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder().locale(Locale.US).seed(111L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().locale(Locale.US).seed(222L).build();

        StateGenerator gen1 = new StateGenerator(config1);
        StateGenerator gen2 = new StateGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertNotEquals(list1, list2);
    }

    @Test
    @DisplayName("generateList() produces correct count")
    void generateListCount() {
        StateGenerator gen = new StateGenerator(Locale.US);
        List<String> states = gen.generateList(20);

        assertEquals(20, states.size());
        states.forEach(s -> {
            assertNotNull(s);
            assertFalse(s.isEmpty());
        });
    }

    @Test
    @DisplayName("stream() generates continuous values")
    void streamGeneration() {
        StateGenerator gen = new StateGenerator(Locale.US);

        List<String> states = gen.stream().limit(30).toList();

        assertEquals(30, states.size());
        states.forEach(s -> {
            assertNotNull(s);
            assertFalse(s.isEmpty());
        });
    }

    @Test
    @DisplayName("getLocale() returns the configured locale")
    void getLocale() {
        assertEquals(Locale.US, new StateGenerator(Locale.US).getLocale());
        assertEquals(Locale.GERMANY, new StateGenerator(Locale.GERMANY).getLocale());
        assertEquals(Locale.UK, new StateGenerator(Locale.UK).getLocale());
    }

    @Test
    @DisplayName("getStateCount() returns positive value for all built-in locales")
    void stateCountPositive() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            StateGenerator gen = new StateGenerator(supportedLocale.locale());
            assertTrue(gen.getStateCount() > 0,
                    "Locale " + supportedLocale.locale() + " should have states");
        }
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for all built-in locales")
    void localeSupported() {
        assertTrue(new StateGenerator(Locale.US).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.UK).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.of("en", "AU")).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.GERMANY).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.FRANCE).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.of("es", "ES")).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.of("it", "IT")).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.of("pt", "BR")).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.JAPAN).isLocaleExplicitlySupported());
        assertTrue(new StateGenerator(Locale.CHINA).isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new StateGenerator(Locale.of("xx", "YY"))
        );

        assertTrue(ex.getMessage().contains("not supported"));
        assertTrue(ex.getMessage().contains("xx_YY"));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class,
            () -> new StateGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        assertThrows(NullPointerException.class,
            () -> new StateGenerator((Locale) null));
    }

    @Test
    @DisplayName("generate() produces variety — not always the same value")
    void generateVariety() {
        StateGenerator gen = new StateGenerator(Locale.US);

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(gen.generate());
        }

        assertTrue(seen.size() > 10, "Expected variety of states, got: " + seen.size());
    }

    @Test
    @DisplayName("generate(false) returns full names")
    void generateFullNames() {
        StateGenerator gen = new StateGenerator(Locale.US);

        Set<String> fullNames = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String state = gen.generate(false);
            fullNames.add(state);
            // Full state names should be longer than 2 characters
            assertTrue(state.length() > 2, "Expected full state name, got: " + state);
        }

        assertTrue(fullNames.size() > 10, "Expected variety of full state names");
    }

    // ── StateDataRegistry extensibility ────────────────────────────────────

    @Test
    @DisplayName("custom provider registered for new locale is used by StateGenerator")
    void customLocaleRegistration() {
        Locale indian = Locale.of("en", "IN");
        String[] indianStates = {"Maharashtra", "Karnataka", "Tamil Nadu", "Gujarat"};
        String[] indianAbbrevs = {"MH", "KA", "TN", "GJ"};

        StateDataRegistry.register(new StateDataProvider() {
            @Override public Locale getLocale() { return indian; }
            @Override public String[] getStates() { return indianStates; }
            @Override public String[] getAbbreviations() { return indianAbbrevs; }
        });

        StateGenerator gen = new StateGenerator(indian);
        assertEquals(4, gen.getStateCount());

        Set<String> seenStates = new HashSet<>();
        Set<String> seenAbbrevs = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seenStates.add(gen.generate(false));
            seenAbbrevs.add(gen.generate(true));
        }
        
        assertTrue(seenStates.containsAll(Arrays.asList(indianStates)));
        assertTrue(seenAbbrevs.containsAll(Arrays.asList(indianAbbrevs)));
    }

    @Test
    @DisplayName("custom provider overrides built-in locale")
    void customProviderOverridesBuiltIn() {
        Locale us = Locale.US;
        String[] custom = {"Foo State", "Bar State"};
        String[] customAbbrevs = {"FS", "BS"};

        StateDataRegistry.register(new StateDataProvider() {
            @Override public Locale getLocale() { return us; }
            @Override public String[] getStates() { return custom; }
            @Override public String[] getAbbreviations() { return customAbbrevs; }
        });

        StateGenerator gen = new StateGenerator(us);
        assertEquals(2, gen.getStateCount());

        Set<String> seenStates = new HashSet<>();
        Set<String> seenAbbrevs = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seenStates.add(gen.generate(false));
            seenAbbrevs.add(gen.generate(true));
        }
        
        assertTrue(seenStates.containsAll(Arrays.asList(custom)));
        assertTrue(seenAbbrevs.containsAll(Arrays.asList(customAbbrevs)));

        // Restore built-in US data so other tests are unaffected.
        StateDataRegistry.register(new BuiltInStateDataProvider(SupportedLocale.EN_US));
    }

    @Test
    @DisplayName("registered custom locale appears in registeredKeys()")
    void customLocaleAppearsInKeys() {
        Locale brazilian = Locale.of("pt", "BR");
        StateDataRegistry.register(new StateDataProvider() {
            @Override public Locale getLocale() { return brazilian; }
            @Override public String[] getStates() { return new String[]{"São Paulo", "Rio de Janeiro"}; }
            @Override public String[] getAbbreviations() { return new String[]{"SP", "RJ"}; }
        });

        assertTrue(StateDataRegistry.registeredKeys().contains("pt_BR"));
        assertTrue(StateDataRegistry.isRegistered(brazilian));
    }

    @Test
    @DisplayName("register rejects null provider")
    void registerRejectsNull() {
        assertThrows(NullPointerException.class, () -> StateDataRegistry.register(null));
    }

    @Test
    @DisplayName("isRegistered returns false for unregistered locale")
    void isRegisteredUnknownLocale() {
        assertFalse(StateDataRegistry.isRegistered(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for completely unknown locale")
    void forLocaleUnknownReturnsNull() {
        assertNull(StateDataRegistry.forLocale(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for null locale")
    void forLocaleNullReturnsNull() {
        assertNull(StateDataRegistry.forLocale(null));
    }

    @Test
    @DisplayName("isRegistered returns false for null locale")
    void isRegisteredNullReturnsFalse() {
        assertFalse(StateDataRegistry.isRegistered(null));
    }

    @Test
    @DisplayName("StateResourceLoader loads valid resource file")
    void resourceLoaderLoadsValidFile() {
        StateResourceLoader.StateData data = 
                StateResourceLoader.load("krandom/states/en_US_states.txt");
        assertTrue(data.states.length > 0);
        assertTrue(data.abbreviations.length > 0);
        assertTrue(Arrays.asList(data.states).contains("California"));
        assertTrue(Arrays.asList(data.abbreviations).contains("CA"));
    }

    @Test
    @DisplayName("StateResourceLoader filters comments and blank lines")
    void resourceLoaderFiltersCommentsAndBlanks() {
        StateResourceLoader.StateData data = 
                StateResourceLoader.load("krandom/states/test_states.txt");
        assertEquals(3, data.states.length);
        assertEquals(3, data.abbreviations.length);
        assertEquals("State One", data.states[0]);
        assertEquals("S1", data.abbreviations[0]);
        assertEquals("State Two", data.states[1]);
        assertEquals("S2", data.abbreviations[1]);
        assertEquals("State Three", data.states[2]);
        assertEquals("S3", data.abbreviations[2]);
    }

    @Test
    @DisplayName("StateResourceLoader throws for missing resource")
    void resourceLoaderThrowsForMissingFile() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> StateResourceLoader.load("krandom/states/nonexistent.txt")
        );
        assertTrue(ex.getMessage().contains("State resource not found"));
    }

    @Test
    @DisplayName("StateDataProvider returns cloned arrays")
    void providerReturnsClonedArrays() {
        StateDataProvider usData = new BuiltInStateDataProvider(SupportedLocale.EN_US);
        
        String[] states1 = usData.getStates();
        String[] states2 = usData.getStates();
        
        assertNotSame(states1, states2, "getStates() should return a clone");
        assertArrayEquals(states1, states2, "getStates() clones should have same content");
        
        String[] abbrevs1 = usData.getAbbreviations();
        String[] abbrevs2 = usData.getAbbreviations();
        
        assertNotSame(abbrevs1, abbrevs2, "getAbbreviations() should return a clone");
        assertArrayEquals(abbrevs1, abbrevs2, "getAbbreviations() clones should have same content");
    }

    @Test
    @DisplayName("StateDataRegistry utility class constructor throws")
    void registryUtilityClassConstructor() throws Exception {
        var constructor = StateDataRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, 
            constructor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("StateResourceLoader utility class constructor throws")
    void resourceLoaderUtilityClassConstructor() throws Exception {
        var constructor = StateResourceLoader.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
            constructor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("abbreviation mode falls back when abbreviation entry is blank")
    void abbreviationFallbackWhenBlankEntry() {
        Locale locale = Locale.of("zz", "BL");
        StateDataRegistry.register(new StateDataProvider() {
            @Override public Locale getLocale() { return locale; }
            @Override public String[] getStates() { return new String[]{"Alpha"}; }
            @Override public String[] getAbbreviations() { return new String[]{""}; }
        });

        StateGenerator gen = new StateGenerator(
                GeneratorConfig.builder().locale(locale).seed(1L).build()
        );
        assertEquals("Alpha", gen.generate(true));
    }

    @Test
    @DisplayName("abbreviation mode falls back when index exceeds abbreviation array length")
    void abbreviationFallbackWhenIndexOutOfBounds() {
        Locale locale = Locale.of("zz", "SH");
        StateDataRegistry.register(new StateDataProvider() {
            @Override public Locale getLocale() { return locale; }
            @Override public String[] getStates() { return new String[]{"First", "Second"}; }
            @Override public String[] getAbbreviations() { return new String[]{"F"}; }
        });

        StateGenerator gen = new StateGenerator(
                GeneratorConfig.builder().locale(locale).seed(2L).build()
        );
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            seen.add(gen.generate(true));
        }
        assertTrue(seen.contains("Second"), "Expected fallback to full state for missing abbreviation index");
    }
}
