/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the tier-2 national-ID providers with validators written independently from each
 * identifier's published rules, never from the provider under test.
 *
 * <p>Every validator is first proven against a published sample ID and rejected counter-examples.
 * Seeded output must then match the provider's documented format and every rule its Javadoc
 * presents, such as a birth date, which must decode to a real and plausible date. Digits a provider
 * documents as random are not asserted against the official rules; those rules are still implemented
 * and proven against the published samples.
 */
@DisplayName("Tier-2 national IDs pass independent validators")
class NationalIdTier2ValidityTest {

    private static final int       SAMPLE_SIZE              = 2_000;
    /** Fixed living-person window, so assertions never depend on the clock. */
    private static final LocalDate EARLIEST_PLAUSIBLE_BIRTH = LocalDate.of(1900, 1, 1);
    private static final LocalDate LATEST_PLAUSIBLE_BIRTH   = LocalDate.of(2025, 12, 31);

    private static void assertSeededIds(Locale locale, long seed, Pattern documentedFormat,
                                        Predicate<String> validator) {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(locale)
                                                .seed(seed)
                                                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        List<String> ids = new NationalIdGenerator(config).generateList(SAMPLE_SIZE);
        assertEquals(SAMPLE_SIZE, ids.size());
        for (String id : ids) {
            assertTrue(documentedFormat.matcher(id).matches(), () -> locale + " broke its documented format: " + id);
            assertTrue(validator.test(id), () -> locale + " generated an invalid ID: " + id);
        }
    }

    private static int number(String value, int fromInclusive, int toExclusive) {
        return Integer.parseInt(value.substring(fromInclusive, toExclusive));
    }

    private static int digit(String value, int index) {
        return Character.digit(value.charAt(index), 10);
    }

    private static boolean isPlausibleBirthDate(int year, int month, int day) {
        try {
            return isPlausible(LocalDate.of(year, month, day));
        } catch (DateTimeException nonExistentDate) {
            return false;
        }
    }

    private static boolean isPlausible(LocalDate birthDate) {
        return !birthDate.isBefore(EARLIEST_PLAUSIBLE_BIRTH) && !birthDate.isAfter(LATEST_PLAUSIBLE_BIRTH);
    }

    private static Set<Integer> codes(IntStream... groups) {
        return Stream.of(groups).flatMapToInt(group -> group).boxed().collect(Collectors.toUnmodifiableSet());
    }

    /** Replays the given {@code nextInt} results so rare draws can be exercised deterministically. */
    private static Random scripted(int... nextIntResults) {
        return new Random() {
            private int index;

            @Override
            public int nextInt(int bound) {
                int value = nextIntResults[index++];
                assertTrue(value >= 0 && value < bound, () -> "scripted draw " + value + " is outside [0, " + bound + ")");
                return value;
            }
        };
    }

    // ── DK ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("da_DK CPR number")
    class DaDkCpr {

        private static final Pattern FORMAT = Pattern.compile("\\d{6}-\\d{4}");

        /**
         * CPR-kontoret, "Personnummeret i CPR-systemet" (1 July 2008), pp. 5-7: DDMMYY followed by a
         * sequence 0001-9999 whose first digit and the year give the century. Modulus 11 is no longer
         * guaranteed (p. 3), so it is not part of validity.
         */
        static boolean isValid(String cpr) {
            if (!FORMAT.matcher(cpr).matches()) {
                return false;
            }
            int year = number(cpr, 4, 6);
            int sequence = number(cpr, 7, 11);
            int centuryDigit = sequence / 1_000;
            int century;
            if (centuryDigit <= 3) {
                century = 1900;
            } else if (centuryDigit == 4 || centuryDigit == 9) {
                century = year <= 36 ? 2000 : 1900;
            } else {
                century = year <= 57 ? 2000 : 1800;
            }
            return sequence > 0 && isPlausibleBirthDate(century + year, number(cpr, 2, 4), number(cpr, 0, 2));
        }

        @Test
        @DisplayName("validator accepts CPR-kontoret's worked example and rejects wrong centuries")
        void validatorMatchesPublishedRules() {
            // CPR-kontoret, "Personnummeret i CPR-systemet", Bilag p. 10: 070761-4285 (born 7 July 1961).
            assertTrue(isValid("070761-4285"));
            assertFalse(isValid("070755-5285"), "sequence 5xxx with year 55 denotes 2055");
            assertFalse(isValid("070761-5285"), "sequence 5xxx with year 61 denotes 1861");
            assertFalse(isValid("070761-0000"), "sequence 0000 is never assigned");
            assertFalse(isValid("310261-4285"), "31 February does not exist");
        }

