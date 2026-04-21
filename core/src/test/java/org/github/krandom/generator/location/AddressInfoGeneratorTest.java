/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.DataRegistryContext;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AddressInfoGenerator")
class AddressInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured address payload")
    void generateAddressInfo() {
        AddressInfo info = new AddressInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertFalse(info.address().isBlank());
        assertFalse(info.street().isBlank());
        assertFalse(info.streetNumber().isBlank());
        assertFalse(info.streetName().isBlank());
        assertFalse(info.streetSuffix().isBlank());
        assertFalse(info.city().isBlank());
        assertFalse(info.state().isBlank());
        assertFalse(info.stateAbbr().isBlank());
        assertFalse(info.zip().isBlank());
        assertEquals("US", info.countryAbbr());
        assertTrue(info.street().contains(info.streetNumber()));
        assertTrue(info.street().contains(info.streetName()));
        assertTrue(info.street().contains(info.streetSuffix()));
        assertTrue(info.address().contains(info.city()));
        assertTrue(info.address().contains(info.zip()));
        assertTrue(info.address().contains(info.country()));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        AddressInfoGenerator one = new AddressInfoGenerator(config);
        AddressInfoGenerator two = new AddressInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new AddressInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new AddressInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new AddressInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofAddressInfo().generate());
        assertNotNull(Generators.ofAddressInfo(Locale.US).generate());
        assertNotNull(Generators.ofAddressInfo(GeneratorConfig.defaults()).generate());
    }

    @Test
    @DisplayName("isolated registry contexts cover no-city no-state and locale-without-country fallbacks")
    void isolatedRegistryFallbacks() {
        Locale locale = Locale.ENGLISH;
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerStreetAddressProvider(new StreetAddressDataProvider() {
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
                                                         })
                                                         .registerCountryProvider(new CountryDataProvider() {
                                                             @Override
                                                             public Locale getLocale() {
                                                                 return locale;
                                                             }

                                                             @Override
                                                             public String[] getCountries() {
                                                                 return new String[] { "Fallbackland" };
                                                             }
                                                         })
                                                         .build();

        AddressInfo info = new AddressInfoGenerator(
            GeneratorConfig.builder()
                           .locale(locale)
                           .registryContext(context)
                           .seed(7L)
                           .build()
        ).generate();

        assertTrue(info.city().isBlank());
        assertTrue(info.state().isBlank());
        assertTrue(info.stateAbbr().isBlank());
        assertEquals("Fallbackland", info.country());
    }

    @Test
    @DisplayName("missing state abbreviations and helper fallbacks are covered")
    void missingStateAbbreviationAndHelpers() throws Exception {
        Locale locale = Locale.US;
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerCityProvider(new CityDataProvider() {
                                                             @Override
                                                             public Locale getLocale() {
                                                                 return locale;
                                                             }

                                                             @Override
                                                             public String[] getCities() {
                                                                 return new String[] { "Testville" };
                                                             }
                                                         })
                                                         .registerStreetAddressProvider(new StreetAddressDataProvider() {
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
                                                         })
                                                         .registerStateProvider(new StateDataProvider() {
                                                             @Override
                                                             public Locale getLocale() {
                                                                 return locale;
                                                             }

                                                             @Override
                                                             public String[] getStates() {
                                                                 return new String[] { "Example State" };
                                                             }

                                                             @Override
                                                             public String[] getAbbreviations() {
                                                                 return new String[0];
                                                             }
                                                         })
                                                         .registerCountryProvider(new CountryDataProvider() {
                                                             @Override
                                                             public Locale getLocale() {
                                                                 return locale;
                                                             }

                                                             @Override
                                                             public String[] getCountries() {
                                                                 return new String[] { "United States" };
                                                             }
                                                         })
                                                         .build();

        AddressInfo info = new AddressInfoGenerator(
            GeneratorConfig.builder()
                           .locale(locale)
                           .registryContext(context)
                           .seed(5L)
                           .build()
        ).generate();

        assertEquals("", info.stateAbbr());
        assertTrue(info.address().contains("Example State"));

        Method safe = AddressInfoGenerator.class.getDeclaredMethod("safe", String.class);
        safe.setAccessible(true);
        assertEquals("", safe.invoke(null, new Object[] { null }));

        Method joinNonBlank = AddressInfoGenerator.class.getDeclaredMethod("joinNonBlank", String.class, String[].class);
        joinNonBlank.setAccessible(true);
        assertEquals("a, b", joinNonBlank.invoke(null, new Object[] { ", ", new String[] { "a", null, "", "b" } }));
    }
}
