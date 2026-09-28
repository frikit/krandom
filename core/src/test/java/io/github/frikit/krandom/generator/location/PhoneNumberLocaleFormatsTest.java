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
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PhoneNumberGenerator country formats and locale resolution")
class PhoneNumberLocaleFormatsTest {

    private static final int SAMPLES = 40;

    private static final String CA_AREA_CODES =
        "(204|226|236|249|250|289|306|343|365|367|403|416|418|431|437|438|450|506|514|519|548|579|581|587"
        + "|604|613|639|647|672|705|709|742|778|780|782|807|819|825|867|873|902|905)";

    private static final Pattern NANP_DIGITS = Pattern.compile("(\\d{3})(\\d{3})(\\d{4})");

    /** Expected national formats per resolved country. */
    private static final Map<String, PhoneFormat> FORMATS = Map.ofEntries(
        // NANP: fictional 555-0100..0199 lines under the default safety policy
        nanp("US", "\\d{3}"),
        nanp("CA", CA_AREA_CODES),
        // Tier-1 formats (unchanged implementations)
        format("GB", "+44", "07\\d{3} \\d{6}", "07\\d{9}",
               "020 \\d{4} \\d{4}|01\\d{2} \\d{3} \\d{4}|01\\d{3} \\d{6}", "0(20\\d{8}|1\\d{9})"),
        format("AU", "+61", "04\\d{2} \\d{3} \\d{3}", "04\\d{8}", "0[2378] \\d{4} \\d{4}", "0[2378]\\d{8}"),
        format("DE", "+49", "01[5-7]\\d \\d{8}", "01[5-7]\\d{9}", "0[2-9]\\d{1,2} \\d{8}", "0[2-9]\\d{9,10}"),
        format("FR", "+33", "0[67]( \\d{2}){4}", "0[67]\\d{8}", "0[1-5]( \\d{2}){4}", "0[1-5]\\d{8}"),
        format("ES", "+34", "[67]\\d{2} \\d{2} \\d{2} \\d{2}", "[67]\\d{8}", "9[1-8] \\d{3} \\d{2} \\d{2}", "9[1-8]\\d{7}"),
        format("IT", "+39", "3\\d{2} \\d{3} \\d{4}", "3\\d{9}", "0\\d{1,2} \\d{4} \\d{4}", "0\\d{9}"),
        format("BR", "+55", "\\(\\d{2}\\) 9\\d{4}-\\d{4}", "\\d{2}9\\d{8}", "\\(\\d{2}\\) [23]\\d{3}-\\d{4}", "\\d{2}[23]\\d{7}"),
        format("JP", "+81", "0[789]0-\\d{4}-\\d{4}", "0[789]0\\d{8}", "0\\d{1,2}-\\d{4}-\\d{4}", "0\\d{9,10}"),
        format("CN", "+86", "1[3-9]\\d \\d{4} \\d{4}", "1[3-9]\\d{9}", "0\\d{2}-\\d{8}", "0\\d{10}"),
        format("NL", "+31", "06\\d \\d{7}", "06\\d{8}", "0\\d{2} \\d{7}", "0\\d{9}"),
        format("PL", "+48", "\\d{3} \\d{3} \\d{3}", "\\d{9}", "\\d{2} \\d{3} \\d{2} \\d{2}", "\\d{9}"),
        format("RU", "+7", "9\\d{2} \\d{3}-\\d{2}-\\d{2}", "9\\d{9}", "\\d{3} \\d{3}-\\d{2}-\\d{2}", "\\d{10}"),
        format("KR", "+82", "01\\d-\\d{4}-\\d{4}", "01\\d{9}", "0\\d{1,2}-\\d{4}-\\d{4}", "0\\d{9,10}"),
        format("TR", "+90", "05\\d{2} \\d{3} \\d{2} \\d{2}", "05\\d{9}", "0[23]\\d{2} \\d{3} \\d{2} \\d{2}", "0[23]\\d{9}"),
        format("SE", "+46", "07\\d-\\d{3} \\d{2} \\d{2}", "07\\d{8}", "0\\d{1,2}-\\d{3} \\d{2} \\d{2}", "0\\d{8,9}"),
        format("NO", "+47", "[49]\\d{2} \\d{2} \\d{3}", "[49]\\d{7}", "[2-7]\\d \\d{2} \\d{2} \\d{2}", "[2-7]\\d{7}"),
        format("CZ", "+420", "[67]\\d{2} \\d{3} \\d{3}", "[67]\\d{8}", "[2-5]\\d{2} \\d{3} \\d{3}", "[2-5]\\d{8}"),
        format("SA", "+966", "05\\d \\d{3} \\d{4}", "05\\d{8}", "01\\d \\d{3} \\d{4}", "01\\d{8}"),
        format("IN", "+91", "[6-9]\\d{4} \\d{5}", "[6-9]\\d{9}", "0\\d{2,3} \\d{8}", "0\\d{10,11}"),
        // Built-in formats for the remaining SupportedLocale countries
        format("AR", "+54",
               "0(11 15-\\d{4}|(221|261|341|351|381) 15-\\d{3})-\\d{4}", "0(1115\\d{8}|(221|261|341|351|381)15\\d{7})",
               "0(11 4\\d{3}|(221|261|341|351|381) 4\\d{2})-\\d{4}", "0(114\\d{7}|(221|261|341|351|381)4\\d{6})"),
        format("AT", "+43",
               "06(50|60|64|76|80|81|88|99) \\d{3} \\d{4}", "06(50|60|64|76|80|81|88|99)\\d{7}",
               "01 \\d{3} \\d{4}|0(316|463|512|662|732) \\d{6}", "01\\d{7}|0(316|463|512|662|732)\\d{6}"),
        format("BE", "+32",
               "04(6[05-8]|7\\d|8[3-9]|9\\d) \\d{2} \\d{2} \\d{2}", "04(6[05-8]|7\\d|8[3-9]|9\\d)\\d{6}",
               "0[2349] \\d{3} \\d{2} \\d{2}|0(1[0-69]|5\\d|6[0-79]|7\\d|8[0-79]) \\d{2} \\d{2} \\d{2}", "0[1-9]\\d{7}"),
        format("BG", "+359",
               "0(87|88|89|98)\\d \\d{3} \\d{3}", "0(87|88|89|98)\\d{7}",
               "02 \\d{3} \\d{4}|0(32|42|44|52|56|58|64|82) \\d{3} \\d{3}", "02\\d{7}|0(32|42|44|52|56|58|64|82)\\d{6}"),
        format("CH", "+41",
               "07[5-9] \\d{3} \\d{2} \\d{2}", "07[5-9]\\d{7}",
               "0(2[1-7]|3[1-4]|4[134]|5[256]|6[12]|71|81|91) \\d{3} \\d{2} \\d{2}",
               "0(2[1-7]|3[1-4]|4[134]|5[256]|6[12]|71|81|91)\\d{7}"),
        format("DK", "+45",
               "(2\\d|3[01]|4[0-2]|5[0-3]|6[01]|71|81|9[1-3])( \\d{2}){3}",
               "(2\\d|3[01]|4[0-2]|5[0-3]|6[01]|71|81|9[1-3])\\d{6}",
               "(3[235689]|4[3-689]|5[4-9]|6[2-69]|7[2-6]|8[679]|9[6-9])( \\d{2}){3}",
               "(3[235689]|4[3-689]|5[4-9]|6[2-69]|7[2-6]|8[679]|9[6-9])\\d{6}"),
        format("FI", "+358",
               "0(40|41|42|44|45|46|50) \\d{3} \\d{4}", "0(40|41|42|44|45|46|50)\\d{7}",
               "0[235689] \\d{3} \\d{4}|01[3-9] \\d{3} \\d{3}", "0[235689]\\d{7}|01[3-9]\\d{6}"),
        format("GR", "+30",
               "69[0345789] \\d{3} \\d{4}", "69[0345789]\\d{7}",
               "210 \\d{3} \\d{4}|2(310|410|610|651|810) \\d{3} \\d{3}", "210\\d{7}|2(310|410|610|651|810)\\d{6}"),
        format("HR", "+385",
               "09[125789] \\d{3} \\d{4}", "09[125789]\\d{7}",
               "01 \\d{3} \\d{4}|0(2[013]|3[15]|4[247]|5[12]) \\d{3} \\d{3}", "01\\d{7}|0(2[013]|3[15]|4[247]|5[12])\\d{6}"),
        format("HU", "+36",
               "06 (20|30|31|50|70) \\d{3} \\d{4}", "06(20|30|31|50|70)\\d{7}",
               "06 1 \\d{3} \\d{4}|06 (2[2468]|3[246]|4[26]|52|62|7[246]|8[28]|9[2469]) \\d{3} \\d{3}",
               "061\\d{7}|06(2[2468]|3[246]|4[26]|52|62|7[246]|8[28]|9[2469])\\d{6}"),
        format("ID", "+62",
               "08(1[1-35-9]|2[12]|3[18]|5[235-9]|7[78]|8[1-3]|9[5-9])-\\d{4}-\\d{4}",
               "08(1[1-35-9]|2[12]|3[18]|5[235-9]|7[78]|8[1-3]|9[5-9])\\d{8}",
               "\\(021\\) \\d{4}-\\d{4}|\\(0(22|24|31|61)\\) \\d{3}-\\d{4}", "021\\d{8}|0(22|24|31|61)\\d{7}"),
        format("IE", "+353",
               "08[35679] \\d{3} \\d{4}", "08[35679]\\d{7}",
               "0(1|21) \\d{3} \\d{4}|0(51|61|91) \\d{3} \\d{3}", "0(1|21)\\d{7}|0(51|61|91)\\d{6}"),
        format("IL", "+972", "05[023458]-\\d{3}-\\d{4}", "05[023458]\\d{7}", "0[23489]-\\d{3}-\\d{4}", "0[23489]\\d{7}"),
        format("MX", "+52",
               "(33|55|81) \\d{4} \\d{4}|(222|442|477|614|664|686|998|999) \\d{3} \\d{4}",
               "(33|55|81)\\d{8}|(222|442|477|614|664|686|998|999)\\d{7}",
               "(33|55|81) \\d{4} \\d{4}|(222|442|477|614|664|686|998|999) \\d{3} \\d{4}",
               "(33|55|81)\\d{8}|(222|442|477|614|664|686|998|999)\\d{7}"),
        format("MY", "+60",
               "01[02346-9]-\\d{3} \\d{4}|011-\\d{4} \\d{4}", "01[02346-9]\\d{7}|011\\d{8}",
               "03-\\d{4} \\d{4}|0[4-79]-\\d{3} \\d{4}|08[28]-\\d{3} \\d{3}", "03\\d{8}|0[4-79]\\d{7}|08[28]\\d{6}"),
        format("NZ", "+64", "02[0-2789] \\d{3} \\d{4}", "02[0-2789]\\d{7}", "0[34679] \\d{3} \\d{4}", "0[34679]\\d{7}"),
        format("PT", "+351", "9[1236]\\d \\d{3} \\d{3}", "9[1236]\\d{7}", "2\\d{2} \\d{3} \\d{3}", "2\\d{8}"),
        format("RO", "+40",
               "07[2-8]\\d \\d{3} \\d{3}", "07[2-8]\\d{7}",
               "0[23]1 \\d{3} \\d{4}|02[3-6]\\d \\d{3} \\d{3}", "0[23]1\\d{7}|02[3-6]\\d{7}"),
        format("SK", "+421",
               "09[0-5]\\d \\d{3} \\d{3}", "09[0-5]\\d{7}",
               "02 \\d{4} \\d{4}|0[345][1-8] \\d{3} \\d{4}", "02\\d{8}|0[345][1-8]\\d{7}"),
        format("TH", "+66",
               "0[689]\\d \\d{3} \\d{4}", "0[689]\\d{8}",
               "02 \\d{3} \\d{4}|0[3-7]\\d \\d{3} \\d{3}", "02\\d{7}|0[3-7]\\d{7}"),
        format("TW", "+886",
               "09\\d{2}-\\d{3}-\\d{3}", "09\\d{8}",
               "\\(0[24]\\) \\d{4}-\\d{4}|\\(0[35-8]\\) \\d{3}-\\d{4}", "0[24]\\d{8}|0[35-8]\\d{7}"),
        format("UA", "+380",
               "0(50|63|6[678]|73|9[35-9]) \\d{3} \\d{2} \\d{2}", "0(50|63|6[678]|73|9[35-9])\\d{7}",
               "0(32|44|48|5[67]|61) \\d{3} \\d{2} \\d{2}", "0(32|44|48|5[67]|61)\\d{7}"),
        format("VN", "+84",
               "0[35789]\\d{2} \\d{3} \\d{3}", "0[35789]\\d{8}",
               "02[48] \\d{4} \\d{4}|02\\d{2} \\d{3} \\d{4}", "02\\d{9}"),
        format("ZA", "+27",
               "0(6[0-8]|7[1-46-9]|8[1-4]) \\d{3} \\d{4}", "0(6[0-8]|7[1-46-9]|8[1-4])\\d{7}",
               "0(1[0-2]|21|31|4[13]|5[13]) \\d{3} \\d{4}", "0(1[0-2]|21|31|4[13]|5[13])\\d{7}")
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

    // ── Built-in country formats ───────────────────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    @DisplayName("every supported locale uses its country's national phone format")
    void everySupportedLocaleUsesItsCountryFormat(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        PhoneFormat expected = FORMATS.get(locale.getCountry());
        assertNotNull(expected, "missing expected phone format for " + locale);

        assertGeneratesFormat(expected, new PhoneNumberGenerator(seeded(locale, 11L)), locale.toString());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    @DisplayName("every supported locale reports its own calling code and MSISDN prefix")
    void everySupportedLocaleUsesItsCallingCode(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        String callingCode = FORMATS.get(locale.getCountry()).callingCode();
        PhoneNumberGenerator generator = new PhoneNumberGenerator(seeded(locale, 5L));

        assertEquals(callingCode, generator.generateCountryCallingCode(), locale.toString());
        for (int i = 0; i < 10; i++) {
            String msisdn = generator.generateMsisdn();
            assertTrue(msisdn.matches("\\d{14,15}"), locale + " MSISDN: " + msisdn);
            assertTrue(msisdn.startsWith(callingCode.substring(1)), locale + " MSISDN: " + msisdn);
        }
    }

    // ── NANP ───────────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "en_US", "en_CA", "fr_CA", "en" })
    @DisplayName("NANP locales use NANPA's fictional 555-0100..0199 range by default")
    void nanpLocalesUseFictionalRangeByDefault(String localeKey) {
        Locale locale = locale(localeKey);
        boolean canadian = "CA".equals(locale.getCountry());
        PhoneNumberGenerator generator = new PhoneNumberGenerator(seeded(locale, 3L));

        for (int i = 0; i < 200; i++) {
            String phone = generator.generate(false, i % 2 == 0);
            Matcher matcher = NANP_DIGITS.matcher(phone);
            assertTrue(matcher.matches(), localeKey + ": " + phone);
            assertEquals("555", matcher.group(2), localeKey + ": " + phone);
            assertTrue(matcher.group(3).matches("01\\d{2}"), localeKey + ": " + phone);
            assertEquals(canadian, matcher.group(1).matches(CA_AREA_CODES), localeKey + " area code: " + phone);

            String formatted = generator.generate(true, i % 2 == 0);
            assertTrue(formatted.matches("\\(\\d{3}\\) 555-01\\d{2}|\\d{3}-555-01\\d{2}"), localeKey + ": " + formatted);
        }
        assertEquals("+1", generator.generateCountryCallingCode());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "en_CA", "fr_CA" })
    @DisplayName("Canadian locales use realistic exchanges only under the realistic policy")
    void canadianLocalesUseRealisticExchangesOnlyWhenRequested(String localeKey) {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(locale(localeKey))
                                                .seed(17L)
                                                .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        PhoneNumberGenerator generator = new PhoneNumberGenerator(config);

        Set<String> exchanges = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            Matcher matcher = NANP_DIGITS.matcher(generator.generate(false));
            assertTrue(matcher.matches());
            assertTrue(matcher.group(1).matches(CA_AREA_CODES), "Canadian area code expected: " + matcher.group(1));
            int exchange = Integer.parseInt(matcher.group(2));
            assertTrue(exchange >= 200 && exchange <= 999, "exchange out of range: " + exchange);
            exchanges.add(matcher.group(2));
        }
        assertTrue(exchanges.size() > 50, "realistic exchanges expected, got " + exchanges);
    }

