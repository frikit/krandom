/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TitleGenerator")
class TitleGeneratorTest {

    @Test
    @DisplayName("default constructor uses US locale")
    void defaultConstructor() {
        TitleGenerator gen = new TitleGenerator();
        
        assertEquals(Locale.US, gen.getLocale());
        assertTrue(gen.getTitleCount() > 0);
    }

    @Test
    @DisplayName("generate() returns non-null, non-empty title")
    void generateNotEmpty() {
        TitleGenerator gen = new TitleGenerator();
        String title = gen.generate();
        
        assertNotNull(title);
        assertFalse(title.isEmpty());
    }

    @Test
    @DisplayName("US locale generates American-style titles")
    void usLocaleTitles() {
        TitleGenerator gen = new TitleGenerator(Locale.US);
        
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            titles.add(gen.generate());
        }
        
        // US titles have periods
        assertTrue(titles.contains("Mr.") || titles.contains("Mrs.") || titles.contains("Dr."));
        assertTrue(gen.getTitleCount() > 5);
    }

    @Test
    @DisplayName("UK locale generates British-style titles")
    void ukLocaleTitles() {
        TitleGenerator gen = new TitleGenerator(Locale.UK);
        
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            titles.add(gen.generate());
        }
        
        // UK titles may include nobility
        assertTrue(titles.size() > 5);
        // UK style without periods or with special titles
        assertTrue(titles.stream().anyMatch(t -> 
            t.equals("Mr") || t.equals("Sir") || t.equals("Lord") || t.equals("Dame")));
    }

    @Test
    @DisplayName("German locale generates German titles")
    void germanLocaleTitles() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .build();
        TitleGenerator gen = new TitleGenerator(config);
        
        assertEquals(Locale.GERMANY, gen.getLocale());
        
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            titles.add(gen.generate());
        }
        
        assertTrue(titles.contains("Herr") || titles.contains("Frau"));
        assertTrue(gen.getTitleCount() > 3);
    }

    @Test
    @DisplayName("Japanese locale generates Japanese honorifics")
    void japaneseLocaleTitles() {
        TitleGenerator gen = new TitleGenerator(Locale.JAPAN);
        
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            titles.add(gen.generate());
        }
        
        // Japanese honorifics
        assertTrue(titles.contains("さん") || titles.contains("様") || titles.contains("先生"));
        assertTrue(gen.getTitleCount() >= 5);
    }

    @Test
    @DisplayName("French locale generates French titles")
    void frenchLocaleTitles() {
        TitleGenerator gen = new TitleGenerator(Locale.FRANCE);
        
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            titles.add(gen.generate());
        }
        
        assertTrue(titles.contains("M.") || titles.contains("Mme") || titles.contains("Dr"));
    }

    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(12345L)
                .build();
        
        GeneratorConfig config2 = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(12345L)
                .build();
        
        TitleGenerator gen1 = new TitleGenerator(config1);
        TitleGenerator gen2 = new TitleGenerator(config2);
        
        List<String> list1 = gen1.generateList(20);
        List<String> list2 = gen2.generateList(20);
        
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .seed(111L)
                .build();
        
        GeneratorConfig config2 = GeneratorConfig.builder()
                .seed(222L)
                .build();
        
        TitleGenerator gen1 = new TitleGenerator(config1);
        TitleGenerator gen2 = new TitleGenerator(config2);
        
        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);
        
        assertNotEquals(list1, list2);
    }

    @Test
    @DisplayName("generateList() produces correct count")
    void generateListCount() {
        TitleGenerator gen = new TitleGenerator();
        
        List<String> titles = gen.generateList(15);
        
        assertEquals(15, titles.size());
        titles.forEach(title -> {
            assertNotNull(title);
            assertFalse(title.isEmpty());
        });
    }

    @Test
    @DisplayName("unsupported locale falls back gracefully")
    void unsupportedLocaleFallback() {
        Locale unsupported = new Locale("xx", "YY");
        TitleGenerator gen = new TitleGenerator(unsupported);
        
        // Should fallback to EN_US
        String title = gen.generate();
        assertNotNull(title);
        assertFalse(title.isEmpty());
        
        // Should not be explicitly supported
        assertFalse(gen.isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("language-only fallback works (en -> en_US)")
    void languageOnlyFallback() {
        Locale english = new Locale("en"); // No country
        TitleGenerator gen = new TitleGenerator(english);
        
        String title = gen.generate();
        assertNotNull(title);
        
        // Should generate valid English titles
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            titles.add(gen.generate());
        }
        assertTrue(titles.size() > 3);
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for supported locales")
    void localeSupported() {
        assertTrue(new TitleGenerator(Locale.US).isLocaleExplicitlySupported());
        assertTrue(new TitleGenerator(Locale.UK).isLocaleExplicitlySupported());
        assertTrue(new TitleGenerator(Locale.GERMANY).isLocaleExplicitlySupported());
        assertTrue(new TitleGenerator(Locale.JAPAN).isLocaleExplicitlySupported());
        assertTrue(new TitleGenerator(Locale.FRANCE).isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("stream() generates continuous values")
    void streamGeneration() {
        TitleGenerator gen = new TitleGenerator();
        
        List<String> titles = gen.stream()
                .limit(25)
                .toList();
        
        assertEquals(25, titles.size());
        titles.forEach(title -> {
            assertNotNull(title);
            assertFalse(title.isEmpty());
        });
    }

    @Test
    @DisplayName("multiple locales can be used simultaneously")
    void multipleLocales() {
        TitleGenerator usGen = new TitleGenerator(Locale.US);
        TitleGenerator ukGen = new TitleGenerator(Locale.UK);
        TitleGenerator deGen = new TitleGenerator(Locale.GERMANY);
        
        String usTitle = usGen.generate();
        String ukTitle = ukGen.generate();
        String deTitle = deGen.generate();
        
        assertNotNull(usTitle);
        assertNotNull(ukTitle);
        assertNotNull(deTitle);
        
        assertEquals(Locale.US, usGen.getLocale());
        assertEquals(Locale.UK, ukGen.getLocale());
        assertEquals(Locale.GERMANY, deGen.getLocale());
    }

    @Test
    @DisplayName("constructor rejects null config")
    void nullConfigRejected() {
        assertThrows(NullPointerException.class, () -> 
            new TitleGenerator((GeneratorConfig) null)
        );
    }

    @Test
    @DisplayName("constructor rejects null locale")
    void nullLocaleRejected() {
        assertThrows(NullPointerException.class, () -> 
            new TitleGenerator((Locale) null)
        );
    }

    @Test
    @DisplayName("getTitleCount() returns positive value for all locales")
    void titleCountPositive() {
        for (LocaleTitleData data : LocaleTitleData.values()) {
            TitleGenerator gen = new TitleGenerator(data.getLocale());
            assertTrue(gen.getTitleCount() > 0,
                    "Locale " + data.getLocale() + " should have titles");
        }
    }
}
