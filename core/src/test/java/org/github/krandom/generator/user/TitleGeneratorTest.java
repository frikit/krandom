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
    @DisplayName("unsupported locale throws exception")
    void unsupportedLocaleThrowsException() {
        Locale unsupported = Locale.of("xx", "YY");
        
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> new TitleGenerator(unsupported)
        );
        
        assertTrue(exception.getMessage().contains("not supported"));
        assertTrue(exception.getMessage().contains("xx_YY"));
    }

    @Test
    @DisplayName("language-only locale works (en -> matches en_US)")
    void languageOnlyLocale() {
        Locale english = Locale.of("en");
        TitleGenerator gen = new TitleGenerator(english);
        
        String title = gen.generate();
        assertNotNull(title);
        
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
    @DisplayName("getTitleCount() returns positive value for all built-in locales")
    void titleCountPositive() {
        for (LocaleTitleData data : LocaleTitleData.values()) {
            TitleGenerator gen = new TitleGenerator(data.getLocale());
            assertTrue(gen.getTitleCount() > 0,
                    "Locale " + data.getLocale() + " should have titles");
        }
    }

    // --- TitleDataRegistry extensibility ---

    @Test
    @DisplayName("custom provider registered for new locale is used by TitleGenerator")
    void customLocaleRegistration() {
        Locale korean = Locale.of("ko", "KR");
        String[] koreanTitles = {"씨", "님", "박사", "교수"};

        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return korean; }
            @Override public String[] getTitles() { return koreanTitles; }
        });

        TitleGenerator gen = new TitleGenerator(korean);
        assertEquals(4, gen.getTitleCount());

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(gen.generate());
        assertTrue(seen.containsAll(Arrays.asList(koreanTitles)));
    }

    @Test
    @DisplayName("custom provider overrides built-in locale")
    void customProviderOverridesBuiltIn() {
        Locale us = Locale.US;
        String[] custom = {"Ser", "Dame"};

        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return us; }
            @Override public String[] getTitles() { return custom; }
        });

        TitleGenerator gen = new TitleGenerator(us);
        assertEquals(2, gen.getTitleCount());

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) seen.add(gen.generate());
        assertTrue(seen.containsAll(Arrays.asList(custom)));

        // Restore built-in US data so other tests are unaffected.
        TitleDataRegistry.register(LocaleTitleData.EN_US);
    }

    @Test
    @DisplayName("registered custom locale appears in registeredKeys()")
    void customLocaleAppearsInKeys() {
        Locale swahili = Locale.of("sw");
        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return swahili; }
            @Override public String[] getTitles() { return new String[]{"Bwana", "Bibi"}; }
        });

        assertTrue(TitleDataRegistry.registeredKeys().contains("sw"));
        assertTrue(TitleDataRegistry.isRegistered(swahili));
    }

    @Test
    @DisplayName("language-only registration serves as fallback for any country variant")
    void languageOnlyFallback() {
        Locale arabic = Locale.of("ar");
        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return arabic; }
            @Override public String[] getTitles() { return new String[]{"السيد", "السيدة"}; }
        });

        // ar_EG should fall back to the "ar" language entry.
        Locale arabicEgypt = Locale.of("ar", "EG");
        assertTrue(TitleDataRegistry.isRegistered(arabicEgypt));

        TitleGenerator gen = new TitleGenerator(arabicEgypt);
        assertEquals(2, gen.getTitleCount());
    }

    @Test
    @DisplayName("register rejects null provider")
    void registerRejectsNull() {
        assertThrows(NullPointerException.class, () -> TitleDataRegistry.register(null));
    }

    @Test
    @DisplayName("generate() returns empty string when provider supplies empty titles array")
    void generateWithEmptyTitlesArray() {
        Locale empty = Locale.of("zz", "ZZ");
        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return empty; }
            @Override public String[] getTitles() { return new String[0]; }
        });

        TitleGenerator gen = new TitleGenerator(empty);
        assertEquals("", gen.generate());
    }

    @Test
    @DisplayName("isRegistered returns false for null locale")
    void isRegisteredNullReturnsFalse() {
        assertFalse(TitleDataRegistry.isRegistered(null));
    }

    @Test
    @DisplayName("forLocale returns null for null locale")
    void forLocaleNullReturnsNull() {
        assertNull(TitleDataRegistry.forLocale(null));
    }

    @Test
    @DisplayName("isRegistered returns false for unregistered locale with country")
    void isRegisteredUnknownLocale() {
        assertFalse(TitleDataRegistry.isRegistered(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for completely unknown locale")
    void forLocaleUnknownReturnsNull() {
        assertNull(TitleDataRegistry.forLocale(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns language-level entry when no exact country match exists")
    void forLocaleLanguageFallbackInRegistry() {
        // "en" is registered (language-level entry seeded from EN_US).
        // "en_ZZ" has no exact entry but shares the "en" language key.
        TitleDataProvider provider = TitleDataRegistry.forLocale(Locale.of("en", "ZZ"));
        assertNotNull(provider);
        assertTrue(provider.getTitles().length > 0);
    }

    @Test
    @DisplayName("explicit language-only registration replaces language fallback")
    void languageOnlyRegistrationReplacesExistingFallback() {
        Locale plain = Locale.of("en");
        String[] slim = {"Doc"};

        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return plain; }
            @Override public String[] getTitles() { return slim; }
        });

        TitleDataProvider found = TitleDataRegistry.forLocale(plain);
        assertNotNull(found);
        assertArrayEquals(slim, found.getTitles());

        // Restore the language-level "en" fallback. Must use a locale with no country so
        // register() takes the explicit-put path rather than putIfAbsent.
        TitleDataRegistry.register(new TitleDataProvider() {
            @Override public Locale getLocale() { return Locale.of("en"); }
            @Override public String[] getTitles() { return LocaleTitleData.EN_US.getTitles(); }
        });
    }
}