    // ── Locale resolution ──────────────────────────────────────────────────────

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
    @DisplayName("language-only locales use the primary catalog country's format")
    void languageOnlyLocalesUsePrimaryCatalogCountry(String language) {
        String country = PRIMARY_COUNTRY_BY_LANGUAGE.get(language);
        PhoneNumberGenerator languageOnly = new PhoneNumberGenerator(seeded(Locale.of(language), 23L));

        assertGeneratesFormat(FORMATS.get(country), languageOnly, language);
        assertEquals(FORMATS.get(country).callingCode(), languageOnly.generateCountryCallingCode(), language);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("languages")
    @DisplayName("language-only locales produce the same seeded output as their primary catalog locale")
    void languageOnlyLocalesMatchPrimaryCatalogLocaleOutput(String language) {
        Locale primary = primaryCatalogLocale(language);

        assertEquals(new PhoneNumberGenerator(seeded(primary, 29L)).generateList(30),
                     new PhoneNumberGenerator(seeded(Locale.of(language), 29L)).generateList(30),
                     language + " should resolve to " + primary);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("rootAndUnknownLanguages")
    @DisplayName("locales without a country and without a catalog language use the en_US default")
    void rootAndUnknownLanguagesResolveToUnitedStates(Locale locale) {
        PhoneNumberGenerator generator = new PhoneNumberGenerator(seeded(locale, 31L));

        assertGeneratesFormat(FORMATS.get("US"), generator, "'" + locale + "'");
        assertEquals("+1", generator.generateCountryCallingCode());
        assertEquals(new PhoneNumberGenerator(seeded(Locale.US, 37L)).generateList(30),
                     new PhoneNumberGenerator(seeded(locale, 37L)).generateList(30));
    }

    @Test
    @DisplayName("the language does not change a country's format")
    void languageDoesNotChangeTheCountryFormat() {
        assertEquals(new PhoneNumberGenerator(seeded(Locale.GERMANY, 41L)).generateList(30),
                     new PhoneNumberGenerator(seeded(Locale.of("en", "DE"), 41L)).generateList(30));
        assertEquals(new PhoneNumberGenerator(seeded(Locale.of("es", "ES"), 41L)).generateList(30),
                     new PhoneNumberGenerator(seeded(Locale.of("ca", "ES"), 41L)).generateList(30));
        assertEquals(new PhoneNumberGenerator(seeded(Locale.of("hi", "IN"), 41L)).generateList(30),
                     new PhoneNumberGenerator(seeded(Locale.of("en", "IN"), 41L)).generateList(30));
        assertEquals(new PhoneNumberGenerator(seeded(Locale.of("fr", "CH"), 41L)).generateList(30),
                     new PhoneNumberGenerator(seeded(Locale.of("de", "CH"), 41L)).generateList(30));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({ "de_LI, de_DE", "en_JM, en_US", "xx_YY, en_US", "es_419, es_ES" })
    @DisplayName("a country without a built-in format falls back to its language's catalog country, then en_US")
    void unsupportedCountriesUseTheLanguageFallback(String localeKey, String expectedKey) {
        Locale locale = locale(localeKey);
        Locale expected = locale(expectedKey);

        assertEquals(new PhoneNumberGenerator(seeded(expected, 43L)).generateList(30),
                     new PhoneNumberGenerator(seeded(locale, 43L)).generateList(30));
        assertEquals(FORMATS.get(expected.getCountry()).callingCode(),
                     new PhoneNumberGenerator(locale).generateCountryCallingCode());
        assertEquals(FORMATS.get(expected.getCountry()).callingCode(),
                     Generators.ofPhoneNumber(locale).generateCountryCallingCode());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

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

    private static void assertGeneratesFormat(PhoneFormat expected, PhoneNumberGenerator generator, String label) {
        for (int i = 0; i < SAMPLES; i++) {
            assertMatches(expected.formattedMobile(), generator.generate(true, true), label + " formatted mobile");
            assertMatches(expected.formattedLandline(), generator.generate(true, false), label + " formatted landline");
            assertMatches(expected.unformattedMobile(), generator.generate(false, true), label + " unformatted mobile");
            assertMatches(expected.unformattedLandline(), generator.generate(false, false), label + " unformatted landline");
        }
    }

    private static void assertMatches(Pattern pattern, String value, String label) {
        assertTrue(pattern.matcher(value).matches(), label + " expected /" + pattern.pattern() + "/ but got: " + value);
    }

    private static GeneratorConfig seeded(Locale locale, long seed) {
        return GeneratorConfig.builder().locale(locale).seed(seed).build();
    }

    private static Locale locale(String key) {
        String[] parts = key.split("_");
        return parts.length == 1 ? Locale.of(parts[0]) : Locale.of(parts[0], parts[1]);
    }

    private static Map.Entry<String, PhoneFormat> nanp(String country, String areaCodes) {
        String formatted = "\\(" + areaCodes + "\\) 555-01\\d{2}|" + areaCodes + "-555-01\\d{2}";
        String unformatted = areaCodes + "55501\\d{2}";
        return format(country, "+1", formatted, unformatted, formatted, unformatted);
    }

    private static Map.Entry<String, PhoneFormat> format(String country,
                                                         String callingCode,
                                                         String formattedMobile,
                                                         String unformattedMobile,
                                                         String formattedLandline,
                                                         String unformattedLandline) {
        return Map.entry(country, new PhoneFormat(callingCode,
                                                  Pattern.compile(formattedMobile),
                                                  Pattern.compile(unformattedMobile),
                                                  Pattern.compile(formattedLandline),
                                                  Pattern.compile(unformattedLandline)));
    }

    private record PhoneFormat(String callingCode,
                               Pattern formattedMobile,
                               Pattern unformattedMobile,
                               Pattern formattedLandline,
                               Pattern unformattedLandline) {
    }
}
