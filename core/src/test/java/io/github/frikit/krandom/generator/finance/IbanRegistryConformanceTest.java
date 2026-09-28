/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.apache.commons.validator.routines.IBANValidator;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.apache.commons.validator.routines.checkdigit.ModulusTenCheckDigit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks generated IBANs and BBANs against an independent copy of the ISO 13616 IBAN registry
 * (BBAN structure, IBAN length, published sample IBAN) and independent national check-digit
 * oracles. The published samples validate the oracles themselves.
 */
@DisplayName("IBAN and BBAN generators follow the ISO 13616 IBAN registry")
class IbanRegistryConformanceTest {

    private static final int    SEEDS_PER_LOCALE = 1_000;
    private static final String DEFAULT_COUNTRY  = "DE";

    private static final IBANValidator IBAN_VALIDATOR = IBANValidator.getInstance();

    /** ISO 13616 IBAN registry: BBAN structure, IBAN length, and the registry sample IBAN. */
    private static final Map<String, RegistryEntry> REGISTRY = Map.ofEntries(
        entry("AT", new RegistryEntry("5!n11!n", 20, "AT611904300234573201")),
        entry("BE", new RegistryEntry("3!n7!n2!n", 16, "BE68539007547034")),
        entry("BG", new RegistryEntry("4!a4!n2!n8!c", 22, "BG80BNBG96611020345678")),
        entry("BR", new RegistryEntry("8!n5!n10!n1!a1!c", 29, "BR1800360305000010009795493C1")),
        entry("CH", new RegistryEntry("5!n12!c", 21, "CH9300762011623852957")),
        entry("CZ", new RegistryEntry("4!n6!n10!n", 24, "CZ6508000000192000145399")),
        entry("DE", new RegistryEntry("8!n10!n", 22, "DE89370400440532013000")),
        entry("DK", new RegistryEntry("4!n9!n1!n", 18, "DK5000400440116243")),
        entry("ES", new RegistryEntry("4!n4!n1!n1!n10!n", 24, "ES9121000418450200051332")),
        entry("FI", new RegistryEntry("3!n11!n", 18, "FI2112345600000785")),
        entry("FR", new RegistryEntry("5!n5!n11!c2!n", 27, "FR1420041010050500013M02606")),
        entry("GB", new RegistryEntry("4!a6!n8!n", 22, "GB29NWBK60161331926819")),
        entry("GR", new RegistryEntry("3!n4!n16!c", 27, "GR1601101250000000012300695")),
        entry("HR", new RegistryEntry("7!n10!n", 21, "HR1210010051863000160")),
        entry("HU", new RegistryEntry("3!n4!n1!n15!n1!n", 28, "HU42117730161111101800000000")),
        entry("IE", new RegistryEntry("4!a6!n8!n", 22, "IE29AIBK93115212345678")),
        entry("IL", new RegistryEntry("3!n3!n13!n", 23, "IL620108000000099999999")),
        entry("IT", new RegistryEntry("1!a5!n5!n12!c", 27, "IT60X0542811101000000123456")),
        entry("NL", new RegistryEntry("4!a10!n", 18, "NL91ABNA0417164300")),
        entry("NO", new RegistryEntry("4!n6!n1!n", 15, "NO9386011117947")),
        entry("PL", new RegistryEntry("8!n16!n", 28, "PL61109010140000071219812874")),
        entry("PT", new RegistryEntry("4!n4!n11!n2!n", 25, "PT50000201231234567890154")),
        entry("RO", new RegistryEntry("4!a16!c", 24, "RO49AAAA1B31007593840000")),
        entry("RU", new RegistryEntry("9!n5!n15!c", 33, "RU0304452522540817810538091310419")),
        entry("SA", new RegistryEntry("2!n18!c", 24, "SA0380000000608010167519")),
        entry("SE", new RegistryEntry("3!n16!n1!n", 24, "SE4550000000058398257466")),
        entry("SK", new RegistryEntry("4!n6!n10!n", 24, "SK3112000000198742637541")),
        entry("TR", new RegistryEntry("5!n1!n16!c", 26, "TR330006100519786457841326")),
        entry("UA", new RegistryEntry("6!n19!c", 29, "UA213223130000026007233566001"))
    );

