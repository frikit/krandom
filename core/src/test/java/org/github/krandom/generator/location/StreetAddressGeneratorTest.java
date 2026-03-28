/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.DataRegistryContext;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("StreetAddressGenerator")
class StreetAddressGeneratorTest {

    private static final int SAMPLES = 80;

    @Test
    @DisplayName("default constructor uses Locale.US")
    void defaultConstructorUsesUs() {
        StreetAddressGenerator gen = new StreetAddressGenerator();
        assertEquals(Locale.US, gen.getLocale());
        assertTrue(gen.getStreetNameCount() > 0);
    }

    @Test
    @DisplayName("locale constructor stores locale")
    void localeConstructor() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.GERMANY);
        assertEquals(Locale.GERMANY, gen.getLocale());
    }

    @Test
    @DisplayName("config constructor stores locale")
    void configConstructor() {
        GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.JAPAN).build();
        StreetAddressGenerator gen = new StreetAddressGenerator(cfg);
        assertEquals(Locale.JAPAN, gen.getLocale());
    }

    @Test
    @DisplayName("generate() returns three-part address")
    void generateThreePart() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        for (int i = 0; i < SAMPLES; i++) {
            String[] parts = gen.generate().split(" ");
            assertEquals(3, parts.length);
        }
    }

    @Test
    @DisplayName("number is in [1,9999]")
    void numberRange() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        for (int i = 0; i < SAMPLES; i++) {
            int number = Integer.parseInt(gen.generateStreetAddressNumber());
            assertTrue(number >= 1 && number <= 9999);
        }
    }

    @Test
    @DisplayName("street-level component methods return non-empty values")
    void streetComponentMethods() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        assertFalse(gen.generateStreetName().isBlank());
        assertFalse(gen.generateStreetSuffix().isBlank());
        assertFalse(gen.generateStreetSuffix(false).isBlank());
        assertFalse(gen.generateBuildingNumber().isBlank());
        assertFalse(gen.generateSecondaryAddress().isBlank());
    }

    @Test
    @DisplayName("secondary address follows unit + number pattern")
    void secondaryAddressPattern() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        for (int i = 0; i < SAMPLES; i++) {
            String value = gen.generateSecondaryAddress();
            assertTrue(value.matches(".+ \\d{1,3}"), "Unexpected secondary address format: " + value);
        }
    }

    @Test
    @DisplayName("full address includes core components")
    void fullAddressIncludesComponents() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        String full = gen.generateFullAddress();
        assertTrue(full.contains(", "), "Full address should contain comma separators: " + full);
        assertTrue(full.matches(".*\\d{5}(-\\d{4})?.*"), "Full address should contain postal code: " + full);
    }

    @Test
    @DisplayName("generate(true) uses short suffixes")
    void shortSuffixMode() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        Set<String> shortSuffixes = Set.of("St", "Ave", "Blvd", "Dr", "Rd", "Ln", "Ct", "Pl", "Way", "Cir");
        for (int i = 0; i < SAMPLES; i++) {
            String suffix = gen.generate(true).split(" ")[2];
            assertTrue(shortSuffixes.contains(suffix));
        }
    }

    @Test
    @DisplayName("generate(false) uses long suffixes")
    void longSuffixMode() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        Set<String> longSuffixes = Set.of("Street", "Avenue", "Boulevard", "Drive", "Road", "Lane", "Court", "Place", "Way", "Circle");
        for (int i = 0; i < SAMPLES; i++) {
            String suffix = gen.generate(false).split(" ")[2];
            assertTrue(longSuffixes.contains(suffix));
        }
    }

    @Test
    @DisplayName("locale-specific German street names appear")
    void germanLocaleNames() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.GERMANY);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            seen.add(gen.generate(false));
        }
        assertTrue(seen.stream().anyMatch(s -> s.contains("Haupt") || s.contains("Schloss") || s.contains("Bahnhof")));
    }

    @Test
    @DisplayName("locale-specific Japanese street names appear")
    void japaneseLocaleNames() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.JAPAN);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            seen.add(gen.generate(false));
        }
        assertTrue(seen.stream().anyMatch(s -> s.codePoints().anyMatch(cp -> cp > 127)));
    }

    @Test
    @DisplayName("seeded generation reproducible for short and long modes")
    void seededReproducibility() {
        GeneratorConfig cfg1 = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        GeneratorConfig cfg2 = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        StreetAddressGenerator a = new StreetAddressGenerator(cfg1);
        StreetAddressGenerator b = new StreetAddressGenerator(cfg2);

        for (int i = 0; i < SAMPLES; i++) {
            assertEquals(a.generate(), b.generate());
            assertEquals(a.generate(false), b.generate(false));
            assertEquals(a.generateSecondaryAddress(), b.generateSecondaryAddress());
            assertEquals(a.generateFullAddress(), b.generateFullAddress());
        }
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeedsDifferent() {
        GeneratorConfig cfg1 = GeneratorConfig.builder().locale(Locale.US).seed(1L).build();
        GeneratorConfig cfg2 = GeneratorConfig.builder().locale(Locale.US).seed(2L).build();
        StreetAddressGenerator a = new StreetAddressGenerator(cfg1);
        StreetAddressGenerator b = new StreetAddressGenerator(cfg2);

        boolean anyDiff = false;
        for (int i = 0; i < 20; i++) {
            if (!a.generate().equals(b.generate())) {
                anyDiff = true;
                break;
            }
        }
        assertTrue(anyDiff);
    }

    @Test
    @DisplayName("unsupported locale throws UnsupportedOperationException")
    void unsupportedLocaleThrows() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new StreetAddressGenerator(Locale.of("xx", "YY"))
        );
        assertTrue(ex.getMessage().contains("not supported"));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new StreetAddressGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        assertThrows(NullPointerException.class, () -> new StreetAddressGenerator((Locale) null));
    }

    @Test
    @DisplayName("isLocaleExplicitlySupported returns true for built-in locale")
    void isLocaleSupported() {
        assertTrue(new StreetAddressGenerator(Locale.US).isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("generateList and stream produce expected counts")
    void generateListAndStream() {
        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        List<String> list = gen.generateList(10);
        assertEquals(10, list.size());
        assertEquals(15, gen.stream().limit(15).count());
    }

    @Test
    @DisplayName("registry custom locale registration works")
    void customLocaleRegistration() {
        Locale koKr = Locale.of("ko", "KR");
        StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return koKr;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "한강", "남산" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "로", "길" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "대로", "거리" };
            }
        });

        StreetAddressGenerator gen = new StreetAddressGenerator(koKr);
        assertNotNull(gen.generate());
        assertNotNull(gen.generateFullAddress());
        assertTrue(StreetAddressDataRegistry.registeredKeys().contains("ko_KR"));
    }

    @Test
    @DisplayName("full address works when city/state/country registries are absent for locale")
    void fullAddressWithoutOtherRegistries() {
        Locale zzZz = Locale.of("zz", "ZZ");
        StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return zzZz;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "Fallback" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "St" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "Street" };
            }
        });

        StreetAddressGenerator gen = new StreetAddressGenerator(zzZz);
        String full = gen.generateFullAddress();
        assertTrue(full.contains("Fallback"));
        assertNotNull(full);
    }

    @Test
    @DisplayName("full address omits state segment when scoped context has no state provider")
    void fullAddressWithoutStateProviderInScopedContext() {
        Locale locale = Locale.of("qq", "QQ");
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerStreetAddressProvider(new StreetAddressDataProvider() {
                                                             @Override
                                                             public Locale getLocale() {
                                                                 return locale;
                                                             }

                                                             @Override
                                                             public String[] getStreetNames() {
                                                                 return new String[] { "Scoped" };
                                                             }

                                                             @Override
                                                             public String[] getStreetTypesShort() {
                                                                 return new String[] { "St" };
                                                             }

                                                             @Override
                                                             public String[] getStreetTypesLong() {
                                                                 return new String[] { "Street" };
                                                             }
                                                         })
                                                         .build();

        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(locale)
                                                .seed(9L)
                                                .registryContext(context)
                                                .build();
        StreetAddressGenerator generator = new StreetAddressGenerator(config);
        String full = generator.generateFullAddress();
        assertTrue(full.contains("Scoped"));
        assertFalse(full.contains(", "));
    }

    @Test
    @DisplayName("registry override built-in locale works")
    void registryOverrideBuiltIn() {
        StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.US;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "TestStreet" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "TS" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "TestSuffix" };
            }
        });

        StreetAddressGenerator gen = new StreetAddressGenerator(Locale.US);
        assertTrue(gen.generate(true).contains("TestStreet TS"));
        assertTrue(gen.generate(false).contains("TestStreet TestSuffix"));

        StreetAddressDataRegistry.register(new BuiltInStreetAddressDataProvider(SupportedLocale.EN_US));
    }

    @Test
    @DisplayName("registry null and validation errors")
    void registryValidation() {
        assertThrows(NullPointerException.class, () -> StreetAddressDataRegistry.register(null));
        assertFalse(StreetAddressDataRegistry.isRegistered(null));
        assertNull(StreetAddressDataRegistry.forLocale(null));

        assertThrows(NullPointerException.class, () -> StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "A" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "B" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "C" };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.of("tt", "TT");
            }

            @Override
            public String[] getStreetNames() {
                return new String[0];
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "B" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "C" };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.of("uu", "UU");
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "A" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { " " };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "C" };
            }
        }));

        assertThrows(NullPointerException.class, () -> StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.of("vv", "VV");
            }

            @Override
            public String[] getStreetNames() {
                return null;
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "B" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "C" };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> StreetAddressDataRegistry.register(new StreetAddressDataProvider() {

            @Override
            public Locale getLocale() {
                return Locale.of("ww", "WW");
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "A", null };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "B" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "C" };
            }
        }));
    }

    @Test
    @DisplayName("Generators.ofStreetAddress returns working generator")
    void generatorsFactory() {
        assertNotNull(Generators.ofStreetAddress().generate());
    }
}
