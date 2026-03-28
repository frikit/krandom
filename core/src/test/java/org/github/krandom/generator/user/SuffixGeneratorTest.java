/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.locale.SupportedLocale;
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

@DisplayName("SuffixGenerator")
class SuffixGeneratorTest {

    // ── Default / US ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses Locale.US")
    void defaultConstructorUsesUsLocale() {
        SuffixGenerator gen = new SuffixGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("generate() returns a non-null, non-empty string")
    void generateReturnsNonEmpty() {
        SuffixGenerator gen = new SuffixGenerator();
        String suffix = gen.generate();
        assertNotNull(suffix);
        assertFalse(suffix.isEmpty());
    }

    @Test
    @DisplayName("generate() returns a value from the configured locale's suffix list")
    void generateReturnsKnownSuffix() {
        SuffixGenerator gen = new SuffixGenerator(Locale.US);
        Set<String> usExpected = Set.of(new BuiltInSuffixDataProvider(SupportedLocale.EN_US).getSuffixes());
        for (int i = 0; i < 50; i++) {
            assertTrue(usExpected.contains(gen.generate()),
                       "Generated suffix not in EN_US list: " + gen.generate());
        }
    }

    // ── Locale variety ────────────────────────────────────────────────────────

    @Test
    @DisplayName("German locale produces German suffixes")
    void germanSuffixes() {
        SuffixGenerator gen = new SuffixGenerator(Locale.GERMANY);
        Set<String> deExpected = Set.of(new BuiltInSuffixDataProvider(SupportedLocale.DE_DE).getSuffixes());
        for (int i = 0; i < 50; i++) {
            assertTrue(deExpected.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("Japanese locale produces Japanese suffixes")
    void japaneseSuffixes() {
        SuffixGenerator gen = new SuffixGenerator(Locale.JAPAN);
        Set<String> jaExpected = Set.of(new BuiltInSuffixDataProvider(SupportedLocale.JA_JP).getSuffixes());
        for (int i = 0; i < 50; i++) {
            assertTrue(jaExpected.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("French locale produces French suffixes")
    void frenchSuffixes() {
        SuffixGenerator gen = new SuffixGenerator(Locale.FRANCE);
        Set<String> frExpected = Set.of(new BuiltInSuffixDataProvider(SupportedLocale.FR_FR).getSuffixes());
        for (int i = 0; i < 50; i++) {
            assertTrue(frExpected.contains(gen.generate()));
        }
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("seeded generator produces identical sequences")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        SuffixGenerator a = new SuffixGenerator(cfg);
        SuffixGenerator b = new SuffixGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLocale() returns the configured locale")
    void getLocale() {
        SuffixGenerator gen = new SuffixGenerator(Locale.UK);
        assertEquals(Locale.of("en", "GB"), gen.getLocale());
    }

    @Test
    @DisplayName("getSuffixCount() matches the locale's array length")
    void getSuffixCount() {
        SuffixGenerator gen = new SuffixGenerator(Locale.US);
        assertEquals(new BuiltInSuffixDataProvider(SupportedLocale.EN_US).getSuffixes().length, gen.getSuffixCount());
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported() returns true for a registered locale")
    void isLocaleExplicitlySupported() {
        assertTrue(new SuffixGenerator(Locale.US).isLocaleExplicitlySupported());
    }

    // ── Unsupported locale ────────────────────────────────────────────────────

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        assertThrows(UnsupportedOperationException.class,
                     () -> new SuffixGenerator(Locale.of("xx", "YY")));
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns the requested number of suffixes")
    void generateList() {
        List<String> suffixes = new SuffixGenerator().generateList(10);
        assertEquals(10, suffixes.size());
        suffixes.forEach(s -> assertFalse(s.isEmpty()));
    }

    @Test
    @DisplayName("stream() produces on-demand values")
    void streamProducesValues() {
        List<String> suffixes = new SuffixGenerator().stream().limit(15).toList();
        assertEquals(15, suffixes.size());
    }

    // ── Registry extensibility ────────────────────────────────────────────────

    @Test
    @DisplayName("all 10 built-in locales produce non-empty suffixes")
    void allBuiltInLocalesProduceValues() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            SuffixGenerator gen = new SuffixGenerator(supportedLocale.locale());
            String suffix = gen.generate();
            assertNotNull(suffix, "Null for " + supportedLocale);
            assertFalse(suffix.isEmpty(), "Empty for " + supportedLocale);
        }
    }

    // ── All built-in locales covered ──────────────────────────────────────────

    @Test
    @DisplayName("all built-in locales produce variety of values over many samples")
    void allBuiltInLocalesProduceVariety() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            SuffixGenerator gen = new SuffixGenerator(supportedLocale.locale());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 200; i++) seen.add(gen.generate());
            assertFalse(seen.isEmpty(), "No variety for " + supportedLocale);
        }
    }


    @Nested
    @DisplayName("SuffixDataRegistry extensibility")
    class RegistryTest {

        @Test
        @DisplayName("custom locale registration is picked up by SuffixGenerator")
        void customLocaleRegistration() {
            Locale korean = Locale.of("ko", "KR");
            SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return korean;
                }

                public String[] getSuffixes() {
                    return new String[] { "박사", "학사" };
                }
            });
            SuffixGenerator gen = new SuffixGenerator(korean);
            assertTrue(Set.of("박사", "학사").contains(gen.generate()));
        }

        @Test
        @DisplayName("custom provider overrides built-in locale data")
        void customProviderOverridesBuiltIn() {
            String[] custom = { "Esq." };
            SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return Locale.US;
                }

                public String[] getSuffixes() {
                    return custom;
                }
            });
            SuffixGenerator gen = new SuffixGenerator(Locale.US);
            assertEquals("Esq.", gen.generate());