    /** Further published IBANs for the countries whose BBAN carries national check digits. */
    private static final List<String> ADDITIONAL_SAMPLES = List.of(
        "ES7921000813610123456789",
        "FR7630006000011234567890189",
        "NO8330001234567",
        "HU93116000060000000012345676",
        "CZ5508000000001234567899"
    );

    /** National check-digit rules that hold for every account of the country, keyed by country. */
    private static final Map<String, Predicate<String>> NATIONAL_CHECKS = Map.ofEntries(
        entry("BE", IbanRegistryConformanceTest::belgianCheckDigitsValid),
        entry("CZ", IbanRegistryConformanceTest::czechSlovakCheckDigitsValid),
        entry("ES", IbanRegistryConformanceTest::spanishCheckDigitsValid),
        entry("FI", bban -> LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(bban)),
        entry("FR", IbanRegistryConformanceTest::frenchRibKeyValid),
        entry("HR", IbanRegistryConformanceTest::croatianCheckDigitsValid),
        entry("HU", IbanRegistryConformanceTest::hungarianCheckDigitsValid),
        entry("IT", IbanRegistryConformanceTest::italianCinValid),
        entry("NO", IbanRegistryConformanceTest::norwegianCheckDigitValid),
        entry("PL", IbanRegistryConformanceTest::polishSortCodeCheckDigitValid),
        entry("PT", IbanRegistryConformanceTest::portugueseNibCheckDigitsValid),
        entry("SK", IbanRegistryConformanceTest::czechSlovakCheckDigitsValid),
        entry("TR", bban -> bban.charAt(5) == '0')
    );

    private record RegistryEntry(String bbanStructure, int ibanLength, String sampleIban) {

        Pattern bbanPattern() {
            Matcher segment = Pattern.compile("(\\d+)!([nac])").matcher(bbanStructure);
            StringBuilder regex = new StringBuilder();
            int end = 0;
            while (segment.find()) {
                assertEquals(end, segment.start(), "unparsed registry notation in " + bbanStructure);
                String characterClass = switch (segment.group(2)) {
                    case "n" -> "[0-9]";
                    case "a" -> "[A-Z]";
                    default -> "[0-9A-Z]";
                };
                regex.append(characterClass).append('{').append(segment.group(1)).append('}');
                end = segment.end();
            }
            assertEquals(bbanStructure.length(), end, "unparsed registry notation in " + bbanStructure);
            return Pattern.compile(regex.toString());
        }
    }

    // ── Oracle self-checks against published IBANs ────────────────────────────

    @Test
    @DisplayName("published registry samples satisfy the test registry and national oracles")
    void publishedSamplesSatisfyRegistryAndNationalOracles() {
        REGISTRY.forEach((country, registry) -> assertConformingIban(country, registry.sampleIban()));
        ADDITIONAL_SAMPLES.forEach(sample -> assertConformingIban(sample.substring(0, 2), sample));
    }

    @Test
    @DisplayName("published sample BBANs match their registry structure")
    void publishedSampleBbansMatchRegistryStructure() {
        assertTrue(REGISTRY.get("DE").bbanPattern().matcher("370400440532013000").matches());
        assertTrue(REGISTRY.get("GB").bbanPattern().matcher("NWBK60161331926819").matches());
        assertTrue(REGISTRY.get("FR").bbanPattern().matcher("20041010050500013M02606").matches());
        assertTrue(REGISTRY.get("IT").bbanPattern().matcher("X0542811101000000123456").matches());
        assertTrue(REGISTRY.get("BR").bbanPattern().matcher("00360305000010009795493C1").matches());
        assertTrue(REGISTRY.get("NL").bbanPattern().matcher("ABNA0417164300").matches());
        // The structure check rejects a GB BBAN without the four-letter bank code.
        assertFalse(REGISTRY.get("GB").bbanPattern().matcher("123460161331926819").matches());
    }

