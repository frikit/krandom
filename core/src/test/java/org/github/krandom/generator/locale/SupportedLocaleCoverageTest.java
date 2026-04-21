/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.locale;

import org.github.krandom.generator.location.CityDataRegistry;
import org.github.krandom.generator.location.CountryDataRegistry;
import org.github.krandom.generator.location.StateDataRegistry;
import org.github.krandom.generator.location.StreetAddressDataRegistry;
import org.github.krandom.generator.user.FirstNameDataRegistry;
import org.github.krandom.generator.user.GenderDataRegistry;
import org.github.krandom.generator.user.LastNameDataRegistry;
import org.github.krandom.generator.user.ProfessionDataRegistry;
import org.github.krandom.generator.user.SuffixDataRegistry;
import org.github.krandom.generator.user.TitleDataRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Supported locale coverage across providers and registries")
class SupportedLocaleCoverageTest {

    @Test
    @DisplayName("locale helpers expose enum locales in declaration order and reject unknown locales")
    void localeHelpersExposeDeclaredLocalesAndRejectUnknownLocales() {
        assertEquals(Arrays.stream(SupportedLocale.values()).map(SupportedLocale::locale).toList(),
                     SupportedLocale.locales());
        assertTrue(SupportedLocale.fromLocale(Locale.CANADA_FRENCH).isEmpty());
    }

    @Test
    @DisplayName("supported locale metadata exposes native and fallback tiers consistently")
    void supportedLocaleMetadataExposesConsistentTiers() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            assertEquals(supportedLocale, SupportedLocale.fromLocale(supportedLocale.locale()).orElseThrow());

            if (supportedLocale.resourceDataTier().isFallback()) {
                assertFalse(supportedLocale.resourceFallbackLocale().isEmpty(), "resource fallback missing " + supportedLocale);
                assertFalse(supportedLocale.resourcePrefix().equals(supportedLocale.canonicalResourcePrefix()),
                            "resource prefix should differ for fallback " + supportedLocale);
            } else {
                assertTrue(supportedLocale.resourceFallbackLocale().isEmpty(), "unexpected resource fallback " + supportedLocale);
                assertEquals(supportedLocale.canonicalResourcePrefix(), supportedLocale.resourcePrefix());
            }

            if (supportedLocale.professionDataTier().isFallback()) {
                assertFalse(supportedLocale.professionFallbackLocale().isEmpty(), "profession fallback missing " + supportedLocale);
                assertTrue(supportedLocale.qualityTier().isFallback(), "overall tier should reflect fallback " + supportedLocale);
            } else {
                assertTrue(supportedLocale.professionFallbackLocale().isEmpty(), "unexpected profession fallback " + supportedLocale);
            }

