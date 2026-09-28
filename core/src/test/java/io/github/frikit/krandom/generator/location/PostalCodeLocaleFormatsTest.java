/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PostalCodeGenerator country formats and locale resolution")
class PostalCodeLocaleFormatsTest {

    private static final int SAMPLES = 60;

    private static final String FOUR_DIGITS_NO_LEADING_ZERO = "[1-9]\\d{3}";

    /** Expected basic and {@code extended} postal formats per resolved country. */
    private static final Map<String, PostalFormat> FORMATS = Map.ofEntries(
        // Tier-1 formats (unchanged implementations)
        format("US", "\\d{5}", "\\d{5}-\\d{4}"),
        format("GB", "[A-Z]{1,2}(\\d{1,2}|\\d[A-Z]) \\d[A-Z]{2}"),
        format("AU", "\\d{4}"),
        format("DE", "\\d{5}"),
        format("FR", "\\d{5}"),
        format("ES", "\\d{5}"),
        format("IT", "\\d{5}"),
        format("BR", "\\d{8}", "\\d{5}-\\d{3}"),
        format("JP", "\\d{7}", "\\d{3}-\\d{4}"),
        format("CN", "\\d{6}"),
        format("NL", "\\d{4} [A-Z]{2}"),
        format("PL", "\\d{2}-\\d{3}"),
        format("RU", "\\d{6}"),
        format("KR", "\\d{5}"),
        format("TR", "\\d{5}"),
        format("SE", "\\d{3} \\d{2}"),
        format("NO", "\\d{4}"),
        format("CZ", "\\d{3} \\d{2}"),
        format("SA", "\\d{5}"),
        format("IN", "[1-9]\\d{5}"),
        // Built-in formats for the remaining SupportedLocale countries
        format("AR", FOUR_DIGITS_NO_LEADING_ZERO, "[A-HJ-NP-Z][1-9]\\d{3}[A-HJ-NP-Z]{3}"),
        format("AT", FOUR_DIGITS_NO_LEADING_ZERO),
        format("BE", FOUR_DIGITS_NO_LEADING_ZERO),
        format("BG", FOUR_DIGITS_NO_LEADING_ZERO),
        format("CA", "[ABCEGHJ-NPRSTVXY]\\d[ABCEGHJ-NPRSTV-Z] \\d[ABCEGHJ-NPRSTV-Z]\\d"),
        format("CH", FOUR_DIGITS_NO_LEADING_ZERO),
        format("DK", FOUR_DIGITS_NO_LEADING_ZERO),
        format("FI", "\\d{5}"),
        format("GR", "[1-8]\\d{2} \\d{2}"),
        format("HR", "(10|2[0-3]|3[1-5]|4[0-9]|5[1-3])\\d{3}"),
        format("HU", FOUR_DIGITS_NO_LEADING_ZERO),
        format("ID", "[1-9]\\d{4}"),
        format("IE", "(D6W|[AC-FHKNPRTV-Y]\\d{2}) [0-9AC-FHKNPRTV-Y]{4}"),
        format("IL", "[1-9]\\d{6}"),
        format("MX", "\\d{5}"),
        format("MY", "\\d{5}"),
        format("NZ", "\\d{4}"),
        format("PT", "[1-9]\\d{3}-\\d{3}"),
        format("RO", "\\d{6}"),
        format("SK", "[089]\\d{2} \\d{2}"),
        format("TH", "[1-9]\\d{4}"),
        format("TW", "[1-9]\\d{2}", "[1-9]\\d{4}"),
        format("UA", "\\d{5}"),
        format("VN", "[1-9]\\d{4}"),
        format("ZA", "\\d{4}")
    );

    /** Primary catalog country per language: the first SupportedLocale constant with that language. */
    private static final Map<String, String> PRIMARY_COUNTRY_BY_LANGUAGE = Map.ofEntries(
        Map.entry("en", "US"), Map.entry("fr", "FR"), Map.entry("de", "DE"), Map.entry("ja", "JP"),
        Map.entry("es", "ES"), Map.entry("it", "IT"), Map.entry("pt", "BR"), Map.entry("zh", "CN"),
        Map.entry("nl", "NL"), Map.entry("pl", "PL"), Map.entry("ru", "RU"), Map.entry("ko", "KR"),
        Map.entry("tr", "TR"), Map.entry("sv", "SE"), Map.entry("nb", "NO"), Map.entry("no", "NO"),
        Map.entry("cs", "CZ"), Map.entry("ar", "SA"), Map.entry("hi", "IN"), Map.entry("da", "DK"),
        Map.entry("fi", "FI"), Map.entry("hu", "HU"), Map.entry("ro", "RO"), Map.entry("sk", "SK"),
        Map.entry("uk", "UA"), Map.entry("bg", "BG"), Map.entry("hr", "HR"), Map.entry("el", "GR"),
        Map.entry("th", "TH"), Map.entry("vi", "VN"), Map.entry("id", "ID"), Map.entry("ms", "MY"),
        Map.entry("he", "IL"), Map.entry("ca", "ES")
    );

    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    @DisplayName("every supported locale uses its country's postal format, basic and extended")
    void everySupportedLocaleUsesItsCountryFormat(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        PostalFormat expected = FORMATS.get(locale.getCountry());
        assertNotNull(expected, "missing expected postal format for " + locale);

        assertGeneratesFormat(expected, new PostalCodeGenerator(seeded(locale, 13L)), locale.toString());
    }

