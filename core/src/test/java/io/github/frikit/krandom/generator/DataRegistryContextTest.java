/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.location.CityDataProvider;
import io.github.frikit.krandom.generator.location.CountryDataProvider;
import io.github.frikit.krandom.generator.location.StateDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DataRegistryContext")
class DataRegistryContextTest {

    @Test
    @DisplayName("globalDefault delegates to static registries")
    void globalDefaultDelegatesToStaticRegistries() {
        DataRegistryContext context = DataRegistryContext.globalDefault();
        Locale locale = Locale.US;

        assertTrue(context.isFirstNameRegistered(locale));
        assertTrue(context.isLastNameRegistered(locale));
        assertTrue(context.isGenderRegistered(locale));
        assertTrue(context.isTitleRegistered(locale));
        assertTrue(context.isSuffixRegistered(locale));
        assertTrue(context.isProfessionRegistered(locale));
        assertTrue(context.isCityRegistered(locale));
        assertTrue(context.isStateRegistered(locale));
        assertTrue(context.isCountryRegistered(locale));
        assertTrue(context.isStreetAddressRegistered(locale));
        assertTrue(context.isNationalIdRegistered(locale));

        assertNotNull(context.firstNameProvider(locale));
        assertNotNull(context.lastNameProvider(locale));
        assertNotNull(context.genderProvider(locale));
        assertNotNull(context.titleProvider(locale));
        assertNotNull(context.suffixProvider(locale));
        assertNotNull(context.professionProvider(locale));
        assertNotNull(context.cityProvider(locale));
        assertNotNull(context.stateProvider(locale));
        assertNotNull(context.countryProvider(locale));
        assertNotNull(context.streetAddressProvider(locale));
        assertNotNull(context.nationalIdProvider(locale));

        assertFalse(context.firstNameRegisteredKeys().isEmpty());
        assertFalse(context.lastNameRegisteredKeys().isEmpty());
        assertFalse(context.genderRegisteredKeys().isEmpty());
        assertFalse(context.titleRegisteredKeys().isEmpty());
        assertFalse(context.suffixRegisteredKeys().isEmpty());
        assertFalse(context.professionRegisteredKeys().isEmpty());
        assertFalse(context.cityRegisteredKeys().isEmpty());
        assertFalse(context.stateRegisteredKeys().isEmpty());
        assertFalse(context.countryRegisteredKeys().isEmpty());
        assertFalse(context.streetAddressRegisteredKeys().isEmpty());
        assertFalse(context.nationalIdRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("isolated context does not fallback to global registries")
    void isolatedContextWithoutFallback() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        Locale locale = Locale.US;

        assertFalse(context.isFirstNameRegistered(locale));
        assertFalse(context.isLastNameRegistered(locale));
        assertFalse(context.isGenderRegistered(locale));
        assertFalse(context.isTitleRegistered(locale));
        assertFalse(context.isSuffixRegistered(locale));
        assertFalse(context.isProfessionRegistered(locale));
        assertFalse(context.isCityRegistered(locale));
        assertFalse(context.isStateRegistered(locale));
        assertFalse(context.isCountryRegistered(locale));
        assertFalse(context.isStreetAddressRegistered(locale));
        assertFalse(context.isNationalIdRegistered(locale));

        assertNull(context.firstNameProvider(locale));
        assertNull(context.lastNameProvider(locale));
        assertNull(context.genderProvider(locale));
        assertNull(context.titleProvider(locale));
        assertNull(context.suffixProvider(locale));
        assertNull(context.professionProvider(locale));
        assertNull(context.cityProvider(locale));
        assertNull(context.stateProvider(locale));
        assertNull(context.countryProvider(locale));
        assertNull(context.streetAddressProvider(locale));
        assertNull(context.nationalIdProvider(locale));
        assertNull(context.firstNameProvider(null));

        assertTrue(context.firstNameRegisteredKeys().isEmpty());
        assertTrue(context.lastNameRegisteredKeys().isEmpty());
        assertTrue(context.genderRegisteredKeys().isEmpty());
        assertTrue(context.titleRegisteredKeys().isEmpty());
        assertTrue(context.suffixRegisteredKeys().isEmpty());
        assertTrue(context.professionRegisteredKeys().isEmpty());
        assertTrue(context.cityRegisteredKeys().isEmpty());
        assertTrue(context.stateRegisteredKeys().isEmpty());
        assertTrue(context.countryRegisteredKeys().isEmpty());
        assertTrue(context.streetAddressRegisteredKeys().isEmpty());
        assertTrue(context.nationalIdRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("builder registration supports exact and language fallback lookups")
    void builderRegistrationLookupAndKeys() {
        Locale locale = Locale.US;
        Locale fallbackLocale = Locale.CANADA;

        DataRegistryContext context = DataRegistryContext.builder()
            .isolated()
            .registerFirstNameProvider(firstNameProvider(locale))
            .registerLastNameProvider(lastNameProvider(locale))
            .registerGenderProvider(genderProvider(locale))
            .registerTitleProvider(titleProvider(locale))
            .registerSuffixProvider(suffixProvider(locale))
            .registerProfessionProvider(professionProvider(locale))
            .registerCityProvider(cityProvider(locale))
            .registerStateProvider(stateProvider(locale))
            .registerCountryProvider(countryProvider(locale))
            .registerStreetAddressProvider(streetAddressProvider(locale))
            .registerNationalIdProvider(nationalIdProvider(locale))
            .build();

        assertEquals("m1", context.firstNameProvider(fallbackLocale).getMaleFirstNames()[0]);
        assertEquals("l1", context.lastNameProvider(fallbackLocale).getLastNames()[0]);
        assertEquals("male", context.genderProvider(fallbackLocale).getMaleLabel());
        assertEquals("Dr", context.titleProvider(fallbackLocale).getTitles()[0]);
        assertEquals("Jr", context.suffixProvider(fallbackLocale).getSuffixes()[0]);
        assertEquals("Engineer", context.professionProvider(fallbackLocale).getProfessions()[0]);
        assertEquals("CityOne", context.cityProvider(fallbackLocale).getCities()[0]);
        assertEquals("StateOne", context.stateProvider(fallbackLocale).getStates()[0]);
        assertEquals("CountryOne", context.countryProvider(fallbackLocale).getCountries()[0]);
        assertEquals("Main", context.streetAddressProvider(fallbackLocale).getStreetNames()[0]);
        assertEquals("NID", context.nationalIdProvider(fallbackLocale).generate(new Random(1L)));

        assertTrue(context.firstNameRegisteredKeys().contains("en_US"));
        assertTrue(context.firstNameRegisteredKeys().contains("en"));
        assertTrue(context.lastNameRegisteredKeys().contains("en_US"));
        assertTrue(context.genderRegisteredKeys().contains("en_US"));
        assertTrue(context.titleRegisteredKeys().contains("en_US"));
        assertTrue(context.suffixRegisteredKeys().contains("en_US"));
        assertTrue(context.professionRegisteredKeys().contains("en_US"));
        assertTrue(context.cityRegisteredKeys().contains("en_US"));
        assertTrue(context.stateRegisteredKeys().contains("en_US"));
        assertTrue(context.countryRegisteredKeys().contains("en_US"));
        assertTrue(context.streetAddressRegisteredKeys().contains("en_US"));
        assertTrue(context.nationalIdRegisteredKeys().contains("en_US"));
        assertTrue(context.nationalIdRegisteredKeys().contains("en"));
    }

    @Test
    @DisplayName("language-only locale registration stores language key")
    void languageOnlyRegistration() {
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerCountryProvider(countryProvider(Locale.of("en")))
                                                         .build();
        assertTrue(context.countryRegisteredKeys().contains("en"));
        assertEquals("CountryOne", context.countryProvider(Locale.of("en", "GB")).getCountries()[0]);
    }

    @Test
    @DisplayName("legacy registry key views are immutable snapshots")
    void legacyRegistryKeyViewsAreImmutableSnapshots() {
        Set<String> keys = FirstNameDataRegistry.registeredKeys();
        Locale testLocale = Locale.of("zz", "ZX");

        FirstNameDataRegistry.register(firstNameProvider(testLocale));

        assertFalse(keys.contains("zz_ZX"));
        assertThrows(UnsupportedOperationException.class, () -> keys.add("other"));
    }

    @Test
    @DisplayName("builder validates provider inputs")
    void builderValidatesInput() {
        Locale locale = Locale.US;
        DataRegistryContext.Builder builder = DataRegistryContext.builder().isolated();

        assertThrows(NullPointerException.class, () -> builder.registerFirstNameProvider(null));
        assertThrows(IllegalArgumentException.class, () -> builder.registerFirstNameProvider(new FirstNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[0];
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] { "f1" };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerLastNameProvider(new LastNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerGenderProvider(new GenderDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return " ";
            }

            @Override
            public String getFemaleLabel() {
                return "female";
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerGenderProvider(new GenderDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return null;
            }

            @Override
            public String getFemaleLabel() {
                return "female";
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerTitleProvider(new TitleDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerSuffixProvider(new SuffixDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "Engineer" };
            }

            @Override
            public int[] getWeights() {
                return new int[0];
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[0];
            }

            @Override
            public int[] getWeights() {
                return new int[0];
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "  " };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 1 };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { null };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 1 };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "Engineer" };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 0 };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerCityProvider(new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getCities() {
                return new String[] { "CityOne" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCityProvider(new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return new String[] { " " };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCityProvider(new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return new String[] { null };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getStates() {
                return new String[] { "StateOne" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { "" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { null };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { "StateOne" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { null };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerCountryProvider(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getCountries() {
                return new String[] { "CountryOne" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCountryProvider(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[] { " " };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCountryProvider(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[] { null };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerStreetAddressProvider(new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "Main" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[0];
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "Street" };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerNationalIdProvider(new NationalIdProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String generate(Random random) {
                return "x";
            }
        }));

        assertThrows(NullPointerException.class, () -> DataRegistryContext.builder().useGlobalFallback(true)
                                                                           .registerFirstNameProvider(new FirstNameDataProvider() {
                                                                               @Override
                                                                               public Locale getLocale() {
                                                                                   return null;
                                                                               }

                                                                               @Override
                                                                               public String[] getMaleFirstNames() {
                                                                                   return new String[] { "m1" };
                                                                               }

                                                                               @Override
                                                                               public String[] getFemaleFirstNames() {
                                                                                   return new String[] { "f1" };
                                                                               }
                                                                           }));
    }

    private static FirstNameDataProvider firstNameProvider(Locale locale) {
        return new FirstNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[] { "m1" };
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] { "f1" };
            }
        };
    }

    private static LastNameDataProvider lastNameProvider(Locale locale) {
        return new LastNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[] { "l1" };
            }
        };
    }

    private static GenderDataProvider genderProvider(Locale locale) {
        return new GenderDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return "male";
            }

            @Override
            public String getFemaleLabel() {
                return "female";
            }
        };
    }

    private static TitleDataProvider titleProvider(Locale locale) {
        return new TitleDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[] { "Dr" };
            }
        };
    }

    private static SuffixDataProvider suffixProvider(Locale locale) {
        return new SuffixDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[] { "Jr" };
            }
        };
    }

    private static ProfessionDataProvider professionProvider(Locale locale) {
        return new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "Engineer" };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 1 };
            }
        };
    }

    private static CityDataProvider cityProvider(Locale locale) {
        return new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return new String[] { "CityOne" };
            }
        };
    }

    private static StateDataProvider stateProvider(Locale locale) {
        return new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { "StateOne" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        };
    }

    private static CountryDataProvider countryProvider(Locale locale) {
        return new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[] { "CountryOne" };
            }
        };
    }

    private static StreetAddressDataProvider streetAddressProvider(Locale locale) {
        return new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "Main" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "St" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "Street" };
            }
        };
    }

    private static NationalIdProvider nationalIdProvider(Locale locale) {
        return new NationalIdProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String generate(Random random) {
                return "NID";
            }
        };
    }
}