        @Test
        @DisplayName("seeded CPR numbers encode a plausible birth date")
        void seededIdsAreValid() {
            assertSeededIds(Locale.of("da", "DK"), 101L, FORMAT, DaDkCpr::isValid);
        }

        @Test
        @DisplayName("valid sequence draws are kept; wrong century digits and 0000 are remapped")
        void sequenceDrawsAreRemappedOnlyWhenInvalid() {
            DaDkNationalIdProvider provider = new DaDkNationalIdProvider();
            // Draws: day offset (0 = 1 January 1950), then the four-digit sequence.
            assertEquals("010150-4321", provider.generate(scripted(0, 4_321)));
            assertEquals("010150-9876", provider.generate(scripted(0, 9_876)));
            assertEquals("010150-1543", provider.generate(scripted(0, 6_543)));
            assertEquals("010150-0001", provider.generate(scripted(0, 0)));
            assertEquals("010150-0001", provider.generate(scripted(0, 5_000)));
        }
    }

    // ── FI ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("fi_FI personal identity code (henkilötunnus)")
    class FiFiHetu {

        private static final Pattern OFFICIAL_FORMAT    = Pattern.compile("\\d{6}[-+A-FU-Y]\\d{3}[0-9A-Y]");
        private static final String  CONTROL_CHARACTERS = "0123456789ABCDEFHJKLMNPRSTUVWXY";

        /**
         * Digital and Population Data Services Agency (DVV), "Personal identity code": DDMMYY and a
         * century sign ({@code +} 1800s, {@code -} or U-Y 1900s, A-F 2000s) give the birth date.
         */
        static boolean hasPlausibleBirthDate(String hetu) {
            char sign = hetu.charAt(6);
            int century = sign == '+' ? 1800 : ("-UVWXY".indexOf(sign) >= 0 ? 1900 : 2000);
            return isPlausibleBirthDate(century + number(hetu, 4, 6), number(hetu, 2, 4), number(hetu, 0, 2));
        }

        /** Also an individual number of at least 002 and the control character DDMMYYZZZ mod 31. */
        static boolean isValid(String hetu) {
            if (!OFFICIAL_FORMAT.matcher(hetu).matches()) {
                return false;
            }
            int remainder = Integer.parseInt(hetu.substring(0, 6) + hetu.substring(7, 10)) % 31;
            return number(hetu, 7, 10) >= 2
                   && CONTROL_CHARACTERS.charAt(remainder) == hetu.charAt(10)
                   && hasPlausibleBirthDate(hetu);
        }

        @Test
        @DisplayName("validator accepts DVV's example and rejects a wrong control character or century")
        void validatorMatchesPublishedRules() {
            // DVV, https://dvv.fi/en/personal-identity-code: 131052-308T (woman born 13 October 1952).
            assertTrue(isValid("131052-308T"));
            assertFalse(isValid("131052-308U"), "wrong control character");
            assertFalse(isValid("131052A308T"), "century sign A denotes 2052");
            assertFalse(isValid("131052-308"), "the control character is mandatory");
        }

        @Test
        @DisplayName("seeded codes pair their birth date with the 1900s century sign")
        void seededIdsHavePlausibleBirthDates() {
            // The provider documents DDMMYY-XXX without the control character, so only the birth date
            // that its century sign presents is asserted.
            assertSeededIds(Locale.of("fi", "FI"), 102L, Pattern.compile("\\d{6}-\\d{3}"), FiFiHetu::hasPlausibleBirthDate);
        }

        @Test
        @DisplayName("the former 2000s century-sign draw now yields '-'; every other draw is kept")
        void centurySignDrawIsKeptButAlwaysDenotesTheNineteenHundreds() {
            FiFiNationalIdProvider provider = new FiFiNationalIdProvider();
            // Draws: day offset (0 = 1 January 1950), century-sign draw, individual number.
            assertEquals("010150-308", provider.generate(scripted(0, 0, 308)));
            assertEquals("010150-308", provider.generate(scripted(0, 1, 308)));
        }
    }

    // ── HU ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hu_HU personal identification number (személyi azonosító)")
    class HuHuSzemelyiAzonosito {

        private static final Pattern FORMAT = Pattern.compile("\\d-\\d{6}-\\d{4}");