    @Test
    @DisplayName("every supported locale country with an IBAN format is covered by the registry table")
    void registryTableCoversEverySupportedIbanCountry() {
        Set<String> expected = new TreeSet<>();
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String country = supportedLocale.locale().getCountry();
            if (IBAN_VALIDATOR.getValidator(country) != null) {
                expected.add(country);
            }
        }
        assertEquals(expected, new TreeSet<>(REGISTRY.keySet()));
    }

    // ── Generated values ──────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    @DisplayName("generated IBANs are valid for the resolved country of every supported locale")
    void generatedIbansAreValid(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        String country = expectedCountry(locale);
        for (long seed = 0; seed < SEEDS_PER_LOCALE; seed++) {
            String iban = new IbanGenerator(realisticConfig(seed, locale)).generate();
            assertEquals(country, iban.substring(0, 2), () -> locale + " emitted " + iban);
            assertConformingIban(country, iban);
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(SupportedLocale.class)
    @DisplayName("BBAN generator returns the registry BBAN embedded by the IBAN generator")
    void bbanMatchesIbanOfSameConfiguration(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        String country = expectedCountry(locale);
        Pattern bbanPattern = REGISTRY.get(country).bbanPattern();
        for (long seed = 0; seed < SEEDS_PER_LOCALE; seed++) {
            GeneratorConfig config = realisticConfig(seed, locale);
            String iban = new IbanGenerator(config).generate();
            String bban = new BbanGenerator(config).generate();
            assertEquals(iban.substring(4), bban, () -> locale + " IBAN " + iban + " vs BBAN " + bban);
            assertTrue(bbanPattern.matcher(bban).matches(), () -> locale + " BBAN " + bban);
        }
    }

    @Test
    @DisplayName("one generator keeps emitting valid IBANs of the same country")
    void repeatedDrawsStayValid() {
        GeneratorConfig config = realisticConfig(42L, Locale.FRANCE);
        IbanGenerator ibans = new IbanGenerator(config);
        BbanGenerator bbans = new BbanGenerator(config);
        for (int i = 0; i < SEEDS_PER_LOCALE; i++) {
            String iban = ibans.generate();
            assertConformingIban("FR", iban);
            assertEquals(iban.substring(4), bbans.generate());
        }
    }

    static Stream<Locale> localesWithoutIbanFormat() {
        return Stream.of(Locale.US, Locale.JAPAN, Locale.CHINA, Locale.KOREA, Locale.CANADA,
                         Locale.of("en", "AU"), Locale.of("hi", "IN"), Locale.of("es", "MX"),
                         Locale.ENGLISH, Locale.ROOT);
    }

    @ParameterizedTest(name = "[{index}] locale \"{0}\"")
    @MethodSource("localesWithoutIbanFormat")
    @DisplayName("locales without an IBAN country use the documented German default")
    void localesWithoutIbanFormatUseGermanDefault(Locale locale) {
        GeneratorConfig config = realisticConfig(7L, locale);
        String iban = new IbanGenerator(config).generate();
        String bban = new BbanGenerator(config).generate();

        assertTrue(iban.matches("DE\\d{20}"), () -> locale + " emitted " + iban);
        assertTrue(IBAN_VALIDATOR.isValid(iban), iban);
        assertTrue(bban.matches("\\d{18}"), () -> locale + " emitted BBAN " + bban);
    }

    @Test
    @DisplayName("regional locales use their own IBAN country")
    void regionalLocalesUseTheirOwnCountry() {
        assertEquals("BE", ibanCountry(Locale.of("fr", "BE")));
        assertEquals("BE", ibanCountry(Locale.of("nl", "BE")));
        assertEquals("CH", ibanCountry(Locale.of("de", "CH")));
        assertEquals("AT", ibanCountry(Locale.of("de", "AT")));
        assertEquals("ES", ibanCountry(Locale.of("ca", "ES")));
        assertEquals("PT", ibanCountry(Locale.of("pt", "PT")));
        assertEquals("IE", ibanCountry(Locale.of("en", "IE")));
        assertEquals("RU", ibanCountry(Locale.of("ru", "RU")));
    }

    @Test
    @DisplayName("default configuration locale emits German IBANs")
    void defaultLocaleEmitsGermanIbans() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        List<String> ibans = new ArrayList<>();
        IbanGenerator generator = new IbanGenerator(config);
        for (int i = 0; i < 100; i++) {
            ibans.add(generator.generate());
        }
        ibans.forEach(iban -> assertConformingIban(DEFAULT_COUNTRY, iban));
    }

    @Test
    @DisplayName("bank-country generator output is unchanged by the IBAN country rules")
    void bankCountryGeneratorOutputUnchanged() {
        assertEquals(List.of("BR", "ES", "IT", "ES", "US", "ES", "CN", "AU"),
                     bankCountries(Locale.CANADA));
        assertEquals(List.of("BR", "ES", "IT", "ES", "US", "ES", "CN", "AU"),
                     bankCountries(Locale.of("nl", "NL")));
        assertEquals(List.of("US", "US", "US", "US", "US", "US", "US", "US"), bankCountries(Locale.US));
        assertEquals(List.of("JP", "JP", "JP", "JP", "JP", "JP", "JP", "JP"), bankCountries(Locale.JAPAN));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void assertConformingIban(String country, String iban) {
        RegistryEntry registry = REGISTRY.get(country);
        String bban = iban.substring(4);
        assertTrue(iban.startsWith(country), () -> country + " expected: " + iban);
        assertEquals(registry.ibanLength(), iban.length(), () -> "registry length: " + iban);
        assertTrue(IBAN_VALIDATOR.isValid(iban), () -> "IBANValidator rejected " + iban);
        assertTrue(registry.bbanPattern().matcher(bban).matches(), () -> "registry BBAN structure: " + iban);
        assertTrue(NATIONAL_CHECKS.getOrDefault(country, value -> true).test(bban),
                   () -> "national check digits: " + iban);
    }

    private static String expectedCountry(Locale locale) {
        return REGISTRY.containsKey(locale.getCountry()) ? locale.getCountry() : DEFAULT_COUNTRY;
    }

    private static String ibanCountry(Locale locale) {
        String iban = new IbanGenerator(realisticConfig(3L, locale)).generate();
        assertConformingIban(iban.substring(0, 2), iban);
        return iban.substring(0, 2);
    }

    private static List<String> bankCountries(Locale locale) {
        BankCountryGenerator generator = new BankCountryGenerator(GeneratorConfig.builder()
                                                                                 .seed(7L)
                                                                                 .locale(locale)
                                                                                 .build());
        List<String> countries = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            countries.add(generator.generate());
        }
        return countries;
    }

    private static GeneratorConfig realisticConfig(long seed, Locale locale) {
        return GeneratorConfig.builder()
                              .seed(seed)
                              .locale(locale)
                              .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                              .build();
    }

    private static int digit(String value, int index) {
        return value.charAt(index) - '0';
    }

    private static int weightedSum(String digits, int... weights) {
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            sum += digit(digits, i) * weights[i];
        }
        return sum;
    }

    // ── Independent national check-digit oracles ──────────────────────────────

    /** Belgium: the last two digits are the first ten digits modulo 97 (97 when the rest is 0). */
    private static boolean belgianCheckDigitsValid(String bban) {
        int remainder = new BigInteger(bban.substring(0, 10)).mod(BigInteger.valueOf(97)).intValue();
        return Integer.parseInt(bban.substring(10)) == (remainder == 0 ? 97 : remainder);
    }

    /** Czechia and Slovakia: account prefix and account number are each weighted multiples of 11. */
    private static boolean czechSlovakCheckDigitsValid(String bban) {
        return weightedSum(bban.substring(4, 10), 10, 5, 8, 4, 2, 1) % 11 == 0
               && weightedSum(bban.substring(10), 6, 3, 7, 9, 10, 5, 8, 4, 2, 1) % 11 == 0;
    }

    /** Spain: the two DC digits check {@code 00 + bank + branch} and the account number. */
    private static boolean spanishCheckDigitsValid(String bban) {
        return digit(bban, 8) == spanishControlDigit("00" + bban.substring(0, 8))
               && digit(bban, 9) == spanishControlDigit(bban.substring(10));
    }

    private static int spanishControlDigit(String tenDigits) {
        int value = 11 - weightedSum(tenDigits, 1, 2, 4, 8, 5, 10, 9, 7, 3, 6) % 11;
        return value == 11 ? 0 : value == 10 ? 1 : value;
    }

    /** France: the 23-character RIB, with letters converted to digits, is a multiple of 97. */
    private static boolean frenchRibKeyValid(String bban) {
        String letterValues = "12345678912345678923456789";
        StringBuilder digits = new StringBuilder();
        for (char ch : bban.toCharArray()) {
            digits.append(Character.isDigit(ch) ? ch : letterValues.charAt(ch - 'A'));
        }
        return new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).signum() == 0;
    }

    /** Croatia: bank code and account number each end with an ISO 7064 MOD 11,10 check digit. */
    private static boolean croatianCheckDigitsValid(String bban) {
        return iso7064Mod1110Valid(bban.substring(0, 7)) && iso7064Mod1110Valid(bban.substring(7));
    }

    private static boolean iso7064Mod1110Valid(String digits) {
        int product = 10;
        for (int i = 0; i < digits.length() - 1; i++) {
            int sum = (product + digit(digits, i)) % 10;
            product = (sum == 0 ? 10 : sum) * 2 % 11;
        }
        return (product + digit(digits, digits.length() - 1)) % 10 == 1;
    }

    /** Hungary: bank/branch and account each carry a check digit with weights 9, 7, 3, 1. */
    private static boolean hungarianCheckDigitsValid(String bban) {
        ModulusTenCheckDigit check = new ModulusTenCheckDigit(new int[] {9, 7, 3, 1}, false, false);
        return check.isValid(bban.substring(0, 8)) && check.isValid(bban.substring(8));
    }

    /** Italy: the leading CIN letter checks ABI, CAB, and account number. */
    private static boolean italianCinValid(String bban) {
        int[] odd = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23};
        int sum = 0;
        for (int i = 1; i < bban.length(); i++) {
            char ch = bban.charAt(i);
            int value = Character.isDigit(ch) ? ch - '0' : ch - 'A';
            sum += i % 2 == 1 ? odd[value] : value;
        }
        return bban.charAt(0) == (char) ('A' + sum % 26);
    }

    /** Norway: MOD 11 check digit with weights 5, 4, 3, 2, 7, 6, 5, 4, 3, 2. */
    private static boolean norwegianCheckDigitValid(String bban) {
        int remainder = weightedSum(bban.substring(0, 10), 5, 4, 3, 2, 7, 6, 5, 4, 3, 2) % 11;
        int expected = remainder == 0 ? 0 : 11 - remainder;
        return expected < 10 && digit(bban, 10) == expected;
    }

    /** Poland: the eighth digit of the bank sort code checks it with weights 3, 9, 7, 1. */
    private static boolean polishSortCodeCheckDigitValid(String bban) {
        return new ModulusTenCheckDigit(new int[] {3, 9, 7, 1}, false, false).isValid(bban.substring(0, 8));
    }

    /** Portugal: the NIB check digits make the whole BBAN congruent to 1 modulo 97. */
    private static boolean portugueseNibCheckDigitsValid(String bban) {
        return new BigInteger(bban).mod(BigInteger.valueOf(97)).intValue() == 1;
    }
}
