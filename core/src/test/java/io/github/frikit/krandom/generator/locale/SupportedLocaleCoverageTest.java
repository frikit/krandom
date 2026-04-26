/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.locale;

import io.github.frikit.krandom.generator.location.CityDataRegistry;
import io.github.frikit.krandom.generator.location.CountryDataRegistry;
import io.github.frikit.krandom.generator.location.StateDataRegistry;
import io.github.frikit.krandom.generator.location.StreetAddressDataRegistry;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataRegistry;
import io.github.frikit.krandom.generator.user.LastNameDataRegistry;
import io.github.frikit.krandom.generator.user.ProfessionDataRegistry;
import io.github.frikit.krandom.generator.user.SuffixDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Supported locale coverage across providers and registries")
class SupportedLocaleCoverageTest {

    private static final double MIN_UNIQUE_RATIO          = 0.95d;
    private static final double MIN_EXPECTED_SCRIPT_RATIO = 0.90d;

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
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.CS_CZ.qualityTier());
        assertTrue(SupportedLocale.CS_CZ.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.CS_CZ.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.KO_KR.qualityTier());
        assertTrue(SupportedLocale.KO_KR.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.KO_KR.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.RU_RU.qualityTier());
        assertTrue(SupportedLocale.RU_RU.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.RU_RU.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.TR_TR.qualityTier());
        assertTrue(SupportedLocale.TR_TR.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.TR_TR.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.SV_SE.qualityTier());
        assertTrue(SupportedLocale.SV_SE.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.SV_SE.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.NB_NO.qualityTier());
        assertTrue(SupportedLocale.NB_NO.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.NB_NO.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.AR_SA.qualityTier());
        assertTrue(SupportedLocale.AR_SA.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.AR_SA.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.NATIVE_DATASET, SupportedLocale.HI_IN.qualityTier());
        assertTrue(SupportedLocale.HI_IN.resourceFallbackLocale().isEmpty());
        assertTrue(SupportedLocale.HI_IN.professionFallbackLocale().isEmpty());
        assertEquals(LocaleDataQualityTier.ALIAS_FALLBACK_DATASET,
                     LocaleDataQualityTier.max(LocaleDataQualityTier.NATIVE_DATASET,
                                               LocaleDataQualityTier.ALIAS_FALLBACK_DATASET));
    }

    @Test
    @DisplayName("fallback locale helpers still support synthetic fallback metadata paths")
    void fallbackLocaleHelpersSupportSyntheticFallbackMetadataPaths() {
        assertTrue(SupportedLocale.resolveFallbackLocale("de_DE", LocaleDataQualityTier.NATIVE_DATASET).isEmpty());
        assertEquals(SupportedLocale.DE_DE,
                     SupportedLocale.resolveFallbackLocale("de_DE", LocaleDataQualityTier.ALIAS_FALLBACK_DATASET)
                                    .orElseThrow());
        assertEquals(SupportedLocale.JA_JP,
                     SupportedLocale.resolveFallbackLocale("ja_JP", LocaleDataQualityTier.CURATED_FALLBACK_DATASET)
                                    .orElseThrow());
        assertTrue(SupportedLocale.resolveFallbackLocale("missing_locale", LocaleDataQualityTier.ALIAS_FALLBACK_DATASET).isEmpty());
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

            Object firstNameProvider = builtInProvider("io.github.frikit.krandom.generator.user.BuiltInFirstNameDataProvider", supportedLocale);
            Object lastNameProvider = builtInProvider("io.github.frikit.krandom.generator.user.BuiltInLastNameDataProvider", supportedLocale);
            Object genderProvider = builtInProvider("io.github.frikit.krandom.generator.user.BuiltInGenderDataProvider", supportedLocale);
            Object professionProvider = builtInProvider("io.github.frikit.krandom.generator.user.BuiltInProfessionDataProvider", supportedLocale);
            Object titleProvider = builtInProvider("io.github.frikit.krandom.generator.user.BuiltInTitleDataProvider", supportedLocale);
            Object suffixProvider = builtInProvider("io.github.frikit.krandom.generator.user.BuiltInSuffixDataProvider", supportedLocale);

            assertEquals(locale, invoke(firstNameProvider, "getLocale"));
            assertEquals(locale, invoke(lastNameProvider, "getLocale"));
            assertEquals(locale, invoke(genderProvider, "getLocale"));
            assertEquals(locale, invoke(professionProvider, "getLocale"));
            assertEquals(locale, invoke(titleProvider, "getLocale"));
            assertEquals(locale, invoke(suffixProvider, "getLocale"));

            String[] maleFirstNames = stringArray(firstNameProvider, "getMaleFirstNames");
            String[] femaleFirstNames = stringArray(firstNameProvider, "getFemaleFirstNames");
            String[] lastNames = stringArray(lastNameProvider, "getLastNames");
            String[] professions = stringArray(professionProvider, "getProfessions");
            String[] titles = stringArray(titleProvider, "getTitles");
            String[] suffixes = stringArray(suffixProvider, "getSuffixes");

            assertDatasetQuality(supportedLocale + " male first names", maleFirstNames, 40);
            assertDatasetQuality(supportedLocale + " female first names", femaleFirstNames, 40);
            assertDatasetQuality(supportedLocale + " last names", lastNames, 40);
            assertTrue(!stringValue(genderProvider, "getMaleLabel").isBlank());
            assertTrue(!stringValue(genderProvider, "getFemaleLabel").isBlank());
            assertTrue(!stringValue(genderProvider, "getMaleLabel").equals(stringValue(genderProvider, "getFemaleLabel")));
            assertDatasetQuality(supportedLocale + " professions", professions, 25);
            assertEquals(25, intArray(professionProvider, "getWeights").length);
            assertDatasetQuality(supportedLocale + " titles", titles, 4);
            assertDatasetQuality(supportedLocale + " suffixes", suffixes, 3);
            assertExpectedScriptCoverage(supportedLocale,
                                         maleFirstNames,
                                         femaleFirstNames,
                                         lastNames,
                                         professions,
                                         titles,
                                         suffixes);
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

            Object cityProvider = builtInProvider("io.github.frikit.krandom.generator.location.BuiltInCityDataProvider", supportedLocale);
            Object stateProvider = builtInProvider("io.github.frikit.krandom.generator.location.BuiltInStateDataProvider", supportedLocale);
            Object countryProvider = builtInProvider("io.github.frikit.krandom.generator.location.BuiltInCountryDataProvider", supportedLocale);
            Object streetProvider = builtInProvider("io.github.frikit.krandom.generator.location.BuiltInStreetAddressDataProvider", supportedLocale);

            assertEquals(locale, invoke(cityProvider, "getLocale"));
            assertEquals(locale, invoke(stateProvider, "getLocale"));
            assertEquals(locale, invoke(countryProvider, "getLocale"));
            assertEquals(locale, invoke(streetProvider, "getLocale"));

            String[] cities = stringArray(cityProvider, "getCities");
            String[] states = stringArray(stateProvider, "getStates");
            String[] stateAbbreviations = stringArray(stateProvider, "getAbbreviations");
            String[] countries = stringArray(countryProvider, "getCountries");
            String[] streetNames = stringArray(streetProvider, "getStreetNames");
            String[] streetTypesShort = stringArray(streetProvider, "getStreetTypesShort");
            String[] streetTypesLong = stringArray(streetProvider, "getStreetTypesLong");

            assertDatasetQuality(supportedLocale + " cities", cities, 70);
            assertDatasetQuality(supportedLocale + " states", states, 4);
            assertEquals(states.length, stateAbbreviations.length);
            assertOptionalDatasetQuality(supportedLocale + " state abbreviations", stateAbbreviations);
            assertDatasetQuality(supportedLocale + " countries", countries, 150);
            assertDatasetQuality(supportedLocale + " street names", streetNames, 20);
            assertDatasetQuality(supportedLocale + " street types short", streetTypesShort, 10);
            assertDatasetQuality(supportedLocale + " street types long", streetTypesLong, 10);
            assertExpectedScriptCoverage(supportedLocale, cities, states, streetNames, streetTypesShort, streetTypesLong);
        }
    }

    private static void assertDatasetQuality(String label, String[] values, int minimumCount) {
        assertTrue(values.length >= minimumCount, label + " below minimum count");
        long nonBlankCount = Arrays.stream(values).filter(value -> value != null && !value.isBlank()).count();
        assertEquals(values.length, nonBlankCount, label + " should not contain blank values");
        long uniqueCount = Arrays.stream(values)
                                 .map(String::trim)
                                 .distinct()
                                 .count();
        assertTrue(uniqueCount / (double) values.length >= MIN_UNIQUE_RATIO,
                   label + " duplicate ratio too high");
    }

    private static void assertOptionalDatasetQuality(String label, String[] values) {
        long nonBlankCount = Arrays.stream(values).filter(value -> value != null && !value.isBlank()).count();
        if (nonBlankCount == 0) {
            return;
        }
        long uniqueCount = Arrays.stream(values)
                                 .filter(value -> value != null && !value.isBlank())
                                 .map(String::trim)
                                 .distinct()
                                 .count();
        assertTrue(uniqueCount / (double) nonBlankCount >= MIN_UNIQUE_RATIO,
                   label + " duplicate ratio too high");
    }

    private static void assertExpectedScriptCoverage(SupportedLocale supportedLocale, String[]... datasets) {
        Set<Character.UnicodeScript> expectedScripts = expectedScriptsFor(supportedLocale);
        long totalEntries = 0;
        long expectedScriptEntries = 0;
        for (String[] dataset : datasets) {
            for (String value : dataset) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                totalEntries++;
                if (containsExpectedScript(value, expectedScripts)) {
                    expectedScriptEntries++;
                }
            }
        }
        assertTrue(totalEntries > 0, supportedLocale + " has no entries to validate for script coverage");
        assertTrue(expectedScriptEntries / (double) totalEntries >= MIN_EXPECTED_SCRIPT_RATIO,
                   supportedLocale + " script coverage below expected ratio");
    }

    private static boolean containsExpectedScript(String value, Set<Character.UnicodeScript> expectedScripts) {
        return value.codePoints()
                    .filter(Character::isLetter)
                    .mapToObj(Character.UnicodeScript::of)
                    .anyMatch(expectedScripts::contains);
    }

    private static Set<Character.UnicodeScript> expectedScriptsFor(SupportedLocale supportedLocale) {
        return switch (supportedLocale.locale().getLanguage()) {
            case "ar" -> Set.of(Character.UnicodeScript.ARABIC);
            case "hi" -> Set.of(Character.UnicodeScript.DEVANAGARI);
            case "ja" -> Set.of(Character.UnicodeScript.HAN,
                                Character.UnicodeScript.HIRAGANA,
                                Character.UnicodeScript.KATAKANA);
            case "ko" -> Set.of(Character.UnicodeScript.HANGUL);
            case "ru" -> Set.of(Character.UnicodeScript.CYRILLIC);
            case "zh" -> Set.of(Character.UnicodeScript.HAN);
            default -> Set.of(Character.UnicodeScript.LATIN);
        };
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