        /**
         * Sex/century digit, YYMMDD and serial, per the law quoted in the Hungarian 2010 May advanced
         * informatics matura exam (Oktatási Hivatal, task "Személyi szám") and hu.wikipedia
         * "Személyi azonosító": 1-2 and 5-6 denote 1900s births, 3-4 the 2000s and 7-8 the 1800s.
         */
        static LocalDate birthDate(String id) {
            String digits = id.replace("-", "");
            if (!digits.matches("\\d{11}")) {
                return null;
            }
            int century = switch (digits.charAt(0)) {
                case '1', '2', '5', '6' -> 1900;
                case '3', '4' -> 2000;
                case '7', '8' -> 1800;
                default -> 0;
            };
            try {
                return LocalDate.of(century + number(digits, 1, 3), number(digits, 3, 5), number(digits, 5, 7));
            } catch (DateTimeException nonExistentDate) {
                return null;
            }
        }

        static boolean hasValidStructure(String id) {
            LocalDate birthDate = birthDate(id);
            return birthDate != null && isPlausible(birthDate);
        }

        /** Digits weighted 1..10 (born before 1997) or 10..1 (from 1997), mod 11; 10 is never issued. */
        static boolean hasValidCheckDigit(String id) {
            String digits = id.replace("-", "");
            boolean from1997 = !birthDate(id).isBefore(LocalDate.of(1997, 1, 1));
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += digit(digits, i) * (from1997 ? 10 - i : i + 1);
            }
            return sum % 11 != 10 && sum % 11 == digit(digits, 10);
        }

        @Test
        @DisplayName("validator accepts the matura exam's valid samples and rejects its invalid one")
        void validatorMatchesPublishedRules() {
            // Oktatási Hivatal, informatika emelt szint, 2010. május 11., task 2 sample sheet:
            // 30310190127 (man, 19 Oct 2003) and 40011261024 (woman, 26 Nov 2000) are "Helyes",
            // 29801236258 is "Hibás".
            assertTrue(hasValidStructure("30310190127") && hasValidCheckDigit("30310190127"));
            assertTrue(hasValidStructure("40011261024") && hasValidCheckDigit("40011261024"));
            assertFalse(hasValidCheckDigit("29801236258"));
            assertFalse(hasValidStructure("9-031019-0127"), "9 is not a sex/century digit");
            assertFalse(hasValidStructure("1-990230-1234"), "30 February does not exist");
        }