            assertEquals(LocaleDataQualityTier.max(supportedLocale.resourceDataTier(), supportedLocale.professionDataTier()),
                         supportedLocale.qualityTier(),
                         "overall tier mismatch " + supportedLocale);
        }

        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.EN_US.qualityTier());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.NL_NL.qualityTier());
        assertTrue(SupportedLocale.NL_NL.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.NL_NL.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.PL_PL.qualityTier());
        assertTrue(SupportedLocale.PL_PL.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.PL_PL.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.TR_TR.qualityTier());
        assertTrue(SupportedLocale.TR_TR.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.TR_TR.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.HI_IN.qualityTier());
        assertTrue(SupportedLocale.HI_IN.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.HI_IN.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.ALIAS_FALLBACK_DATASET,
                     LocaleDataQualityTier.max(LocaleDataQualityTier.NATIVE_DATASET,
                                               LocaleDataQualityTier.ALIAS_FALLBACK_DATASET));
    }

    @Test
    @DisplayName("user registries are seeded with exact providers and dataset shape for every supported locale")
    void userRegistriesCoverAllSupportedLocales() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            assertTrue(FirstNameDataRegistry.isRegistered(locale), "FirstNameDataRegistry missing " + locale);
            assertTrue(LastNameDataRegistry.isRegistered(locale), "LastNameDataRegistry missing " + locale);
            assertTrue(GenderDataRegistry.isRegistered(locale), "GenderDataRegistry missing " + locale);
            assertTrue(ProfessionDataRegistry.isRegistered(locale), "ProfessionDataRegistry missing " + locale);
            assertTrue(TitleDataRegistry.isRegistered(locale), "TitleDataRegistry missing " + locale);
            assertTrue(SuffixDataRegistry.isRegistered(locale), "SuffixDataRegistry missing " + locale);

            Object firstNameProvider = builtInProvider("org.github.krandom.generator.user.BuiltInFirstNameDataProvider", supportedLocale);
            Object lastNameProvider = builtInProvider("org.github.krandom.generator.user.BuiltInLastNameDataProvider", supportedLocale);
            Object genderProvider = builtInProvider("org.github.krandom.generator.user.BuiltInGenderDataProvider", supportedLocale);
            Object professionProvider = builtInProvider("org.github.krandom.generator.user.BuiltInProfessionDataProvider", supportedLocale);
            Object titleProvider = builtInProvider("org.github.krandom.generator.user.BuiltInTitleDataProvider", supportedLocale);
            Object suffixProvider = builtInProvider("org.github.krandom.generator.user.BuiltInSuffixDataProvider", supportedLocale);

            assertEquals(locale, invoke(firstNameProvider, "getLocale"));
            assertEquals(locale, invoke(lastNameProvider, "getLocale"));
            assertEquals(locale, invoke(genderProvider, "getLocale"));
            assertEquals(locale, invoke(professionProvider, "getLocale"));
            assertEquals(locale, invoke(titleProvider, "getLocale"));
            assertEquals(locale, invoke(suffixProvider, "getLocale"));

            assertTrue(stringArray(firstNameProvider, "getMaleFirstNames").length >= 40);
            assertTrue(stringArray(firstNameProvider, "getFemaleFirstNames").length >= 40);
            assertTrue(stringArray(lastNameProvider, "getLastNames").length >= 40);
            assertTrue(!stringValue(genderProvider, "getMaleLabel").isBlank());
            assertTrue(!stringValue(genderProvider, "getFemaleLabel").isBlank());
            assertTrue(!stringValue(genderProvider, "getMaleLabel").equals(stringValue(genderProvider, "getFemaleLabel")));
            assertEquals(25, stringArray(professionProvider, "getProfessions").length);
            assertEquals(25, intArray(professionProvider, "getWeights").length);
            assertTrue(stringArray(titleProvider, "getTitles").length >= 4);
            assertTrue(stringArray(suffixProvider, "getSuffixes").length >= 3);
        }
    }

    @Test
    @DisplayName("location registries are seeded with exact providers and dataset shape for every supported locale")
    void locationRegistriesCoverAllSupportedLocales() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            assertTrue(CityDataRegistry.isRegistered(locale), "CityDataRegistry missing " + locale);
            assertTrue(StateDataRegistry.isRegistered(locale), "StateDataRegistry missing " + locale);
            assertTrue(CountryDataRegistry.isRegistered(locale), "CountryDataRegistry missing " + locale);
            assertTrue(StreetAddressDataRegistry.isRegistered(locale), "StreetAddressDataRegistry missing " + locale);

            Object cityProvider = builtInProvider("org.github.krandom.generator.location.BuiltInCityDataProvider", supportedLocale);
            Object stateProvider = builtInProvider("org.github.krandom.generator.location.BuiltInStateDataProvider", supportedLocale);
            Object countryProvider = builtInProvider("org.github.krandom.generator.location.BuiltInCountryDataProvider", supportedLocale);
            Object streetProvider = builtInProvider("org.github.krandom.generator.location.BuiltInStreetAddressDataProvider", supportedLocale);

            assertEquals(locale, invoke(cityProvider, "getLocale"));
            assertEquals(locale, invoke(stateProvider, "getLocale"));
            assertEquals(locale, invoke(countryProvider, "getLocale"));
            assertEquals(locale, invoke(streetProvider, "getLocale"));

            assertTrue(stringArray(cityProvider, "getCities").length >= 70);
            assertTrue(stringArray(stateProvider, "getStates").length >= 4);
            assertEquals(stringArray(stateProvider, "getStates").length,
                         stringArray(stateProvider, "getAbbreviations").length);
            assertTrue(stringArray(countryProvider, "getCountries").length >= 150);
            assertTrue(stringArray(streetProvider, "getStreetNames").length >= 20);
            assertTrue(stringArray(streetProvider, "getStreetTypesShort").length >= 10);
            assertTrue(stringArray(streetProvider, "getStreetTypesLong").length >= 10);
        }
    }

    private static Object builtInProvider(String className, SupportedLocale supportedLocale) {
        try {
            Class<?> type = Class.forName(className);
            Constructor<?> constructor = type.getDeclaredConstructor(SupportedLocale.class);
            constructor.setAccessible(true);
            return constructor.newInstance(supportedLocale);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to construct built-in provider " + className, ex);
        }
    }

    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to invoke " + methodName + " on " + target.getClass().getName(), ex);
        }
    }

    private static String[] stringArray(Object target, String methodName) {
        return (String[]) invoke(target, methodName);
    }

    private static int[] intArray(Object target, String methodName) {
        return (int[]) invoke(target, methodName);
    }

    private static String stringValue(Object target, String methodName) {
        return (String) invoke(target, methodName);
    }
}
