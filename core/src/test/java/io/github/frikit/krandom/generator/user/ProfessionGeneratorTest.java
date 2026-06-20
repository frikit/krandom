/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProfessionGenerator")
class ProfessionGeneratorTest {

    private static final int SAMPLES = 100;

    private static void assertLanguageFallbackMatches(Locale fallbackLocale, Locale expectedBaseLocale) {
        GeneratorConfig fallbackCfg = GeneratorConfig.builder().locale(fallbackLocale).seed(123456L).build();
        GeneratorConfig baseCfg = GeneratorConfig.builder().locale(expectedBaseLocale).seed(123456L).build();
        ProfessionGenerator fallbackGenerator = new ProfessionGenerator(fallbackCfg);
        ProfessionGenerator baseGenerator = new ProfessionGenerator(baseCfg);

        for (int i = 0; i < SAMPLES; i++) {
            assertEquals(baseGenerator.generate(), fallbackGenerator.generate());
        }

        fallbackGenerator = new ProfessionGenerator(fallbackCfg);
        baseGenerator = new ProfessionGenerator(baseCfg);
        for (int i = 0; i < SAMPLES; i++) {
            assertEquals(baseGenerator.generateRanked(), fallbackGenerator.generateRanked());
        }
    }

    private static void restoreProfessionRegistryBaseline() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            ProfessionDataRegistry.register(new BuiltInProfessionDataProvider(supportedLocale));
        }

        registerLanguageFallback(Locale.of("en"), SupportedLocale.EN_US);
        registerLanguageFallback(Locale.of("fr"), SupportedLocale.FR_FR);
        registerLanguageFallback(Locale.of("de"), SupportedLocale.DE_DE);
        registerLanguageFallback(Locale.of("ja"), SupportedLocale.JA_JP);
        registerLanguageFallback(Locale.of("es"), SupportedLocale.ES_ES);
        registerLanguageFallback(Locale.of("it"), SupportedLocale.IT_IT);
        registerLanguageFallback(Locale.of("pt"), SupportedLocale.PT_BR);
        registerLanguageFallback(Locale.of("zh"), SupportedLocale.ZH_CN);

        ProfessionDataRegistry.register(new ProfessionDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.of("en", "CA");
            }

            @Override
            public String[] getProfessions() {
                return new BuiltInProfessionDataProvider(SupportedLocale.EN_US).getProfessions();
            }

            @Override
            public int[] getWeights() {
                return new BuiltInProfessionDataProvider(SupportedLocale.EN_US).getWeights();
            }
        });
    }

    private static void registerLanguageFallback(Locale locale, SupportedLocale source) {
        ProfessionDataProvider provider = new BuiltInProfessionDataProvider(source);
        ProfessionDataRegistry.register(new ProfessionDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return provider.getProfessions();
            }

            @Override
            public int[] getWeights() {
                return provider.getWeights();
            }
        });
    }


    @Nested
    @DisplayName("Built-in data")
    class BuiltInDataTests {

        @BeforeEach
        void resetBaseline() {
            restoreProfessionRegistryBaseline();
        }

        @Test
        @DisplayName("all built-in locale providers have at least 40 professions")
        void builtInLocaleCounts() {
            for (SupportedLocale supportedLocale : SupportedLocale.values()) {
                ProfessionDataProvider data = new BuiltInProfessionDataProvider(supportedLocale);
                assertTrue(data.getProfessions().length >= 40);
                assertEquals(data.getProfessions().length, data.getWeights().length);
            }
        }

        @Test
        @DisplayName("default constructor uses Locale.US")
        void defaultLocale() {
            assertEquals(Locale.US, new ProfessionGenerator().getLocale());
        }

        @Test
        @DisplayName("locale constructor stores locale")
        void localeConstructor() {
            assertEquals(Locale.GERMANY, new ProfessionGenerator(Locale.GERMANY).getLocale());
        }

        @Test
        @DisplayName("config constructor stores locale")
        void configConstructor() {
            GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.JAPAN).build();
            assertEquals(Locale.JAPAN, new ProfessionGenerator(cfg).getLocale());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class, () -> new ProfessionGenerator((GeneratorConfig) null));
        }

        @Test
        @DisplayName("generate returns non-null non-blank profession")
        void generate() {
            ProfessionGenerator gen = new ProfessionGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                String profession = gen.generate();
                assertNotNull(profession);
                assertFalse(profession.isBlank());
            }
        }

        @Test
        @DisplayName("generate(false) matches uniform mode")
        void generateFalse() {
            ProfessionGenerator gen = new ProfessionGenerator();
            String profession = gen.generate(false);
            assertNotNull(profession);
            assertFalse(profession.isBlank());
        }

        @Test
        @DisplayName("generate(true) returns ranked profession")
        void generateTrueRanked() {
            ProfessionGenerator gen = new ProfessionGenerator();
            String profession = gen.generate(true);
            assertNotNull(profession);
            assertFalse(profession.isBlank());
        }

        @Test
        @DisplayName("generateRanked returns non-null non-blank profession")
        void generateRanked() {
            ProfessionGenerator gen = new ProfessionGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                String profession = gen.generateRanked();
                assertNotNull(profession);
                assertFalse(profession.isBlank());
            }
        }

        @Test
        @DisplayName("language fallback maps to expected locale datasets")
        void languageFallbackMappings() {
            assertLanguageFallbackMatches(Locale.of("en", "CA"), Locale.US);
            assertLanguageFallbackMatches(Locale.of("fr", "CA"), Locale.FRANCE);
            assertLanguageFallbackMatches(Locale.of("de", "AT"), Locale.GERMANY);
            assertLanguageFallbackMatches(Locale.of("ja", "US"), Locale.JAPAN);
            assertLanguageFallbackMatches(Locale.of("es", "MX"), Locale.of("es", "ES"));
            assertLanguageFallbackMatches(Locale.of("it", "CH"), Locale.ITALY);
            assertLanguageFallbackMatches(Locale.of("pt", "PT"), Locale.of("pt", "BR"));
            assertLanguageFallbackMatches(Locale.of("zh", "TW"), Locale.CHINA);
        }

        @Test
        @DisplayName("unsupported locale without language fallback throws")
        void unsupportedLocaleThrows() {
            UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> new ProfessionGenerator(Locale.of("am", "ET"))
            );
            assertTrue(ex.getMessage().contains("am_ET"));
        }

        @Test
        @DisplayName("Japanese locale yields Japanese profession values")
        void japaneseLocaleValues() {
            ProfessionGenerator gen = new ProfessionGenerator(Locale.JAPAN);
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < SAMPLES; i++) {
                seen.add(gen.generate());
            }
            assertTrue(seen.stream().anyMatch(s -> s.codePoints().anyMatch(cp -> cp > 127)),
                       "Expected at least one non-ASCII Japanese profession");
        }

        @Test
        @DisplayName("seeded generators produce identical generate() sequence")
        void seededGenerateReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(42L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(42L).build();
            ProfessionGenerator a = new ProfessionGenerator(cfg1);
            ProfessionGenerator b = new ProfessionGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("seeded generators produce identical ranked sequence")
        void seededRankedReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(77L).locale(Locale.GERMANY).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(77L).locale(Locale.GERMANY).build();
            ProfessionGenerator a = new ProfessionGenerator(cfg1);
            ProfessionGenerator b = new ProfessionGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generateRanked(), b.generateRanked());
            }
        }
    }


    @Nested
    @DisplayName("Custom data per instance")
    class CustomInstanceDataTests {

        @BeforeEach
        void resetBaseline() {
            restoreProfessionRegistryBaseline();
        }

        @Test
        @DisplayName("custom professions constructor uses only provided set")
        void customProfessionsOnly() {
            ProfessionGenerator generator = new ProfessionGenerator(
                Locale.US,
                new String[] { "A", "B", "C" }
            );

            for (int i = 0; i < SAMPLES; i++) {
                assertTrue(Set.of("A", "B", "C").contains(generator.generate()));
            }
            assertEquals(3, generator.getProfessionCount());
        }

        @Test
        @DisplayName("custom professions+weights constructor supports ranked")
        void customProfessionsWithWeights() {
            GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).seed(3L).build();
            ProfessionGenerator generator = new ProfessionGenerator(
                config,
                new String[] { "Common", "Rare" },
                new int[] { 100, 1 }
            );

            Set<String> seen = new HashSet<>();
            for (int i = 0; i < SAMPLES; i++) {
                seen.add(generator.generateRanked());
            }
            assertTrue(seen.contains("Common"));
        }

        @Test
        @DisplayName("custom constructor validates array lengths")
        void customArrayLengthValidation() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ProfessionGenerator(Locale.US, new String[] { "A" }, new int[] { 1, 2 }));
        }

        @Test
        @DisplayName("custom constructor validates empty profession list")
        void customEmptyValidation() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ProfessionGenerator(Locale.US, new String[0], new int[0]));
        }

        @Test
        @DisplayName("custom constructor rejects blank profession")
        void customBlankValidation() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ProfessionGenerator(Locale.US, new String[] { " ", "B" }, new int[] { 1, 1 }));
        }

        @Test
        @DisplayName("custom constructor rejects null profession entry")
        void customNullProfessionValidation() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ProfessionGenerator(Locale.US, new String[] { null, "B" }, new int[] { 1, 1 }));
        }

        @Test
        @DisplayName("custom constructor rejects non-positive weight")
        void customWeightValidation() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ProfessionGenerator(Locale.US, new String[] { "A" }, new int[] { 0 }));
        }

        @Test
        @DisplayName("locale + professions + weights constructor works")
        void customLocaleWeightsConstructorWorks() {
            ProfessionGenerator generator = new ProfessionGenerator(
                Locale.US,
                new String[] { "Primary", "Secondary" },
                new int[] { 5, 1 }
            );
            assertNotNull(generator.generate());
            assertNotNull(generator.generateRanked());
        }
    }


    @Nested
    @DisplayName("Registry extensibility")
    class RegistryExtensibilityTests {

        @BeforeEach
        void resetBaseline() {
            restoreProfessionRegistryBaseline();
        }

        @Test
        @DisplayName("custom locale registration is picked up")
        void customLocaleRegistration() {
            Locale korean = Locale.of("ko", "KR");
            ProfessionDataRegistry.register(new ProfessionDataProvider() {

                @Override
                public Locale getLocale() {
                    return korean;
                }

                @Override
                public String[] getProfessions() {
                    return new String[] { "개발자", "교사" };
                }

                @Override
                public int[] getWeights() {
                    return new int[] { 5, 1 };
                }
            });

            ProfessionGenerator gen = new ProfessionGenerator(korean);
            assertTrue(Set.of("개발자", "교사").contains(gen.generate()));
            assertTrue(ProfessionDataRegistry.registeredKeys().contains("ko_KR"));
        }

        @Test
        @DisplayName("custom provider overrides built-in locale data")
        void customProviderOverridesBuiltIn() {
            ProfessionDataRegistry.register(new ProfessionDataProvider() {

                @Override
                public Locale getLocale() {
                    return Locale.US;
                }

                @Override
                public String[] getProfessions() {
                    return new String[] { "TestProfession" };
                }

                @Override
                public int[] getWeights() {
                    return new int[] { 1 };
                }
            });

            ProfessionGenerator gen = new ProfessionGenerator(Locale.US);
            for (int i = 0; i < 20; i++) {
                assertEquals("TestProfession", gen.generate());
                assertEquals("TestProfession", gen.generateRanked());
            }

            ProfessionDataRegistry.register(new BuiltInProfessionDataProvider(SupportedLocale.EN_US));
        }

        @Test
        @DisplayName("append adds more professions to existing locale")
        void appendToExistingLocale() {
            ProfessionDataProvider before = ProfessionDataRegistry.forLocale(Locale.US);
            int beforeCount = before.getProfessions().length;

            ProfessionDataRegistry.append(Locale.US, new String[] { "Cloud Reliability Engineer" }, new int[] { 5 });
            ProfessionDataProvider after = ProfessionDataRegistry.forLocale(Locale.US);

            assertEquals(beforeCount + 1, after.getProfessions().length);
            assertTrue(Set.of(after.getProfessions()).contains("Cloud Reliability Engineer"));

            ProfessionDataRegistry.register(new BuiltInProfessionDataProvider(SupportedLocale.EN_US));
        }

        @Test
        @DisplayName("append to locale using language fallback creates exact locale provider")
        void appendOnLanguageFallbackCreatesExactProvider() {
            Locale locale = Locale.of("en", "CA");
            ProfessionDataRegistry.append(locale, new String[] { "AI Governance Specialist" }, new int[] { 4 });
            ProfessionDataProvider provider = ProfessionDataRegistry.forLocale(locale);
            assertNotNull(provider);
            assertEquals(locale, provider.getLocale());
            assertTrue(Set.of(provider.getProfessions()).contains("AI Governance Specialist"));
        }

        @Test
        @DisplayName("append with new locale creates provider")
        void appendCreatesNewLocale() {
            Locale swahili = Locale.of("sw", "KE");
            ProfessionDataRegistry.append(swahili, new String[] { "Mhandisi wa Data", "Daktari" }, new int[] { 3, 2 });

            ProfessionGenerator generator = new ProfessionGenerator(swahili);
            assertNotNull(generator.generate());
            assertEquals(2, generator.getProfessionCount());
        }

        @Test
        @DisplayName("append uniform overload works")
        void appendUniformWorks() {
            Locale testLocale = Locale.of("zu", "ZA");
            ProfessionDataRegistry.append(testLocale, new String[] { "Umakhi", "Uthisha" });
            ProfessionGenerator generator = new ProfessionGenerator(testLocale);
            assertEquals(2, generator.getProfessionCount());
        }

        @Test
        @DisplayName("register language-only provider updates language fallback")
        void registerLanguageOnlyProvider() {
            Locale languageOnly = Locale.of("en");
            ProfessionDataRegistry.register(new ProfessionDataProvider() {

                @Override
                public Locale getLocale() {
                    return languageOnly;
                }

                @Override
                public String[] getProfessions() {
                    return new String[] { "LanguageFallbackOnly" };
                }

                @Override
                public int[] getWeights() {
                    return new int[] { 1 };
                }
            });

            ProfessionDataProvider provider = ProfessionDataRegistry.forLocale(Locale.of("en", "ZZ"));
            assertNotNull(provider);
            assertEquals(languageOnly, provider.getLocale());
            assertEquals("LanguageFallbackOnly", provider.getProfessions()[0]);

            ProfessionDataRegistry.register(new BuiltInProfessionDataProvider(SupportedLocale.EN_US));
        }

        @Test
        @DisplayName("register null throws")
        void registerNullThrows() {
            assertThrows(NullPointerException.class, () -> ProfessionDataRegistry.register(null));
        }

        @Test
        @DisplayName("append validations")
        void appendValidations() {
            assertThrows(NullPointerException.class, () -> ProfessionDataRegistry.append(null, new String[] { "A" }, new int[] { 1 }));
            assertThrows(NullPointerException.class, () -> ProfessionDataRegistry.append(Locale.US, null, new int[] { 1 }));
            assertThrows(NullPointerException.class, () -> ProfessionDataRegistry.append(Locale.US, new String[] { "A" }, null));
            assertThrows(IllegalArgumentException.class, () -> ProfessionDataRegistry.append(Locale.US, new String[0], new int[0]));
            assertThrows(IllegalArgumentException.class, () -> ProfessionDataRegistry.append(Locale.US, new String[] { "A" }, new int[] { 1, 2 }));
            assertThrows(IllegalArgumentException.class, () -> ProfessionDataRegistry.append(Locale.US, new String[] { null }, new int[] { 1 }));
            assertThrows(IllegalArgumentException.class, () -> ProfessionDataRegistry.append(Locale.US, new String[] { " " }, new int[] { 1 }));
            assertThrows(IllegalArgumentException.class, () -> ProfessionDataRegistry.append(Locale.US, new String[] { "A" }, new int[] { 0 }));
        }

        @Test
        @DisplayName("isRegistered and forLocale null behavior")
        void nullLookupBehavior() {
            assertFalse(ProfessionDataRegistry.isRegistered(null));
            assertNull(ProfessionDataRegistry.forLocale(null));
        }

        @Test
        @DisplayName("isRegistered returns true and false for known and unknown locales")
        void isRegisteredKnownAndUnknown() {
            assertTrue(ProfessionDataRegistry.isRegistered(Locale.US));
            assertTrue(ProfessionDataRegistry.isRegistered(Locale.of("en")));
            assertFalse(ProfessionDataRegistry.isRegistered(Locale.of("qq", "QQ")));
        }

        @Test
        @DisplayName("forLocale supports language-only lookup")
        void languageOnlyLookup() {
            assertNotNull(ProfessionDataRegistry.forLocale(Locale.of("en")));
        }

        @Test
        @DisplayName("isLocaleExplicitlySupported distinguishes fallback")
        void explicitSupportCheck() {
            ProfessionGenerator fallbackGenerator = new ProfessionGenerator(Locale.of("en", "SG"));
            assertFalse(fallbackGenerator.isLocaleExplicitlySupported());

            ProfessionGenerator exactGenerator = new ProfessionGenerator(Locale.US);
            assertTrue(exactGenerator.isLocaleExplicitlySupported());
        }

        @Test
        @DisplayName("isLocaleExplicitlySupported is false when using per-instance custom data")
        void explicitSupportWithInstanceCustomData() {
            ProfessionGenerator generator = new ProfessionGenerator(
                Locale.of("xx", "YY"),
                new String[] { "Custom" },
                new int[] { 1 }
            );
            assertFalse(generator.isLocaleExplicitlySupported());
        }
    }


    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofProfession returns working generator")
        void factory() {
            assertNotNull(Generators.ofProfession().generate());
        }
    }
}