        @Test
        @DisplayName("seeded numbers keep the documented X-YYMMDD-XXXX structure (format-only contract)")
        void seededIdsHaveDocumentedStructure() {
            // The provider documents a format only: the trailing digits are random, so the check
            // digit is deliberately not asserted.
            assertSeededIds(Locale.of("hu", "HU"), 103L, FORMAT, HuHuSzemelyiAzonosito::hasValidStructure);
        }
    }

    // ── RO ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ro_RO personal numeric code (CNP)")
    class RoRoCnp {

        private static final Pattern      FORMAT       = Pattern.compile("\\d{13}");
        /** ro.wikipedia "Cod numeric personal (România)": 01-46, former Bucharest sectors 47-48, 51-52, 70. */
        private static final Set<Integer> COUNTY_CODES = codes(IntStream.rangeClosed(1, 48), IntStream.of(51, 52, 70));

        /** S (1-2: 1900s, 3-4: 1800s, 5-6: 2000s, 7-9: residents/foreigners), YYMMDD and JJ. */
        static boolean hasValidStructure(String cnp) {
            if (!FORMAT.matcher(cnp).matches()) {
                return false;
            }
            int century = switch (cnp.charAt(0)) {
                case '1', '2', '7', '8', '9' -> 1900;
                case '3', '4' -> 1800;
                case '5', '6' -> 2000;
                default -> 0;
            };
            return COUNTY_CODES.contains(number(cnp, 7, 9))
                   && isPlausibleBirthDate(century + number(cnp, 1, 3), number(cnp, 3, 5), number(cnp, 5, 7));
        }

        /**
         * Also a serial NNN from 001 to 999 and the control digit: the first 12 digits weighted by
         * 279146358279, sum mod 11, where a remainder of 10 becomes 1.
         */
        static boolean isValid(String cnp) {
            if (!hasValidStructure(cnp) || number(cnp, 9, 12) < 1) {
                return false;
            }
            String weights = "279146358279";
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += digit(cnp, i) * digit(weights, i);
            }
            int remainder = sum % 11;
            return (remainder == 10 ? 1 : remainder) == digit(cnp, 12);
        }

        @Test
        @DisplayName("validator accepts a published CNP and rejects bad control digits, counties and serials")
        void validatorMatchesPublishedRules() {
            // python-stdnum stdnum.ro.cnp doctests: 1630615123457 (man, 15 June 1963, Cluj) is valid;
            // 1630615123458 has a bad check digit and 1630615993454 an unknown county.
            assertTrue(isValid("1630615123457"));
            assertFalse(isValid("1630615123458"));
            assertFalse(hasValidStructure("1630615993454"));
            assertFalse(isValid("1630615120007"), "serial 000 is never assigned");
        }

        @Test
        @DisplayName("seeded CNPs encode sex, a plausible birth date and a county (last four digits documented random)")
        void seededIdsHaveDocumentedStructure() {
            // The provider documents its serial and final digit as random, so the serial range and the
            // control digit are deliberately not asserted.
            assertSeededIds(Locale.of("ro", "RO"), 104L, FORMAT, RoRoCnp::hasValidStructure);
        }
    }

    // ── SK ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sk_SK birth number (rodné číslo)")
    class SkSkRodneCislo {

        private static final Pattern FORMAT            = Pattern.compile("\\d{6}/\\d{3,4}");
        private static final Pattern DOCUMENTED_FORMAT = Pattern.compile("\\d{6}/\\d{4}");

        /**
         * sk.wikipedia and cs.wikipedia "Rodné číslo": YYMMDD with month +50 for women (+20 more since
         * 2004); births before 1954 carry a three-digit suffix, later births four digits, so a
         * ten-digit number with a year below 54 denotes a birth from 2000.
         */
        static boolean hasValidStructure(String rc) {
            if (!FORMAT.matcher(rc).matches()) {
                return false;
            }
            String digits = rc.replace("/", "");
            int year = number(digits, 0, 2);
            int encodedMonth = number(digits, 2, 4);
            int month = encodedMonth > 70 ? encodedMonth - 70
                : encodedMonth > 50 ? encodedMonth - 50
                : encodedMonth > 20 ? encodedMonth - 20
                : encodedMonth;
            int century = digits.length() == 9 ? (year <= 53 ? 1900 : 1800) : (year >= 54 ? 1900 : 2000);
            return isPlausibleBirthDate(century + year, month, number(digits, 4, 6));
        }

        /** Ten-digit numbers: the first nine digits mod 11, then mod 10, equal the last digit. */
        static boolean hasValidCheckDigit(String rc) {
            String digits = rc.replace("/", "");
            return digits.length() == 9 || Long.parseLong(digits.substring(0, 9)) % 11 % 10 == digit(digits, 9);
        }

        @Test
        @DisplayName("validator accepts a published birth number and rejects a bad check digit or century")
        void validatorMatchesPublishedRules() {
            // python-stdnum stdnum.sk.rc doctests: 710319/2745 (born 19 March 1971) is valid,
            // 7103192746 has a bad check digit.
            assertTrue(hasValidStructure("710319/2745") && hasValidCheckDigit("710319/2745"));
            assertFalse(hasValidCheckDigit("710319/2746"));
            assertFalse(hasValidStructure("500101/1234"), "a ten-digit number with year 50 denotes 2050");
            assertTrue(hasValidStructure("500101/123"), "births before 1954 carry a three-digit suffix");
        }

        @Test
        @DisplayName("seeded ten-digit birth numbers encode a plausible birth date (suffix documented random)")
        void seededIdsHaveDocumentedStructure() {
            // The provider documents its suffix as random, so the modulo-11 rule is deliberately not
            // asserted.
            assertSeededIds(Locale.of("sk", "SK"), 105L, DOCUMENTED_FORMAT, SkSkRodneCislo::hasValidStructure);
        }

        @Test
        @DisplayName("birth dates drawn before 1954 move four years later; every other draw is kept")
        void preNineteenFiftyFourDrawsMoveIntoTheTenDigitEra() {
            SkSkNationalIdProvider provider = new SkSkNationalIdProvider();
            // Draws: day offset (0 = 1 January 1950, 18261 = 31 December 1999), then the suffix.
            assertEquals("540101/1234", provider.generate(scripted(0, 1_234)));
            assertEquals("991231/1234", provider.generate(scripted(18_261, 1_234)));
        }
    }

    // ── UA ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("uk_UA taxpayer registration number (RNOKPP)")
    class UkUaRnokpp {

        private static final Pattern FORMAT  = Pattern.compile("\\d{10}");
        private static final int[]   WEIGHTS = { -1, 5, 7, 9, 4, 6, 10, 5, 7 };

        /**
         * uk.wikipedia "Реєстраційний номер облікової картки платника податків": digits 1-5 count
         * the days from 31 December 1899 to the birth date; digit 10 is
         * MOD(MOD(sum of the first nine digits weighted -1, 5, 7, 9, 4, 6, 10, 5, 7; 11); 10).
         */
        static boolean isValid(String code) {
            if (!FORMAT.matcher(code).matches()) {
                return false;
            }
            int sum = 0;
            for (int i = 0; i < WEIGHTS.length; i++) {
                sum += digit(code, i) * WEIGHTS[i];
            }
            return Math.floorMod(sum, 11) % 10 == digit(code, 9)
                   && isPlausible(LocalDate.of(1899, 12, 31).plusDays(number(code, 0, 5)));
        }

        @Test
        @DisplayName("validator accepts a published RNOKPP and rejects a bad check digit or future birth")
        void validatorMatchesPublishedRules() {
            // python-stdnum stdnum.ua.rntrc doctests: 1759013776 (born 28 February 1948) is valid,
            // 1759013770 has a bad check digit.
            assertTrue(isValid("1759013776"));
            assertFalse(isValid("1759013770"));
            assertFalse(isValid("9999900007"), "day 99999 after 31 December 1899 lies in 2173");
        }

        @Test
        @DisplayName("seeded numbers keep the documented 10-digit format (digits documented random)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents all ten digits as random, so the birth-date encoding and the check
            // digit are deliberately not asserted.
            assertSeededIds(Locale.of("uk", "UA"), 106L, FORMAT, code -> true);
        }
    }

    // ── BG ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("bg_BG unified civil number (EGN)")
    class BgBgEgn {

        private static final Pattern FORMAT  = Pattern.compile("\\d{10}");
        private static final int[]   WEIGHTS = { 2, 4, 8, 5, 10, 9, 7, 3, 6 };

        /** en.wikipedia "Unique citizenship number": YYMMDD with month +20 for the 1800s and +40 for the 2000s. */
        static boolean hasValidStructure(String egn) {
            if (!FORMAT.matcher(egn).matches()) {
                return false;
            }
            int encodedMonth = number(egn, 2, 4);
            int century = encodedMonth > 40 ? 2000 : (encodedMonth > 20 ? 1800 : 1900);
            int monthOffset = century == 2000 ? 40 : (century == 1800 ? 20 : 0);
            return isPlausibleBirthDate(century + number(egn, 0, 2), encodedMonth - monthOffset, number(egn, 4, 6));
        }

        /** The first nine digits weighted 2, 4, 8, 5, 10, 9, 7, 3, 6, sum mod 11; a remainder of 10 becomes 0. */
        static boolean hasValidCheckDigit(String egn) {
            int sum = 0;
            for (int i = 0; i < WEIGHTS.length; i++) {
                sum += digit(egn, i) * WEIGHTS[i];
            }
            return sum % 11 % 10 == digit(egn, 9);
        }

        @Test
        @DisplayName("validator accepts Wikipedia's EGN example and rejects a bad check digit or month")
        void validatorMatchesPublishedRules() {
            // en.wikipedia "Unique citizenship number": 1547121580 (boy born 12 July 2015).
            assertTrue(hasValidStructure("1547121580") && hasValidCheckDigit("1547121580"));
            assertFalse(hasValidCheckDigit("1547121581"));
            assertFalse(hasValidStructure("1535121580"), "month 35 is not a month code");
        }

        @Test
        @DisplayName("seeded EGNs encode a plausible birth date (format-only contract)")
        void seededIdsHaveDocumentedStructure() {
            // The provider documents a format only: the four trailing digits are random, so the check
            // digit is deliberately not asserted.
            assertSeededIds(Locale.of("bg", "BG"), 107L, FORMAT, BgBgEgn::hasValidStructure);
        }
    }

    // ── HR ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hr_HR personal identification number (OIB)")
    class HrHrOib {

        private static final Pattern FORMAT = Pattern.compile("\\d{11}");

        /** ISO 7064 MOD 11,10 verification over all eleven digits: the final running sum must be 1. */
        static boolean hasValidCheckDigit(String oib) {
            int product = 10;
            int sum = 0;
            for (int i = 0; i < 11; i++) {
                sum = (digit(oib, i) + product) % 10;
                if (sum == 0) {
                    sum = 10;
                }
                product = sum * 2 % 11;
            }
            return sum == 1;
        }

        @Test
        @DisplayName("validator accepts published OIBs and rejects a bad check digit")
        void validatorMatchesPublishedRules() {
            // FINA info.BIZ register: Republika Hrvatska Ministarstvo financija, OIB 18683136487.
            assertTrue(FORMAT.matcher("18683136487").matches() && hasValidCheckDigit("18683136487"));
            // python-stdnum stdnum.hr.oib doctests: 33392005961 is valid, 33392005962 is not.
            assertTrue(hasValidCheckDigit("33392005961"));
            assertFalse(hasValidCheckDigit("33392005962"));
        }

        @Test
        @DisplayName("seeded OIBs keep the documented 11-digit format (format-only contract)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents a format only: all digits are random, so the ISO 7064 check digit
            // is deliberately not asserted.
            assertSeededIds(Locale.of("hr", "HR"), 108L, FORMAT, oib -> true);
        }
    }

    // ── GR ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("el_GR tax registration number (AFM)")
    class ElGrAfm {

        private static final Pattern FORMAT = Pattern.compile("\\d{9}");

        /** The first eight digits weighted 256, 128, ..., 2, sum mod 11, then mod 10, equal the ninth. */
        static boolean hasValidCheckDigit(String afm) {
            int sum = 0;
            for (int i = 0; i < 8; i++) {
                sum += digit(afm, i) << (8 - i);
            }
            return sum % 11 % 10 == digit(afm, 8);
        }

        @Test
        @DisplayName("validator accepts published AFMs and rejects a bad check digit")
        void validatorMatchesPublishedRules() {
            // ΔΕΗ Α.Ε. (Public Power Corporation) prints AFM 090000045 on its official documents.
            assertTrue(FORMAT.matcher("090000045").matches() && hasValidCheckDigit("090000045"));
            // python-stdnum stdnum.gr.vat doctest: 094259216 is valid.
            assertTrue(hasValidCheckDigit("094259216"));
            assertFalse(hasValidCheckDigit("090000046"));
        }

        @Test
        @DisplayName("seeded AFMs keep the documented 9-digit format (format-only contract)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents a format only: all digits are random, so the AFM check digit is
            // deliberately not asserted.
            assertSeededIds(Locale.of("el", "GR"), 109L, FORMAT, afm -> true);
        }
    }

    // ── TH ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("th_TH personal identification number")
    class ThThPin {

        private static final Pattern      FORMAT         = Pattern.compile("\\d-\\d{4}-\\d{5}-\\d{2}-\\d");
        /** Numeric ISO 3166-2:TH province codes (TH-10 Bangkok to TH-96 Narathiwat). */
        private static final Set<Integer> PROVINCE_CODES = codes(IntStream.rangeClosed(10, 27),
                                                                 IntStream.rangeClosed(30, 58),
                                                                 IntStream.rangeClosed(60, 67),
                                                                 IntStream.rangeClosed(70, 77),
                                                                 IntStream.rangeClosed(80, 86),
                                                                 IntStream.rangeClosed(90, 96));

        /**
         * th.wikipedia "เลขประจำตัวประชาชนไทย": category digit (0-8; 9 is reserved), registrar
         * office (province + district), and check digit (11 - (13*d1 + 12*d2 + ... + 2*d12) mod 11) mod 10.
         */
        static boolean isValid(String pin) {
            String digits = pin.replace("-", "");
            if (!digits.matches("\\d{13}")) {
                return false;
            }
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += digit(digits, i) * (13 - i);
            }
            return digit(digits, 0) != 9
                   && PROVINCE_CODES.contains(number(digits, 1, 3))
                   && number(digits, 3, 5) >= 1
                   && (11 - sum % 11) % 10 == digit(digits, 12);
        }

        @Test
        @DisplayName("validator accepts a published PIN and rejects a bad check digit or province")
        void validatorMatchesPublishedRules() {
            // python-stdnum stdnum.th.pin doctests: 3100600445635 is valid, 1-2345-45678-78-9 is not.
            assertTrue(isValid("3100600445635"));
            assertFalse(isValid("1-2345-45678-78-9"));
            assertFalse(isValid("3000600445636"), "00 is not a province code");
        }

        @Test
        @DisplayName("seeded PINs keep the documented X-XXXX-XXXXX-XX-X format (digits documented random)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents a non-zero first digit followed by random digits, so the registrar
            // office and the check digit are deliberately not asserted.
            assertSeededIds(Locale.of("th", "TH"), 110L, FORMAT, pin -> pin.charAt(0) != '0');
        }
    }

    // ── VN ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("vi_VN citizen identity card number (CCCD)")
    class ViVnCccd {

        private static final Pattern      FORMAT         = Pattern.compile("\\d{12}");
        /** Appendix 1 of Circular 07/2016/TT-BCA (Ministry of Public Security): the 63 province codes. */
        private static final Set<Integer> PROVINCE_CODES = codes(IntStream.of(
            1, 2, 4, 6, 8, 10, 11, 12, 14, 15, 17, 19, 20, 22, 24, 25, 26, 27, 30, 31, 33, 34, 35, 36, 37, 38,
            40, 42, 44, 45, 46, 48, 49, 51, 52, 54, 56, 58, 60, 62, 64, 66, 67, 68, 70, 72, 74, 75, 77, 79, 80,
            82, 83, 84, 86, 87, 89, 91, 92, 93, 94, 95, 96));

        /**
         * Decree 137/2015/ND-CP art. 7: province code, sex/century digit (0/1 = 1900s, 2/3 = 2000s, ...),
         * two-digit birth year and a six-digit random number.
         */
        static boolean isValid(String id) {
            if (!FORMAT.matcher(id).matches()) {
                return false;
            }
            int birthYear = 1900 + 100 * (digit(id, 3) / 2) + number(id, 4, 6);
            return PROVINCE_CODES.contains(number(id, 0, 3))
                   && number(id, 6, 12) >= 1
                   && birthYear >= EARLIEST_PLAUSIBLE_BIRTH.getYear()
                   && birthYear <= LATEST_PLAUSIBLE_BIRTH.getYear();
        }

        @Test
        @DisplayName("validator accepts published CCCD numbers and rejects bad provinces or centuries")
        void validatorMatchesPublishedRules() {
            // CafeF (2022-11-11) decodes 020093001656 as Lạng Sơn, male, born 1993; Vietnamnet
            // (Dân tộc & Phát triển) decodes 001215000001 as Hà Nội, male, born 2015.
            assertTrue(isValid("020093001656"));
            assertTrue(isValid("001215000001"));
            assertFalse(isValid("097093001656"), "097 is not a province code");
            assertFalse(isValid("020293001656"), "sex/century digit 2 with year 93 denotes 2093");
        }

        @Test
        @DisplayName("seeded numbers keep the documented 12-digit format (digits documented random)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents all twelve digits as random, so the province, century and birth-year
            // fields are deliberately not asserted.
            assertSeededIds(Locale.of("vi", "VN"), 111L, FORMAT, id -> true);
        }
    }

    // ── ID ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("id_ID population identification number (NIK)")
    class IdIdNik {

        private static final Pattern      FORMAT         = Pattern.compile("\\d{16}");
        /** Kepmendagri No 300.2.2-2430 Tahun 2025 province codes. */
        private static final Set<Integer> PROVINCE_CODES = codes(IntStream.rangeClosed(11, 19), IntStream.of(21),
                                                                 IntStream.rangeClosed(31, 36), IntStream.rangeClosed(51, 53),
                                                                 IntStream.rangeClosed(61, 65), IntStream.rangeClosed(71, 76),
                                                                 IntStream.of(81, 82), IntStream.rangeClosed(91, 96));

        /**
         * id.wikipedia "Nomor Induk Kependudukan": province, regency and district codes, the birth
         * date DDMMYY (40 added to the day for women) and a serial number starting at 0001.
         */
        static boolean isValid(String nik) {
            if (!FORMAT.matcher(nik).matches()) {
                return false;
            }
            int encodedDay = number(nik, 6, 8);
            int day = encodedDay > 40 ? encodedDay - 40 : encodedDay;
            int month = number(nik, 8, 10);
            int year = number(nik, 10, 12);
            return PROVINCE_CODES.contains(number(nik, 0, 2))
                   && number(nik, 2, 4) >= 1
                   && number(nik, 4, 6) >= 1
                   && number(nik, 12, 16) >= 1
                   && (isPlausibleBirthDate(1900 + year, month, day) || isPlausibleBirthDate(2000 + year, month, day));
        }

        @Test
        @DisplayName("validator accepts published NIKs and rejects bad provinces or dates")
        void validatorMatchesPublishedRules() {
            // python-stdnum stdnum.id.nik doctests: 3171011708450001 is valid, 3171015708450001 encodes
            // a woman born 17 August 1945, 9971011708450001 has an unknown province and
            // 3171013002001234 an impossible date.
            assertTrue(isValid("3171011708450001"));
            assertTrue(isValid("3171015708450001"));
            assertFalse(isValid("9971011708450001"));
            assertFalse(isValid("3171013002001234"));
        }

        @Test
        @DisplayName("seeded NIKs keep the documented 16-digit format (digits documented random)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents all sixteen digits as random, so the region, birth-date and serial
            // fields are deliberately not asserted.
            assertSeededIds(Locale.of("id", "ID"), 112L, FORMAT, nik -> true);
        }
    }

    // ── MY ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ms_MY identity card number (MyKad)")
    class MsMyMyKad {

        private static final Pattern      FORMAT                = Pattern.compile("\\d{6}-\\d{2}-\\d{4}");
        /** en.wikipedia "Malaysian identity card": place-of-birth codes marked n/a. */
        private static final Set<Integer> UNUSED_BIRTH_PLACES   = codes(IntStream.of(0), IntStream.rangeClosed(17, 20),
                                                                        IntStream.of(69, 70, 73, 80, 81),
                                                                        IntStream.rangeClosed(94, 97));

        /** YYMMDD birth date (no century marker), two-digit place of birth and four more digits. */
        static boolean isValid(String nric) {
            if (!FORMAT.matcher(nric).matches()) {
                return false;
            }
            int year = number(nric, 0, 2);
            int month = number(nric, 2, 4);
            int day = number(nric, 4, 6);
            return !UNUSED_BIRTH_PLACES.contains(number(nric, 7, 9))
                   && (isPlausibleBirthDate(1900 + year, month, day) || isPlausibleBirthDate(2000 + year, month, day));
        }

        @Test
        @DisplayName("validator accepts a published MyKad number and rejects bad dates or birth places")
        void validatorMatchesPublishedRules() {
            // python-stdnum stdnum.my.nric doctests: 770305-02-1234 is valid, 771305-02-1234 has an
            // invalid date and 770305-17-1234 an unknown birth place.
            assertTrue(isValid("770305-02-1234"));
            assertFalse(isValid("771305-02-1234"));
            assertFalse(isValid("770305-17-1234"));
        }

        @Test
        @DisplayName("seeded MyKad numbers use a real birth date and a state birth place")
        void seededIdsAreValid() {
            assertSeededIds(Locale.of("ms", "MY"), 113L, FORMAT,
                            nric -> isValid(nric) && number(nric, 7, 9) >= 1 && number(nric, 7, 9) <= 16);
        }
    }

    // ── IL ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("he_IL identity number (Teudat Zehut)")
    class HeIlTeudatZehut {

        private static final Pattern FORMAT = Pattern.compile("\\d{9}");

        /** Digits weighted 1, 2, 1, 2, ... with two-digit products reduced to their digit sum; total mod 10 is 0. */
        static boolean hasValidCheckDigit(String id) {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                int product = digit(id, i) * (i % 2 == 0 ? 1 : 2);
                sum += product / 10 + product % 10;
            }
            return sum % 10 == 0;
        }

        @Test
        @DisplayName("validator accepts published identity numbers and rejects a bad check digit")
        void validatorMatchesPublishedRules() {
            // he.wikipedia "מספר זהות (ישראל)" infobox sample: 050012343.
            assertTrue(FORMAT.matcher("050012343").matches() && hasValidCheckDigit("050012343"));
            // python-stdnum stdnum.il.idnr doctest: 3933742-3 (039337423) is valid.
            assertTrue(hasValidCheckDigit("039337423"));
            assertFalse(hasValidCheckDigit("050012344"));
        }

        @Test
        @DisplayName("seeded identity numbers keep the documented 9-digit format (format-only contract)")
        void seededIdsHaveDocumentedFormat() {
            // The provider documents a format only: all digits are random, so the check digit is
            // deliberately not asserted.
            assertSeededIds(Locale.of("he", "IL"), 114L, FORMAT, id -> true);
        }
    }

    // ── ES (ca) ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ca_ES Spanish DNI")
    class CaEsDni {

        private static final Pattern FORMAT        = Pattern.compile("\\d{8}[A-Z]");
        private static final String  CHECK_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

        /** Ministerio del Interior, "Cálculo del dígito de control del NIF/NIE": the number mod 23 selects the letter. */
        static boolean isValid(String dni) {
            return FORMAT.matcher(dni).matches()
                   && CHECK_LETTERS.charAt(Integer.parseInt(dni.substring(0, 8)) % 23) == dni.charAt(8);
        }

        @Test
        @DisplayName("validator accepts the Ministerio del Interior example and rejects a wrong letter")
        void validatorMatchesPublishedRules() {
            // Ministerio del Interior worked example: 12345678 mod 23 = 14 -> Z.
            assertTrue(isValid("12345678Z"));
            assertFalse(isValid("12345678A"));
        }

        @Test
        @DisplayName("seeded DNIs carry the correct check letter")
        void seededIdsAreValid() {
            assertSeededIds(Locale.of("ca", "ES"), 115L, FORMAT, CaEsDni::isValid);
        }
    }
}
