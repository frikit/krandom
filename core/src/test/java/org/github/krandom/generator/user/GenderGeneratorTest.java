/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenderGenerator")
class GenderGeneratorTest {

    // ── Default constructor ────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses US locale")
    void defaultConstructor() {
        GenderGenerator gen = new GenderGenerator();
        assertEquals(Locale.US, gen.getLocale());
        assertEquals("Male", gen.getMaleLabel());
        assertEquals("Female", gen.getFemaleLabel());
    }

    // ── generate() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("generate() returns non-null, non-empty label")
    void generateNotEmpty() {
        GenderGenerator gen = new GenderGenerator();
        String label = gen.generate();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("generate() returns only Male or Female for US locale")
    void generateOnlyValidLabels() {
        GenderGenerator gen = new GenderGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(gen.generate());
        }
        assertEquals(Set.of("Male", "Female"), seen);
    }

    @Test
    @DisplayName("generate() covers both male and female branches")
    void generateCoversBothBranches() {
        GenderGenerator gen = new GenderGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(gen.generate());
        }
        assertTrue(seen.contains("Male"), "Male label should appear");
        assertTrue(seen.contains("Female"), "Female label should appear");
    }

    // ── generate(Gender) ──────────────────────────────────────────────────────

    @Test
    @DisplayName("generate(MALE) always returns male label")
    void generateMale() {
        GenderGenerator gen = new GenderGenerator();
        for (int i = 0; i < 50; i++) {
            assertEquals("Male", gen.generate(Gender.MALE));
        }
    }

    @Test
    @DisplayName("generate(FEMALE) always returns female label")
    void generateFemale() {
        GenderGenerator gen = new GenderGenerator();
        for (int i = 0; i < 50; i++) {
            assertEquals("Female", gen.generate(Gender.FEMALE));
        }
    }

    @Test
    @DisplayName("generate(null) throws NullPointerException")
    void generateNullThrows() {
        GenderGenerator gen = new GenderGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate(null));
    }

    // ── Locale support ────────────────────────────────────────────────────────

    @Test
    @DisplayName("French locale returns French labels")
    void frenchLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.FRANCE);
        assertEquals("Homme", gen.getMaleLabel());
        assertEquals("Femme", gen.getFemaleLabel());
        assertEquals("Homme", gen.generate(Gender.MALE));
        assertEquals("Femme", gen.generate(Gender.FEMALE));
    }

    @Test
    @DisplayName("German locale returns German labels")
    void germanLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.GERMANY);
        assertEquals("Männlich", gen.getMaleLabel());
        assertEquals("Weiblich", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("Japanese locale returns Japanese labels")
    void japaneseLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.JAPAN);
        assertEquals("男性", gen.getMaleLabel());
        assertEquals("女性", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("Spanish locale returns Spanish labels")
    void spanishLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.of("es", "ES"));
        assertEquals("Hombre", gen.getMaleLabel());
        assertEquals("Mujer", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("Italian locale returns Italian labels")
    void italianLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.of("it", "IT"));
        assertEquals("Maschio", gen.getMaleLabel());
        assertEquals("Femmina", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("Portuguese (Brazil) locale returns Portuguese labels")
    void portugueseLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.of("pt", "BR"));
        assertEquals("Masculino", gen.getMaleLabel());
        assertEquals("Feminino", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("Chinese locale returns Chinese labels")
    void chineseLocale() {
        GenderGenerator gen = new GenderGenerator(Locale.of("zh", "CN"));
        assertEquals("男", gen.getMaleLabel());
        assertEquals("女", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("language-only locale falls back to language-level entry")
    void languageOnlyLocaleFallback() {
        GenderGenerator gen = new GenderGenerator(Locale.of("en"));
        assertEquals("Male", gen.getMaleLabel());
        assertEquals("Female", gen.getFemaleLabel());
    }

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                                                        () -> new GenderGenerator(Locale.of("xx", "YY")));
        assertTrue(ex.getMessage().contains("not supported"));
    }

    // ── GeneratorConfig constructor ───────────────────────────────────────────

    @Test
    @DisplayName("GeneratorConfig constructor with locale and seed works")
    void configConstructor() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .seed(42L)
                                                .build();
        GenderGenerator gen = new GenderGenerator(config);
        assertEquals(Locale.US, gen.getLocale());
        assertTrue(gen.isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class,
                     () -> new GenderGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        assertThrows(NullPointerException.class,
                     () -> new GenderGenerator((Locale) null));
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("seeded generators produce reproducible output")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder().seed(9999L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().seed(9999L).build();
        List<String> list1 = new GenderGenerator(config1).generateList(50);
        List<String> list2 = new GenderGenerator(config2).generateList(50);
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder().seed(1L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().seed(2L).build();
        assertNotEquals(
            new GenderGenerator(config1).generateList(50),
            new GenderGenerator(config2).generateList(50));
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns correct count")
    void generateListCount() {
        GenderGenerator gen = new GenderGenerator();
        List<String> labels = gen.generateList(20);
        assertEquals(20, labels.size());
        labels.forEach(l -> assertTrue(l.equals("Male") || l.equals("Female")));
    }

    @Test
    @DisplayName("stream generates continuous values")
    void streamGeneration() {
        GenderGenerator gen = new GenderGenerator(Locale.FRANCE);
        List<String> labels = gen.stream().limit(25).toList();
        assertEquals(25, labels.size());
        labels.forEach(l -> assertTrue(l.equals("Homme") || l.equals("Femme")));
    }

    // ── All built-in locales ──────────────────────────────────────────────────

    @Test
    @DisplayName("all built-in locales produce non-empty labels")
    void allBuiltInLocales() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            GenderGenerator gen = new GenderGenerator(supportedLocale.locale());
            assertFalse(gen.getMaleLabel().isEmpty(),
                        "Male label should not be empty for " + supportedLocale.locale());
            assertFalse(gen.getFemaleLabel().isEmpty(),
                        "Female label should not be empty for " + supportedLocale.locale());
        }
    }

    // ── GenderDataRegistry extensibility ─────────────────────────────────────

    @Test
    @DisplayName("custom provider registered for new locale is used by GenderGenerator")
    void customLocaleRegistration() {
        Locale korean = Locale.of("ko", "KR");
        GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return korean;
            }

            @Override
            public String getMaleLabel() {
                return "남성";
            }

            @Override
            public String getFemaleLabel() {
                return "여성";
            }
        });

        GenderGenerator gen = new GenderGenerator(korean);
        assertEquals("남성", gen.generate(Gender.MALE));
        assertEquals("여성", gen.generate(Gender.FEMALE));
    }

    @Test
    @DisplayName("custom provider overrides built-in locale")
    void customProviderOverridesBuiltIn() {
        Locale us = Locale.US;
        GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return us;
            }

            @Override
            public String getMaleLabel() {
                return "M";
            }

            @Override
            public String getFemaleLabel() {
                return "F";
            }
        });

        GenderGenerator gen = new GenderGenerator(us);
        assertEquals("M", gen.generate(Gender.MALE));
        assertEquals("F", gen.generate(Gender.FEMALE));

        // Restore built-in data so other tests are unaffected
        GenderDataRegistry.register(new BuiltInGenderDataProvider(SupportedLocale.EN_US));
    }

    @Test
    @DisplayName("language-only registration serves as fallback for any country variant")
    void languageOnlyFallback() {
        Locale arabic = Locale.of("ar");
        GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return arabic;
            }

            @Override
            public String getMaleLabel() {
                return "ذكر";
            }

            @Override
            public String getFemaleLabel() {
                return "أنثى";
            }
        });

        Locale arabicEgypt = Locale.of("ar", "EG");
        assertTrue(GenderDataRegistry.isRegistered(arabicEgypt));
        GenderGenerator gen = new GenderGenerator(arabicEgypt);
        assertEquals("ذكر", gen.getMaleLabel());
    }

    @Test
    @DisplayName("register rejects null provider")
    void registerRejectsNull() {
        assertThrows(NullPointerException.class, () -> GenderDataRegistry.register(null));
    }

    @Test
    @DisplayName("isRegistered returns false for null locale")
    void isRegisteredNullReturnsFalse() {
        assertFalse(GenderDataRegistry.isRegistered(null));
    }

    @Test
    @DisplayName("forLocale returns null for null locale")
    void forLocaleNullReturnsNull() {
        assertNull(GenderDataRegistry.forLocale(null));
    }

    @Test
    @DisplayName("isRegistered returns false for unregistered locale")
    void isRegisteredUnknownLocale() {
        assertFalse(GenderDataRegistry.isRegistered(Locale.of("xx", "YY")));
    }

    @Test
    @DisplayName("forLocale returns null for completely unknown locale")
    void forLocaleUnknownReturnsNull() {
        assertNull(GenderDataRegistry.forLocale(Locale.of("xx", "ZZ")));
    }

    @Test
    @DisplayName("forLocale returns language-level entry when no exact country match")
    void forLocaleLanguageFallback() {
        GenderDataProvider provider = GenderDataRegistry.forLocale(Locale.of("en", "ZZ"));
        assertNotNull(provider);
        assertEquals("Male", provider.getMaleLabel());
    }

    @Test
    @DisplayName("isRegistered returns true for language-only locale")
    void isRegisteredLanguageOnly() {
        assertTrue(GenderDataRegistry.isRegistered(Locale.of("fr")));
    }

    @Test
    @DisplayName("forLocale returns provider for language-only locale")
    void forLocaleLanguageOnly() {
        GenderDataProvider provider = GenderDataRegistry.forLocale(Locale.of("de"));
        assertNotNull(provider);
        assertEquals("Männlich", provider.getMaleLabel());
    }

    @Test
    @DisplayName("registeredKeys contains all built-in locale keys")
    void registeredKeysContainsBuiltIn() {
        Set<String> keys = GenderDataRegistry.registeredKeys();
        assertTrue(keys.contains("en_US"));
        assertTrue(keys.contains("fr_FR"));
        assertTrue(keys.contains("de_DE"));
        assertTrue(keys.contains("ja_JP"));
        assertTrue(keys.contains("zh_CN"));
    }

    @Test
    @DisplayName("explicit language-only registration replaces language fallback")
    void languageOnlyRegistrationReplacesFallback() {
        Locale plain = Locale.of("en");
        GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return plain;
            }

            @Override
            public String getMaleLabel() {
                return "M2";
            }

            @Override
            public String getFemaleLabel() {
                return "F2";
            }
        });

        GenderDataProvider found = GenderDataRegistry.forLocale(plain);
        assertNotNull(found);
        assertEquals("M2", found.getMaleLabel());

        // Restore
        GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.of("en");
            }

            @Override
            public String getMaleLabel() {
                return new BuiltInGenderDataProvider(SupportedLocale.EN_US).getMaleLabel();
            }

            @Override
            public String getFemaleLabel() {
                return new BuiltInGenderDataProvider(SupportedLocale.EN_US).getFemaleLabel();
            }
        });
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported returns true for all built-in locales")
    void localeSupported() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            assertTrue(new GenderGenerator(supportedLocale.locale()).isLocaleExplicitlySupported(),
                       "Locale " + supportedLocale.locale() + " should be supported");
        }
    }

    @Test
    @DisplayName("multiple locales can be used simultaneously")
    void multipleLocales() {
        GenderGenerator us = new GenderGenerator(Locale.US);
        GenderGenerator fr = new GenderGenerator(Locale.FRANCE);
        GenderGenerator de = new GenderGenerator(Locale.GERMANY);

        assertNotNull(us.generate(Gender.MALE));
        assertNotNull(fr.generate(Gender.FEMALE));
        assertNotNull(de.generate(Gender.MALE));

        // Labels are locale-specific
        assertNotEquals(us.getMaleLabel(), de.getMaleLabel());
        assertNotEquals(fr.getMaleLabel(), de.getMaleLabel());
    }

    @Test
    @DisplayName("custom locale appears in registeredKeys")
    void customLocaleAppearsInKeys() {
        Locale swahili = Locale.of("sw");
        GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return swahili;
            }

            @Override
            public String getMaleLabel() {
                return "Mwanaume";
            }

            @Override
            public String getFemaleLabel() {
                return "Mwanamke";
            }
        });
        assertTrue(GenderDataRegistry.registeredKeys().contains("sw"));
        assertTrue(GenderDataRegistry.isRegistered(swahili));
    }
}
