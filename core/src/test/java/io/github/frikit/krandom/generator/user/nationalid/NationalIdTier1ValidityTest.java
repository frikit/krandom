/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.apache.commons.validator.routines.checkdigit.VerhoeffCheckDigit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every Tier-1 built-in national-ID provider against an independent validator written from
 * the published rules of the identifier. Validators never call provider helpers; each one must accept
 * published sample identifiers, reject known-invalid ones, and accept {@value #GENERATED_COUNT} seeded
 * generated values.
 */
@DisplayName("Tier-1 national IDs satisfy their published validity rules")
class NationalIdTier1ValidityTest {

    static final int  GENERATED_COUNT = 1_000;
    static final long SEED            = 20_260_927L;

    static List<String> generated(Locale locale) {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(locale)
                                                .seed(SEED)
                                                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        List<String> ids = new NationalIdGenerator(config).generateList(GENERATED_COUNT);
        assertEquals(GENERATED_COUNT, ids.size());
        return ids;
    }

    static List<String> generated(NationalIdProvider provider) {
        Random random = new Random(SEED);
        List<String> ids = new ArrayList<>(GENERATED_COUNT);
        for (int i = 0; i < GENERATED_COUNT; i++) {
            ids.add(provider.generate(random));
        }
        return ids;
    }

    static void assertAllSatisfy(List<String> ids, Predicate<String> rule) {
        for (String id : ids) {
            assertTrue(rule.test(id), () -> "Generated ID violates the published rules: " + id);
        }
    }

    static void assertAccepted(Predicate<String> validator, String... samples) {
        for (String sample : samples) {
            assertTrue(validator.test(sample), () -> "Valid sample rejected: " + sample);
        }
    }

    static void assertRejected(Predicate<String> validator, String... samples) {
        for (String sample : samples) {
            assertFalse(validator.test(sample), () -> "Invalid sample accepted: " + sample);
        }
    }

    static int digit(String value, int index) {
        return value.charAt(index) - '0';
    }

    static int number(String value, int beginIndex, int endIndex) {
        return Integer.parseInt(value.substring(beginIndex, endIndex));
    }

    static boolean isCalendarDate(int year, int month, int day) {
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    static boolean isDateInEitherCentury(int twoDigitYear, int month, int day) {
        return isCalendarDate(1900 + twoDigitYear, month, day) || isCalendarDate(2000 + twoDigitYear, month, day);
    }

    // ── US ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("US Social Security Number")
    class UsSocialSecurityNumber {

        /**
         * SSA, "Social Security Number Randomization" (ssa.gov/employer/randomization.html): area
         * numbers 000, 666 and 900-999, group number 00 and serial number 0000 are never assigned.
         */
        static boolean isValid(String value) {
            if (!value.matches("\\d{3}-\\d{2}-\\d{4}|\\d{9}")) {
                return false;
            }
            String digits = value.replace("-", "");
            int area = number(digits, 0, 3);
            int group = number(digits, 3, 5);
            int serial = number(digits, 5, 9);
            return area != 0 && area != 666 && area < 900 && group != 0 && serial != 0;
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // SSA history / Wikipedia "Social Security number": the Woolworth wallet-insert number.
            assertAccepted(UsSocialSecurityNumber::isValid, "078-05-1120", "078051120");
        }

        @Test
        @DisplayName("never-assigned areas, groups and serials are rejected")
        void neverAssignedRejected() {
            assertRejected(UsSocialSecurityNumber::isValid,
                           "000-12-3456", "666-12-3456", "900-12-3456", "987-65-4320", "123-00-4567",
                           "123-45-0000", "123-45-678");
        }

        @Test
        @DisplayName("1000 seeded SSNs validate in every output format")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.US), id -> id.matches("\\d{3}-\\d{2}-\\d{4}") && isValid(id));
            assertAllSatisfy(generated(new UsNationalIdProvider().withoutDashes()),
                             id -> id.matches("\\d{9}") && isValid(id));
            assertAllSatisfy(generated(new UsNationalIdProvider().lastFourOnly()),
                             id -> id.matches("\\d{4}") && !id.equals("0000"));
        }
    }

    // ── GB ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GB National Insurance number")
    class GbNationalInsuranceNumber {

        private static final Set<String> NOT_ALLOCATED = Set.of("BG", "GB", "KN", "NK", "NT", "TN", "ZZ");

        /**
         * HMRC National Insurance Manual NIM39110: two prefix letters, six digits and a final letter
         * A-D; D, F, I, Q, U and V are never prefix letters, O is never the second prefix letter, and
         * the prefixes BG, GB, KN, NK, NT, TN and ZZ are not used.
         */
        static boolean isValid(String value) {
            String nino = value.replace(" ", "");
            if (!nino.matches("[A-Z]{2}\\d{6}[A-D]")) {
                return false;
            }
            return "DFIQUV".indexOf(nino.charAt(0)) < 0
                   && "DFIOQUV".indexOf(nino.charAt(1)) < 0
                   && !NOT_ALLOCATED.contains(nino.substring(0, 2));
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // HMRC Developer Hub, Making Tax Digital API documentation example NINO.
            assertAccepted(GbNationalInsuranceNumber::isValid, "TC663795B", "TC 66 37 95 B");
        }

        @Test
        @DisplayName("HMRC's illustrative number and unallocated prefixes are rejected")
        void invalidRejected() {
            // HMRC NIM39110 illustrates the format with "QQ 12 34 56 A", which is not a usable number.
            assertRejected(GbNationalInsuranceNumber::isValid,
                           "QQ 12 34 56 A", "GB 12 34 56 A", "AO 12 34 56 A", "DA 12 34 56 A", "AB 12 34 56 E",
                           "AB 12 34 5 A");
        }

        @Test
        @DisplayName("1000 seeded NINOs validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.UK),
                             id -> id.matches("[A-Z]{2} \\d{2} \\d{2} \\d{2} [A-D]") && isValid(id));
        }
    }

    // ── AU ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AU Tax File Number")
    class AuTaxFileNumber {

        private static final int[] WEIGHTS = { 1, 4, 3, 7, 5, 8, 6, 9, 10 };

        /**
         * ATO TFN algorithm (Wikipedia "Tax file number"): the digits weighted 1, 4, 3, 7, 5, 8, 6, 9
         * and 10 must sum to a multiple of 11.
         */
        static boolean isValid(String value) {
            if (!value.matches("\\d{3} \\d{3} \\d{3}|\\d{9}")) {
                return false;
            }
            String digits = value.replace(" ", "");
            int sum = 0;
            for (int i = 0; i < WEIGHTS.length; i++) {
                sum += WEIGHTS[i] * digit(digits, i);
            }
            return sum % 11 == 0;
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // Wikipedia "Tax file number" worked example: weighted sum 253 = 11 x 23.
            assertAccepted(AuTaxFileNumber::isValid, "123 456 782", "123456782");
        }

        @Test
        @DisplayName("wrong check digit is rejected")
        void invalidRejected() {
            assertRejected(AuTaxFileNumber::isValid, "123 456 789", "123 456 783", "123 456 78");
        }

        @Test
        @DisplayName("1000 seeded TFNs validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("en", "AU")),
                             id -> id.matches("[1-9]\\d{2} \\d{3} \\d{3}") && isValid(id));
        }
    }

    // ── FR ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FR NIR")
    class FrNir {

        private static final Pattern NIR =
            Pattern.compile("[12]\\d{2}(?:0[1-9]|1[0-2])(\\d{2}|2A|2B)(\\d{3})(\\d{3})(\\d{2})");

        /**
         * French NIR (fr.wikipedia "Numéro de sécurité sociale en France"): sex, YY, month,
         * department, commune and birth-order fields followed by the key 97 - (first 13 characters
         * mod 97), where the Corsican departments 2A and 2B count as 19 and 18.
         */
        static boolean isValid(String value) {
            String nir = value.replace(" ", "");
            Matcher matcher = NIR.matcher(nir);
            if (!matcher.matches()
                || matcher.group(1).equals("00")
                || matcher.group(2).equals("000")
                || matcher.group(3).equals("000")) {
                return false;
            }
            long body = Long.parseLong(nir.substring(0, 13).replace("2A", "19").replace("2B", "18"));
            return Integer.parseInt(matcher.group(4)) == 97 - body % 97;
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // fr.wikipedia "Numéro de sécurité sociale en France" example.
            assertAccepted(FrNir::isValid, "2 69 05 49 588 157 80", "269054958815780");
            // Constructed Corsican examples exercising the 2A/2B substitution.
            assertAccepted(FrNir::isValid, "1 85 05 2A 004 123 56", "1 85 05 2B 004 123 83");
        }

        @Test
        @DisplayName("wrong keys are rejected")
        void invalidRejected() {
            // "183054212345643" was the provider's former Javadoc example; its correct key is 92.
            assertRejected(FrNir::isValid, "2 69 05 49 588 157 81", "183054212345643", "1 85 05 2A 004 123 83");
        }

        @Test
        @DisplayName("1000 seeded NIRs validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.FRANCE), id -> id.matches("[12]\\d{14}") && isValid(id));
        }
    }

    // ── DE ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DE Steuer-ID")
    class DeSteuerId {

        /**
         * ELSTER, "Prüfung der Steuer- und Steueridentifikationsnummer", section 2.2: eleven digits
         * without a leading zero; among digits 1-10 exactly one digit occurs twice or three times, its
         * three occurrences never directly consecutive, and every other digit at most once; digit 11 is
         * the ISO/IEC 7064 MOD 11,10 check digit, so the running sum after all eleven digits is 1.
         */
        static boolean isValid(String value) {
            if (!value.matches("[1-9]\\d{10}")) {
                return false;
            }
            int[] occurrences = new int[10];
            for (int i = 0; i < 10; i++) {
                occurrences[digit(value, i)]++;
            }
            int repeatedDigits = 0;
            for (int count : occurrences) {
                if (count > 3) {
                    return false;
                }
                if (count > 1) {
                    repeatedDigits++;
                }
            }
            if (repeatedDigits != 1) {
                return false;
            }
            for (int i = 2; i < 10; i++) {
                if (digit(value, i) == digit(value, i - 1) && digit(value, i) == digit(value, i - 2)) {
                    return false;
                }
            }
            int product = 10;
            int sum = 0;
            for (int i = 0; i < 11; i++) {
                sum = (digit(value, i) + product) % 10;
                if (sum == 0) {
                    sum = 10;
                }
                product = (2 * sum) % 11;
            }
            return sum == 1;
        }

        @Test
        @DisplayName("published samples validate")
        void publishedSamples() {
            // ELSTER "Prüfung der Steuer- und Steueridentifikationsnummer", Tabelle 2-1 (ITZ Bund concept
            // IDs): a digit twice (7, 8) and a digit three times with two adjacent (9, 5, 1).
            assertAccepted(DeSteuerId::isValid,
                           "86095742719", "47036892816", "65929970489", "57549285017", "25768131411");
        }

        @Test
        @DisplayName("check-digit and digit-repetition violations are rejected")
        void invalidRejected() {
            assertRejected(DeSteuerId::isValid,
                           "86095742718", // wrong check digit
                           "02476291358", // ELSTER test IdNr: a leading zero is only allowed for test numbers
                           "12345678903", // correct check digit, but no digit repeats
                           "11223456785", // correct check digit, but two digits repeat
                           "11123456786", // correct check digit, but a digit occurs three times in a row
                           "11112345678"  // correct check digit, but a digit occurs four times
            );
        }

        @Test
        @DisplayName("1000 seeded Steuer-IDs validate and cover both repetition shapes and check digit 0")
        void generatedValidate() {
            List<String> ids = generated(Locale.GERMANY);
            assertAllSatisfy(ids, DeSteuerId::isValid);
            Set<Integer> maxOccurrences = new HashSet<>();
            Set<Character> checkDigits = new HashSet<>();
            for (String id : ids) {
                int[] occurrences = new int[10];
                int max = 0;
                for (int i = 0; i < 10; i++) {
                    max = Math.max(max, ++occurrences[digit(id, i)]);
                }
                maxOccurrences.add(max);
                checkDigits.add(id.charAt(10));
            }
            assertEquals(Set.of(2, 3), maxOccurrences);
            assertEquals(10, checkDigits.size(), "every check digit 0-9 is reachable");
        }
    }

    // ── JP ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("JP My Number")
    class JpMyNumber {

        /**
         * Check digit defined by 平成26年総務省令第85号 第5条: 11 - (sum of Pn x Qn mod 11), or 0 when
         * the remainder is at most 1; Pn is the n-th digit counted from the right of the first eleven
         * digits and Qn is n + 1 for n up to 6 and n - 5 for n from 7 to 11.
         */
        static boolean isValid(String value) {
            if (!value.matches("\\d{12}")) {
                return false;
            }
            int sum = 0;
            for (int n = 1; n <= 11; n++) {
                int q = n <= 6 ? n + 1 : n - 5;
                sum += digit(value, 11 - n) * q;
            }
            int remainder = sum % 11;
            return digit(value, 11) == (remainder <= 1 ? 0 : 11 - remainder);
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // Worked example citing the ordinance formula (blog.ohgaki.net/my-number-check-digit-calculation).
            assertAccepted(JpMyNumber::isValid, "123456789018");
        }

        @Test
        @DisplayName("wrong check digit is rejected")
        void invalidRejected() {
            assertRejected(JpMyNumber::isValid, "123456789017", "12345678901");
        }

        @Test
        @DisplayName("1000 seeded My Numbers validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.JAPAN), id -> id.matches("[1-9]\\d{11}") && isValid(id));
        }
    }

    // ── ES ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ES DNI")
    class EsDni {

        private static final String  CONTROL_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
        private static final Pattern DNI             = Pattern.compile("(\\d{8})-?([A-Z])");

        /**
         * Ministerio del Interior, "Cálculo del dígito de control del NIF/NIE": the control letter is
         * the entry of TRWAGMYFPDXBNJZSQVHLCKE at the eight-digit number modulo 23.
         */
        static boolean isValid(String value) {
            Matcher matcher = DNI.matcher(value);
            return matcher.matches()
                   && CONTROL_LETTERS.charAt(Integer.parseInt(matcher.group(1)) % 23) == matcher.group(2).charAt(0);
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // Ministerio del Interior worked example: 12345678 mod 23 = 14 -> Z.
            assertAccepted(EsDni::isValid, "12345678Z", "12345678-Z");
        }

        @Test
        @DisplayName("wrong control letter is rejected")
        void invalidRejected() {
            assertRejected(EsDni::isValid, "12345678A", "1234567Z");
        }

        @Test
        @DisplayName("1000 seeded DNIs validate with and without the dash")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("es", "ES")), id -> id.matches("\\d{8}[A-Z]") && isValid(id));
            assertAllSatisfy(generated(new EsNationalIdProvider().withDash()),
                             id -> id.matches("\\d{8}-[A-Z]") && isValid(id));
        }
    }

    // ── IT ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("IT Codice Fiscale")
    class ItCodiceFiscale {

        private static final String  MONTH_LETTERS       = "ABCDEHLMPRST";
        // Odd-position conversion values from DM 23 December 1976, for 0-9 and A-Z (0 and A share 1, ...).
        private static final int[]   ODD_POSITION_VALUES = {
            1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23
        };
        private static final Pattern CODE                =
            Pattern.compile("[A-Z]{6}(\\d{2})([" + MONTH_LETTERS + "])(\\d{2})[A-Z]\\d{3}[A-Z]");

        /**
         * Codice fiscale of a natural person (DM 23 December 1976; Wikipedia "Italian fiscal code"):
         * name letters, YY, month letter, birth day (plus 40 for women) forming a real calendar date,
         * birthplace code, and a check letter A-Z equal to the sum of the odd-position conversion values
         * and the even-position values (0-9, A-Z = 0-25) modulo 26.
         */
        static boolean isValid(String value) {
            Matcher matcher = CODE.matcher(value);
            if (!matcher.matches()) {
                return false;
            }
            int dayField = Integer.parseInt(matcher.group(3));
            int day = dayField > 40 ? dayField - 40 : dayField;
            int month = MONTH_LETTERS.indexOf(matcher.group(2)) + 1;
            if (!isDateInEitherCentury(Integer.parseInt(matcher.group(1)), month, day)) {
                return false;
            }
            int sum = 0;
            for (int position = 1; position <= 15; position++) {
                char c = value.charAt(position - 1);
                int ordinal = Character.isDigit(c) ? c - '0' : c - 'A';
                sum += position % 2 == 1 ? ODD_POSITION_VALUES[ordinal] : ordinal;
            }
            return value.charAt(15) == 'A' + sum % 26;
        }

        @Test
        @DisplayName("published samples validate")
        void publishedSamples() {
            // Wikipedia "Italian fiscal code" (male born in Milan; female born abroad) and the
            // python-stdnum it.codicefiscale doctest.
            assertAccepted(ItCodiceFiscale::isValid, "MRTMTT91D08F205J", "MLLSNT82P65Z404U", "RCCMNL83S18D969H");
            // Constructed: 29 February 2000 is a real date for YY = 00.
            assertAccepted(ItCodiceFiscale::isValid, "RSSMRA00B29H501Y");
        }

        @Test
        @DisplayName("wrong check letters and impossible birth dates are rejected")
        void invalidRejected() {
            assertRejected(ItCodiceFiscale::isValid,
                           "MRTMTT91D08F205K", // wrong check letter
                           "RSSMRA85B30H501C", // correct check letter, but 30 February
                           "RSSMRA01B29H501Z", // correct check letter, but 29 February in a non-leap year
                           "RSSMRA85D31H501I"  // correct check letter, but 31 April
            );
        }

        @Test
        @DisplayName("1000 seeded codici fiscali validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.ITALY), ItCodiceFiscale::isValid);
        }
    }

    // ── BR ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("BR CPF")
    class BrCpf {

        /**
         * Receita Federal CPF verifier digits (pt.wikipedia "Cadastro de pessoas físicas"): nine digits
         * are weighted 10 down to 2 (digits 1-9 for the first verifier, digits 2-9 plus the first
         * verifier for the second); each verifier is 11 - (sum mod 11), or 0 when that remainder is
         * below 2.
         */
        static boolean isValid(String value) {
            if (!value.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}")) {
                return false;
            }
            String digits = value.replaceAll("\\D", "");
            return digit(digits, 9) == verifier(digits, 0) && digit(digits, 10) == verifier(digits, 1);
        }

        private static int verifier(String digits, int start) {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += digit(digits, start + i) * (10 - i);
            }
            int remainder = sum % 11;
            return remainder < 2 ? 0 : 11 - remainder;
        }

        @Test
        @DisplayName("published samples validate")
        void publishedSamples() {
            // pt.wikipedia "Cadastro de pessoas físicas" worked examples.
            assertAccepted(BrCpf::isValid, "123.456.789-09", "012.345.678-90", "12345678909");
        }

        @Test
        @DisplayName("wrong verifier digits are rejected")
        void invalidRejected() {
            assertRejected(BrCpf::isValid, "123.456.789-10", "123.456.789-00", "123.456.789-0");
        }

        @Test
        @DisplayName("1000 seeded CPFs validate with and without formatting")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("pt", "BR")),
                             id -> id.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}") && isValid(id));
            assertAllSatisfy(generated(new BrNationalIdProvider().withoutFormatting()),
                             id -> id.matches("\\d{11}") && isValid(id));
        }
    }

    // ── CN ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CN Resident Identity Card number")
    class CnResidentIdentityNumber {

        private static final Pattern ID = Pattern.compile("(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})[\\dX]");

        /**
         * GB 11643-1999 (Wikipedia "Resident Identity Card"): region, yyyyMMdd birth date, sequence and
         * an ISO 7064 MOD 11-2 check character. The weight of each digit is 2^(i-1) mod 11, counting
         * positions from the right with the check character at i = 1; the check value is
         * (12 - (S mod 11)) mod 11, written as 'X' when it is 10.
         */
        static boolean isValid(String value) {
            Matcher matcher = ID.matcher(value);
            if (!matcher.matches()
                || !isCalendarDate(Integer.parseInt(matcher.group(2)),
                                   Integer.parseInt(matcher.group(3)),
                                   Integer.parseInt(matcher.group(4)))) {
                return false;
            }
            int sum = 0;
            int weight = 1;
            for (int i = 16; i >= 0; i--) {
                weight = weight * 2 % 11;
                sum += digit(value, i) * weight;
            }
            int check = (12 - sum % 11) % 11;
            return value.charAt(17) == (check == 10 ? 'X' : (char) ('0' + check));
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // GB 11643-1999 example quoted by Wikipedia "Resident Identity Card" (S mod 11 = 2 -> 'X').
            assertAccepted(CnResidentIdentityNumber::isValid, "11010519491231002X");
        }

        @Test
        @DisplayName("wrong check characters and impossible dates are rejected")
        void invalidRejected() {
            // "110101198001011237" was the provider's former Javadoc example; its correct check character is 2.
            assertRejected(CnResidentIdentityNumber::isValid,
                           "110101198001011237", "110105194912310021", "11010519491331002X");
        }

        @Test
        @DisplayName("1000 seeded IDs validate and stay inside the documented field ranges")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.CHINA), id -> isValid(id)
                                                            && number(id, 0, 6) >= 100_000
                                                            && number(id, 0, 6) <= 658_999
                                                            && number(id, 6, 10) >= 1940
                                                            && number(id, 6, 10) <= 2005
                                                            && number(id, 12, 14) <= 28
                                                            && number(id, 14, 17) >= 1);
        }
    }

    // ── NL ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("NL BSN")
    class NlBsn {

        /**
         * Elfproef (nl.wikipedia "Burgerservicenummer"): 9A + 8B + 7C + 6D + 5E + 4F + 3G + 2H - I is a
         * positive multiple of 11.
         */
        static boolean isValid(String value) {
            if (!value.matches("\\d{9}")) {
                return false;
            }
            int sum = -digit(value, 8);
            for (int i = 0; i < 8; i++) {
                sum += (9 - i) * digit(value, i);
            }
            return sum > 0 && sum % 11 == 0;
        }

        @Test
        @DisplayName("published samples validate")
        void publishedSamples() {
            // nl.wikipedia "Burgerservicenummer" examples.
            assertAccepted(NlBsn::isValid, "111222333", "123456782");
        }

        @Test
        @DisplayName("elfproef failures are rejected")
        void invalidRejected() {
            assertRejected(NlBsn::isValid, "111222334", "000000000", "12345678");
        }

        @Test
        @DisplayName("1000 seeded BSNs validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("nl", "NL")), NlBsn::isValid);
        }
    }

    // ── PL ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PL PESEL-style identifier (documented synthetic digits, standard checksum)")
    class PlPesel {

        private static final int[] WEIGHTS                 = { 1, 3, 7, 9, 1, 3, 7, 9, 1, 3, 1 };
        // Month + 0 for 1900-1999, + 20 for 2000-2099, + 40 for 2100-2199, + 60 for 2200-2299, + 80 for 1800-1899.
        private static final int[] CENTURY_BY_MONTH_BLOCK = { 1900, 2000, 2100, 2200, 1800 };

        /**
         * The PESEL check digit (gov.pl "Czym jest numer PESEL"): the 1-3-7-9 weighted sum of all
         * eleven digits (check digit weight 1) is divisible by 10.
         */
        static boolean hasValidChecksum(String value) {
            if (!value.matches("\\d{11}")) {
                return false;
            }
            int sum = 0;
            for (int i = 0; i < 11; i++) {
                sum += WEIGHTS[i] * digit(value, i);
            }
            return sum % 10 == 0;
        }

        /**
         * A full PESEL: the checksum plus a YYMMDD birth date whose month carries the century offset.
         */
        static boolean isValid(String value) {
            if (!hasValidChecksum(value)) {
                return false;
            }
            int encodedMonth = number(value, 2, 4);
            return isCalendarDate(CENTURY_BY_MONTH_BLOCK[encodedMonth / 20] + number(value, 0, 2),
                                  encodedMonth % 20,
                                  number(value, 4, 6));
        }

        @Test
        @DisplayName("published samples validate")
        void publishedSamples() {
            // gov.pl "Czym jest numer PESEL" (born 8 July 1902) and the python-stdnum pl.pesel doctests
            // (born 14 May 1944; born 13 January 2002, month 21 = January + 20).
            assertAccepted(PlPesel::isValid, "02070803628", "44051401359", "02211307589");
            assertAccepted(PlPesel::hasValidChecksum, "02070803628", "44051401359", "02211307589");
        }

        @Test
        @DisplayName("wrong check digits and impossible birth dates are rejected")
        void invalidRejected() {
            // python-stdnum pl.pesel doctests: wrong check digit; correct check digit but invalid birth date.
            assertRejected(PlPesel::isValid, "44051401358", "02381307589", "4405140135");
            assertRejected(PlPesel::hasValidChecksum, "44051401358", "4405140135");
        }

        @Test
        @DisplayName("1000 seeded identifiers carry the standard PESEL checksum over synthetic digits")
        void generatedValidate() {
            // PlNationalIdProvider documents its first ten digits as synthetic, so birth dates are not
            // guaranteed; only the documented check digit is.
            assertAllSatisfy(generated(Locale.of("pl", "PL")), PlPesel::hasValidChecksum);
        }
    }

    // ── RU ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("RU SNILS")
    class RuSnils {

        /**
         * PFR algorithm (Информационное сообщение ПФР от 21.02.2013, п. 8; ru.wikipedia "Страховой номер
         * индивидуального лицевого счёта"): each digit times its position counted from the end; a sum
         * below 100 is the control number, 100 and 101 give 00, and a larger sum is reduced modulo 101
         * with a remainder of 100 giving 00. The provider emits a control number for every value, so
         * the check is applied to all numbers (the PFR only mandates it above 001-001-998).
         */
        static boolean isValid(String value) {
            if (!value.matches("\\d{3}-\\d{3}-\\d{3} \\d{2}")) {
                return false;
            }
            String digits = value.replaceAll("\\D", "");
            int sum = 0;
            for (int position = 1; position <= 9; position++) {
                sum += position * digit(digits, 9 - position);
            }
            int control;
            if (sum < 100) {
                control = sum;
            } else if (sum <= 101) {
                control = 0;
            } else {
                control = sum % 101 == 100 ? 0 : sum % 101;
            }
            return number(digits, 9, 11) == control;
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // ru.wikipedia worked example: 1x9 + 1x8 + 2x7 + 2x6 + 3x5 + 3x4 + 4x3 + 4x2 + 5x1 = 95.
            assertAccepted(RuSnils::isValid, "112-233-445 95");
        }

        @Test
        @DisplayName("wrong control numbers are rejected")
        void invalidRejected() {
            assertRejected(RuSnils::isValid, "112-233-445 96", "112-233-44595");
        }

        @Test
        @DisplayName("1000 seeded SNILS validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("ru", "RU")), RuSnils::isValid);
        }
    }

    // ── KR ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("KR resident registration number")
    class KrResidentRegistrationNumber {

        private static final int[]   WEIGHTS = { 2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5 };
        private static final Pattern RRN     = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})-(\\d)\\d{6}");

        /**
         * Resident registration number (ko.wikipedia "주민등록번호"; en.wikipedia "Resident registration
         * number"): YYMMDD birth date, a sex/century digit (9/0: 1800s, 1/2 and 5/6: 1900s, 3/4 and 7/8:
         * 2000s), and a check digit 11 - (weighted sum mod 11) where 10 becomes 0 and 11 becomes 1.
         */
        static boolean isValid(String value) {
            Matcher matcher = RRN.matcher(value);
            if (!matcher.matches()) {
                return false;
            }
            int century = switch (Integer.parseInt(matcher.group(4))) {
                case 9, 0 -> 1800;
                case 1, 2, 5, 6 -> 1900;
                default -> 2000;
            };
            if (!isCalendarDate(century + Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)),
                                Integer.parseInt(matcher.group(3)))) {
                return false;
            }
            String digits = value.replace("-", "");
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += WEIGHTS[i] * digit(digits, i);
            }
            int check = 11 - sum % 11;
            return digit(digits, 12) == (check >= 10 ? check - 10 : check);
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // python-stdnum kr.rrn doctest (born 13 October 1897, sex/century digit 9).
            assertAccepted(KrResidentRegistrationNumber::isValid, "971013-9019902");
        }

        @Test
        @DisplayName("wrong check digits and impossible dates are rejected")
        void invalidRejected() {
            assertRejected(KrResidentRegistrationNumber::isValid, "971013-9019903", "971313-9019902");
        }

        @Test
        @DisplayName("1000 seeded RRNs validate with documented 1980-1999 dates")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("ko", "KR")), id -> isValid(id)
                                                                   && id.matches("[89]\\d{5}-[12]\\d{6}"));
        }
    }

    // ── TR ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("TR T.C. Kimlik No")
    class TrKimlikNo {

        /**
         * T.C. Kimlik No (tr.wikipedia "Türkiye Cumhuriyeti Kimlik Numarası"): eleven digits without a
         * leading zero; digit 10 is the ones digit of 7 x (digits 1, 3, 5, 7, 9) + 9 x (digits 2, 4, 6,
         * 8) and digit 11 is the ones digit of the sum of the first ten digits.
         */
        static boolean isValid(String value) {
            if (!value.matches("[1-9]\\d{10}")) {
                return false;
            }
            int odd = digit(value, 0) + digit(value, 2) + digit(value, 4) + digit(value, 6) + digit(value, 8);
            int even = digit(value, 1) + digit(value, 3) + digit(value, 5) + digit(value, 7);
            int firstTen = odd + even + digit(value, 9);
            return (7 * odd + 9 * even) % 10 == digit(value, 9) && firstTen % 10 == digit(value, 10);
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // python-stdnum tr.tckimlik doctest.
            assertAccepted(TrKimlikNo::isValid, "17291716060");
        }

        @Test
        @DisplayName("wrong check digits and leading zeros are rejected")
        void invalidRejected() {
            // python-stdnum tr.tckimlik doctests.
            assertRejected(TrKimlikNo::isValid, "17291716050", "07291716092");
        }

        @Test
        @DisplayName("1000 seeded numbers validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("tr", "TR")), TrKimlikNo::isValid);
        }
    }

    // ── SE ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SE personnummer")
    class SePersonnummer {

        private static final Pattern PNR = Pattern.compile("(\\d{2})?(\\d{2})(\\d{2})(\\d{2})-(\\d{4})");

        /**
         * Swedish personal identity number (Skatteverket; Wikipedia "Personal identity number
         * (Sweden)"): a real birth date and a Luhn check digit over the ten-digit form YYMMDDNNNC.
         */
        static boolean isValid(String value) {
            Matcher matcher = PNR.matcher(value);
            if (!matcher.matches()) {
                return false;
            }
            int twoDigitYear = Integer.parseInt(matcher.group(2));
            int month = Integer.parseInt(matcher.group(3));
            int day = Integer.parseInt(matcher.group(4));
            boolean realDate = matcher.group(1) == null
                               ? isDateInEitherCentury(twoDigitYear, month, day)
                               : isCalendarDate(Integer.parseInt(matcher.group(1) + matcher.group(2)), month, day);
            return realDate && LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(
                matcher.group(2) + matcher.group(3) + matcher.group(4) + matcher.group(5));
        }

        @Test
        @DisplayName("published samples validate")
        void publishedSamples() {
            // Wikipedia "Personal identity number (Sweden)" worked examples.
            assertAccepted(SePersonnummer::isValid, "811228-9874", "670919-9530", "19811228-9874");
        }

        @Test
        @DisplayName("wrong check digits and impossible dates are rejected")
        void invalidRejected() {
            assertRejected(SePersonnummer::isValid, "811228-9875", "19810229-9874", "8112289874");
        }

        @Test
        @DisplayName("1000 seeded personnummer validate with documented 1950-1999 dates")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("sv", "SE")), id -> id.matches("19[5-9]\\d{5}-\\d{4}")
                                                                   && isValid(id));
        }
    }

    // ── NO ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("NO fødselsnummer (documented fake format, no checksum claim)")
    class NoFodselsnummer {

        /**
         * The provider documents a DDMMYYNNNNN fake-format value that does not claim official control
         * digits, so only the shape and a real birth date are validated.
         */
        static boolean isValid(String value) {
            return value.matches("\\d{11}")
                   && isDateInEitherCentury(number(value, 4, 6), number(value, 2, 4), number(value, 0, 2));
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // python-stdnum no.fodselsnummer doctest (born 15 October 1986).
            assertAccepted(NoFodselsnummer::isValid, "15108695088");
        }

        @Test
        @DisplayName("impossible dates and wrong shapes are rejected")
        void invalidRejected() {
            assertRejected(NoFodselsnummer::isValid, "32108695088", "15138695088", "1510869508");
        }

        @Test
        @DisplayName("1000 seeded values keep the documented shape with 1950-1999 dates")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("nb", "NO")), id -> isValid(id)
                                                                   && number(id, 4, 6) >= 50
                                                                   && isCalendarDate(1900 + number(id, 4, 6),
                                                                                     number(id, 2, 4),
                                                                                     number(id, 0, 2)));
        }
    }

    // ── CZ ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CZ rodné číslo (documented fixture shape, no checksum claim)")
    class CzRodneCislo {

        private static final Pattern NUMBER = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})/\\d{4}");

        /**
         * The provider documents a YYMMDD/XXXX shaped value without the mod-11 check, so only the shape
         * and a real birth date are validated; month offsets for women (+50) and the 2004 extension (+20)
         * are decoded as described by cs.wikipedia "Rodné číslo".
         */
        static boolean isValid(String value) {
            Matcher matcher = NUMBER.matcher(value);
            if (!matcher.matches()) {
                return false;
            }
            int month = Integer.parseInt(matcher.group(2)) % 50;
            if (month > 20) {
                month -= 20;
            }
            return isDateInEitherCentury(Integer.parseInt(matcher.group(1)), month, Integer.parseInt(matcher.group(3)));
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // python-stdnum cz.rc doctest (born 19 March 1971).
            assertAccepted(CzRodneCislo::isValid, "710319/2745");
        }

        @Test
        @DisplayName("impossible dates and wrong shapes are rejected")
        void invalidRejected() {
            // python-stdnum cz.rc doctest "1103492745" has an invalid birth date.
            assertRejected(CzRodneCislo::isValid, "110349/2745", "7103192745", "710319/274");
        }

        @Test
        @DisplayName("1000 seeded values keep the documented shape with 1950-1999 dates")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("cs", "CZ")), id -> isValid(id)
                                                                   && number(id, 0, 2) >= 50
                                                                   && isCalendarDate(1900 + number(id, 0, 2),
                                                                                     number(id, 2, 4),
                                                                                     number(id, 4, 6)));
        }
    }

    // ── SA ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SA national ID (documented citizen shape, no checksum claim)")
    class SaNationalId {

        /** The provider documents ten digits beginning with 1 (the citizen range) and no checksum. */
        static boolean isValid(String value) {
            return value.matches("1\\d{9}");
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // National ID example from the SaudiId-validator README (github.com/aalgrou/SaudiId-validator).
            assertAccepted(SaNationalId::isValid, "1087654321");
        }

        @Test
        @DisplayName("non-citizen ranges and wrong lengths are rejected")
        void invalidRejected() {
            assertRejected(SaNationalId::isValid, "2087654321", "108765432");
        }

        @Test
        @DisplayName("1000 seeded values keep the documented shape")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("ar", "SA")), SaNationalId::isValid);
        }
    }

    // ── IN ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("IN Aadhaar")
    class InAadhaar {

        /**
         * Aadhaar (UIDAI "A UID Numbering Scheme"): twelve digits, never starting with 0 or 1, the last
         * being a Verhoeff check digit (validated with Apache Commons Validator's implementation).
         */
        static boolean isValid(String value) {
            return value.matches("[2-9]\\d{11}") && VerhoeffCheckDigit.VERHOEFF_CHECK_DIGIT.isValid(value);
        }

        @Test
        @DisplayName("published sample validates")
        void publishedSample() {
            // python-stdnum in_.aadhaar doctest ("2341 2341 2346").
            assertAccepted(InAadhaar::isValid, "234123412346");
        }

        @Test
        @DisplayName("wrong check digits and 0/1 prefixes are rejected")
        void invalidRejected() {
            // python-stdnum in_.aadhaar doctests.
            assertRejected(InAadhaar::isValid, "234123412347", "123412341234");
        }

        @Test
        @DisplayName("1000 seeded values validate")
        void generatedValidate() {
            assertAllSatisfy(generated(Locale.of("hi", "IN")), InAadhaar::isValid);
        }
    }
}