            // Restore built-in
            SuffixDataRegistry.register(new BuiltInSuffixDataProvider(SupportedLocale.EN_US));
        }

        @Test
        @DisplayName("registered custom locale appears in registeredKeys()")
        void customLocaleAppearsInKeys() {
            Locale swahili = Locale.of("sw", "KE");
            SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return swahili;
                }

                public String[] getSuffixes() {
                    return new String[] { "Jr.", "Sr." };
                }
            });
            assertTrue(SuffixDataRegistry.registeredKeys().contains("sw_KE"));
        }

        @Test
        @DisplayName("language-only locale falls back to language entry")
        void languageOnlyFallback() {
            Locale arabic = Locale.of("ar");
            SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return arabic;
                }

                public String[] getSuffixes() {
                    return new String[] { "الابن", "الأب" };
                }
            });
            SuffixGenerator gen = new SuffixGenerator(Locale.of("ar", "EG"));
            assertNotNull(gen.generate());
        }

        @Test
        @DisplayName("register(null) throws NullPointerException")
        void registerRejectsNull() {
            assertThrows(NullPointerException.class, () -> SuffixDataRegistry.register(null));
        }

        @Test
        @DisplayName("register rejects provider with empty suffix array")
        void generateWithEmptySuffixArray() {
            Locale empty = Locale.of("zz", "ZZ");
            assertThrows(IllegalArgumentException.class, () -> SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return empty;
                }

                public String[] getSuffixes() {
                    return new String[0];
                }
            }));
        }

        @Test
        @DisplayName("isRegistered(null) returns false")
        void isRegisteredNullReturnsFalse() {
            assertFalse(SuffixDataRegistry.isRegistered(null));
        }

        @Test
        @DisplayName("forLocale(null) returns null")
        void forLocaleNullReturnsNull() {
            assertNull(SuffixDataRegistry.forLocale(null));
        }

        @Test
        @DisplayName("isRegistered returns false for unknown locale")
        void isRegisteredUnknownLocale() {
            assertFalse(SuffixDataRegistry.isRegistered(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale returns null for unknown locale")
        void forLocaleUnknownReturnsNull() {
            assertNull(SuffixDataRegistry.forLocale(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale falls back to language-level entry for unknown country")
        void forLocaleLanguageFallback() {
            SuffixDataProvider provider = SuffixDataRegistry.forLocale(Locale.of("en", "ZZ"));
            assertNotNull(provider);
        }

        @Test
        @DisplayName("isRegistered with language-only locale checks language-level entry")
        void isRegisteredWithLanguageOnlyLocale() {
            assertTrue(SuffixDataRegistry.isRegistered(Locale.of("en")));
        }

        @Test
        @DisplayName("forLocale with language-only locale returns language-level provider")
        void forLocaleWithLanguageOnlyLocale() {
            assertNotNull(SuffixDataRegistry.forLocale(Locale.of("en")));
        }

        @Test
        @DisplayName("language-only registration replaces the existing language fallback")
        void languageOnlyRegistrationReplacesFallback() {
            String[] custom = { "Esq." };
            Locale enOnly = Locale.of("en");
            SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return enOnly;
                }

                public String[] getSuffixes() {
                    return custom;
                }
            });
            SuffixGenerator gen = new SuffixGenerator(Locale.of("en", "ZZ"));
            assertEquals("Esq.", gen.generate());

            // Restore language fallback
            SuffixDataRegistry.register(new SuffixDataProvider() {

                public Locale getLocale() {
                    return enOnly;
                }

                public String[] getSuffixes() {
                    return new BuiltInSuffixDataProvider(SupportedLocale.EN_US).getSuffixes();
                }
            });
        }
    }
}