    @Test
    @DisplayName("every catalog language has an expected primary country")
    void everyCatalogLanguageHasAPrimaryCountryExpectation() {
        Map<String, String> firstCountryByLanguage = new LinkedHashMap<>();
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            firstCountryByLanguage.putIfAbsent(supportedLocale.locale().getLanguage(), supportedLocale.locale().getCountry());
        }
        firstCountryByLanguage.forEach((language, country) ->
            assertEquals(country, PRIMARY_COUNTRY_BY_LANGUAGE.get(language), "primary country for " + language));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("languages")
    @DisplayName("language-only locales use the primary catalog country's postal format")
    void languageOnlyLocalesUsePrimaryCatalogCountry(String language) {
        String country = PRIMARY_COUNTRY_BY_LANGUAGE.get(language);

        assertGeneratesFormat(FORMATS.get(country), new PostalCodeGenerator(seeded(Locale.of(language), 19L)), language);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("languages")
    @DisplayName("language-only locales produce the same seeded postal codes as their primary catalog locale")
    void languageOnlyLocalesMatchPrimaryCatalogLocaleOutput(String language) {
        Locale primary = primaryCatalogLocale(language);

        assertEquals(new PostalCodeGenerator(seeded(primary, 29L)).generateList(30),
                     new PostalCodeGenerator(seeded(Locale.of(language), 29L)).generateList(30),
                     language + " should resolve to " + primary);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("rootAndUnknownLanguages")
    @DisplayName("locales without a country and without a catalog language use the en_US default")
    void rootAndUnknownLanguagesResolveToUnitedStates(Locale locale) {
        assertGeneratesFormat(FORMATS.get("US"), new PostalCodeGenerator(seeded(locale, 31L)), "'" + locale + "'");
        assertEquals(new PostalCodeGenerator(seeded(Locale.US, 37L)).generateList(30),
                     new PostalCodeGenerator(seeded(locale, 37L)).generateList(30));
    }

    @Test
    @DisplayName("the language does not change a country's postal format")
    void languageDoesNotChangeTheCountryFormat() {
        assertEquals(new PostalCodeGenerator(seeded(Locale.GERMANY, 41L)).generateList(30),
                     new PostalCodeGenerator(seeded(Locale.of("en", "DE"), 41L)).generateList(30));
        assertEquals(new PostalCodeGenerator(seeded(Locale.of("es", "ES"), 41L)).generateList(30),
                     new PostalCodeGenerator(seeded(Locale.of("ca", "ES"), 41L)).generateList(30));
        assertEquals(new PostalCodeGenerator(seeded(Locale.of("en", "CA"), 41L)).generateList(30),
                     new PostalCodeGenerator(seeded(Locale.of("fr", "CA"), 41L)).generateList(30));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({ "de_LI, de_DE", "en_JM, en_US", "xx_YY, en_US", "es_419, es_ES" })
    @DisplayName("a country without a built-in postal format falls back to its language's catalog country, then en_US")
    void unsupportedCountriesUseTheLanguageFallback(String localeKey, String expectedKey) {
        String[] parts = localeKey.split("_");
        Locale locale = Locale.of(parts[0], parts[1]);
        String[] expectedParts = expectedKey.split("_");
        Locale expected = Locale.of(expectedParts[0], expectedParts[1]);

        assertEquals(new PostalCodeGenerator(seeded(expected, 43L)).generateList(30),
                     new PostalCodeGenerator(seeded(locale, 43L)).generateList(30));
        assertEquals(new PostalCodeGenerator(seeded(expected, 47L)).generate(true),
                     Generators.ofPostalCode(seeded(locale, 47L)).generate(true));
        assertNotNull(new PostalCodeGenerator(locale).generate());
    }

    static Stream<String> languages() {
        return PRIMARY_COUNTRY_BY_LANGUAGE.keySet().stream().sorted();
    }

    static Stream<Locale> rootAndUnknownLanguages() {
        return Stream.of(Locale.ROOT, Locale.of("zz"), new Locale.Builder().setScript("Latn").build());
    }

    private static Locale primaryCatalogLocale(String language) {
        String lookupLanguage = "no".equals(language) ? "nb" : language;
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            if (supportedLocale.locale().getLanguage().equals(lookupLanguage)) {
                return supportedLocale.locale();
            }
        }
        throw new IllegalArgumentException("No catalog locale for " + language);
    }

    private static void assertGeneratesFormat(PostalFormat expected, PostalCodeGenerator generator, String label) {
        for (int i = 0; i < SAMPLES; i++) {
            assertMatches(expected.basic(), generator.generate(), label + " basic");
            assertMatches(expected.basic(), generator.generate(false), label + " basic");
            assertMatches(expected.extended(), generator.generate(true), label + " extended");
        }
    }

    private static void assertMatches(Pattern pattern, String value, String label) {
        assertTrue(pattern.matcher(value).matches(), label + " expected /" + pattern.pattern() + "/ but got: " + value);
    }

    private static GeneratorConfig seeded(Locale locale, long seed) {
        return GeneratorConfig.builder().locale(locale).seed(seed).build();
    }

    private static Map.Entry<String, PostalFormat> format(String country, String basicAndExtended) {
        return format(country, basicAndExtended, basicAndExtended);
    }

    private static Map.Entry<String, PostalFormat> format(String country, String basic, String extended) {
        return Map.entry(country, new PostalFormat(Pattern.compile(basic), Pattern.compile(extended)));
    }

    private record PostalFormat(Pattern basic, Pattern extended) {
    }
}
