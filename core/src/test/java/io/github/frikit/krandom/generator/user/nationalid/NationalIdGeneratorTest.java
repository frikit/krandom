/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NationalIdGenerator")
class NationalIdGeneratorTest {

    @Test
    @DisplayName("built-in providers cover every SupportedLocale")
    void builtInProvidersCoverEverySupportedLocale() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            assertTrue(NationalIdRegistry.isRegistered(locale), supportedLocale.name());
            String nationalId = new NationalIdGenerator(locale, 123L).generate();
            assertNotNull(nationalId, supportedLocale.name());
            assertFalse(nationalId.isBlank(), supportedLocale.name());
        }
    }

    @Test
    @DisplayName("default configured national-ID generation fails closed")
    void defaultConfiguredGenerationFailsClosed() {
        NationalIdGenerator generator = new NationalIdGenerator(GeneratorConfig.builder()
                                                                                .locale(Locale.US)
                                                                                .seed(123L)
                                                                                .build());

        IllegalStateException error = assertThrows(IllegalStateException.class, generator::generate);

        assertTrue(error.getMessage().contains("nationalIdSafetyPolicy"));
    }

    @Test
    @DisplayName("explicit realistic policy preserves prior compatibility output")
    void explicitRealisticPolicyPreservesCompatibilityOutput() {
        NationalIdGenerator generator = new NationalIdGenerator(GeneratorConfig.builder()
                                                                                .locale(Locale.US)
                                                                                .seed(123L)
                                                                                .nationalIdSafetyPolicy(
                                                                                    NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                                                .build());

        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("additional SupportedLocale national IDs use documented fake formats")
    void additionalSupportedLocaleNationalIdFormats() {
        Map<Locale, Pattern> patterns = Map.ofEntries(
            Map.entry(Locale.of("nl", "NL"), Pattern.compile("^\\d{9}$")),
            Map.entry(Locale.of("pl", "PL"), Pattern.compile("^\\d{11}$")),
            Map.entry(Locale.of("ru", "RU"), Pattern.compile("^\\d{3}-\\d{3}-\\d{3} \\d{2}$")),
            Map.entry(Locale.of("ko", "KR"), Pattern.compile("^\\d{6}-\\d{7}$")),
            Map.entry(Locale.of("tr", "TR"), Pattern.compile("^[1-9]\\d{10}$")),
            Map.entry(Locale.of("sv", "SE"), Pattern.compile("^\\d{8}-\\d{4}$")),
            Map.entry(Locale.of("nb", "NO"), Pattern.compile("^\\d{11}$")),
            Map.entry(Locale.of("cs", "CZ"), Pattern.compile("^\\d{6}/\\d{4}$")),
            Map.entry(Locale.of("ar", "SA"), Pattern.compile("^1\\d{9}$")),
            Map.entry(Locale.of("hi", "IN"), Pattern.compile("^[2-9]\\d{11}$"))
        );

        for (Map.Entry<Locale, Pattern> entry : patterns.entrySet()) {
            String nationalId = new NationalIdGenerator(entry.getKey(), 123L).generate();
            assertTrue(entry.getValue().matcher(nationalId).matches(), entry.getKey() + ": " + nationalId);
        }
    }

    // ── US (SSN) ──────────────────────────────────────────────────────────────


    @Nested
    @DisplayName("US SSN")
    class UsTests {

        private static final Pattern WITH_DASHES    = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
        private static final Pattern WITHOUT_DASHES = Pattern.compile("^\\d{9}$");
        private static final Pattern LAST_FOUR      = Pattern.compile("^\\d{4}$");

        @Test
        @DisplayName("default format AAA-GG-SSSS")
        void defaultFormat() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.US);
            for (int i = 0; i < 50; i++) {
                assertTrue(WITH_DASHES.matcher(gen.generate()).matches());
            }
        }

        @Test
        @DisplayName("valid ranges: area 001-899 (not 666), group 01-99, serial 0001-9999")
        void validRanges() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.US);
            for (int i = 0; i < 200; i++) {
                String ssn = gen.generate();
                String[] p = ssn.split("-");
                int area = Integer.parseInt(p[0]);
                assertTrue(area >= 1 && area <= 899);
                assertNotEquals(666, area, "Area 666 must never appear");
                assertTrue(Integer.parseInt(p[1]) >= 1 && Integer.parseInt(p[1]) <= 99);
                assertTrue(Integer.parseInt(p[2]) >= 1 && Integer.parseInt(p[2]) <= 9999);
            }
        }

        @Test
        @DisplayName("area 666 is never generated; areas below and above both appear")
        void area666NeverGenerated() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.US, 0L);
            Set<String> areas = new HashSet<>();
            for (int i = 0; i < 5000; i++) {
                areas.add(gen.generate().substring(0, 3));
            }
            assertFalse(areas.contains("666"));
            assertTrue(areas.stream().anyMatch(a -> Integer.parseInt(a) < 666));
            assertTrue(areas.stream().anyMatch(a -> Integer.parseInt(a) > 666));
        }

        @Test
        @DisplayName("withoutDashes() produces 9-digit string")
        void withoutDashes() {
            UsNationalIdProvider provider = new UsNationalIdProvider().withoutDashes();
            Random rng = new Random(1L);
            for (int i = 0; i < 50; i++) {
                assertTrue(WITHOUT_DASHES.matcher(provider.generate(rng)).matches());
            }
        }

        @Test
        @DisplayName("lastFourOnly() produces 4-digit string in [0001, 9999]")
        void lastFourOnly() {
            UsNationalIdProvider provider = new UsNationalIdProvider().lastFourOnly();
            Random rng = new Random(1L);
            for (int i = 0; i < 50; i++) {
                String s = provider.generate(rng);
                assertTrue(LAST_FOUR.matcher(s).matches());
                int v = Integer.parseInt(s);
                assertTrue(v >= 1 && v <= 9999);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.US, 42L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.US, 42L);
            assertEquals(g1.generateList(30), g2.generateList(30));
        }
    }

    // ── GB (NI) ───────────────────────────────────────────────────────────────


    @Nested
    @DisplayName("GB National Insurance")
    class GbTests {

        private static final Pattern NI_FORMAT =
            Pattern.compile("^[A-Z]{2} \\d{2} \\d{2} \\d{2} [A-D]$");

        @Test
        @DisplayName("format: XX NN NN NN X")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.UK);
            for (int i = 0; i < 200; i++) {
                String ni = gen.generate();
                assertTrue(NI_FORMAT.matcher(ni).matches(), "Bad NI format: " + ni);
            }
        }

        @Test
        @DisplayName("disallowed prefixes never appear")
        void disallowedPrefixes() {
            Set<String> disallowed = Set.of("BG", "GB", "NK", "KN", "NT", "TN", "ZZ");
            NationalIdGenerator gen = new NationalIdGenerator(Locale.UK, 1L);
            for (int i = 0; i < 10_000; i++) {
                String prefix = gen.generate().substring(0, 2);
                assertFalse(disallowed.contains(prefix), "Disallowed prefix: " + prefix);
            }
        }

        @Test
        @DisplayName("both disallowed-pair branch sides are covered over 10000 samples")
        void disallowedPairRetryBranch() {
            // By generating many, we statistically guarantee the retry loop is exercised.
            NationalIdGenerator gen = new NationalIdGenerator(Locale.UK, 77L);
            Set<String> prefixes = new HashSet<>();
            for (int i = 0; i < 10_000; i++) {
                prefixes.add(gen.generate().substring(0, 2));
            }
            // Multiple distinct valid prefixes must appear, proving the loop proceeds normally
            assertTrue(prefixes.size() > 50, "Expected many distinct prefixes, got: " + prefixes.size());
        }

        @Test
        @DisplayName("suffix letter is always A, B, C, or D")
        void suffixLetter() {
            Set<Character> valid = Set.of('A', 'B', 'C', 'D');
            NationalIdGenerator gen = new NationalIdGenerator(Locale.UK, 2L);
            for (int i = 0; i < 200; i++) {
                String ni = gen.generate();
                char suffix = ni.charAt(ni.length() - 1);
                assertTrue(valid.contains(suffix), "Invalid suffix: " + suffix);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.UK, 99L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.UK, 99L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── AU (TFN) ──────────────────────────────────────────────────────────────


    @Nested
    @DisplayName("AU Tax File Number")
    class AuTests {

        private static final Pattern TFN_FORMAT = Pattern.compile("^\\d{3} \\d{3} \\d{3}$");

        @Test
        @DisplayName("format: NNN NNN NNN")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("en", "AU"));
            for (int i = 0; i < 200; i++) {
                assertTrue(TFN_FORMAT.matcher(gen.generate()).matches());
            }
        }

        @Test
        @DisplayName("first digit is never 0")
        void firstDigitNonZero() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("en", "AU"), 3L);
            for (int i = 0; i < 200; i++) {
                assertNotEquals('0', gen.generate().charAt(0));
            }
        }

        @Test
        @DisplayName("computeRemainder returns 0 for weighted-sum-divisible input")
        void computeRemainderZeroBranch() {
            // Weights: 1,4,3,7,5,8,6,9,10
            // Construct digits so sum % 11 == 0
            // Use: all zeros except first = 1 (sum = 1*1 = 1, not 0); let's find a valid set
            // 11*1=11 → need sum=11: d0*1 + d1*4 = 11 → d0=7,d1=1: 7+4=11 ✓, rest 0
            int[] digits = { 7, 1, 0, 0, 0, 0, 0, 0, 0 };
            assertEquals(0, AuNationalIdProvider.computeRemainder(digits));
        }

        @Test
        @DisplayName("computeRemainder returns non-zero for valid TFN digit array")
        void computeRemainderNonZero() {
            // d0=1, rest 0: sum = 1*1 = 1 → remainder = 1
            int[] digits = { 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            assertEquals(1, AuNationalIdProvider.computeRemainder(digits));
        }

        @Test
        @DisplayName("generated TFNs always have non-zero remainder")
        void checksumValid() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("en", "AU"), 5L);
            for (int i = 0; i < 200; i++) {
                String tfn = gen.generate().replace(" ", "");
                int[] digits = new int[9];
                for (int j = 0; j < 9; j++) digits[j] = tfn.charAt(j) - '0';
                assertNotEquals(0, AuNationalIdProvider.computeRemainder(digits),
                                "TFN checksum remainder should not be 0: " + tfn);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.of("en", "AU"), 7L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.of("en", "AU"), 7L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── FR (NIR) ──────────────────────────────────────────────────────────────


    @Nested
    @DisplayName("FR NIR")
    class FrTests {

        // Format: G YY MM DD PPP OOO KK (no spaces), 15 chars
        private static final Pattern NIR_FORMAT = Pattern.compile("^[12]\\d{14}$");

        @Test
        @DisplayName("format: 15-digit string starting with 1 or 2")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.FRANCE);
            for (int i = 0; i < 200; i++) {
                assertTrue(NIR_FORMAT.matcher(gen.generate()).matches());
            }
        }

        @Test
        @DisplayName("control key is in range [01, 97]")
        void controlKeyRange() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.FRANCE, 10L);
            for (int i = 0; i < 200; i++) {
                String nir = gen.generate();
                int key = Integer.parseInt(nir.substring(13, 15));
                assertTrue(key >= 1 && key <= 97, "Key out of range: " + key);
            }
        }

        @Test
        @DisplayName("control key satisfies 97 - (N mod 97) formula")
        void controlKeyValid() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.FRANCE, 11L);
            for (int i = 0; i < 200; i++) {
                String nir = gen.generate();
                long n13 = Long.parseLong(nir.substring(0, 13));
                int expectedKey = (int) (97 - (n13 % 97));
                int actualKey = Integer.parseInt(nir.substring(13, 15));
                assertEquals(expectedKey, actualKey, "Bad key for NIR: " + nir);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.FRANCE, 20L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.FRANCE, 20L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── DE (Steuer-ID) ────────────────────────────────────────────────────────


    @Nested
    @DisplayName("DE Steuer-ID")
    class DeTests {

        private static final Pattern STEUER_FORMAT = Pattern.compile("^[1-9]\\d{10}$");

        @Test
        @DisplayName("format: 11 digits, first non-zero")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.GERMANY);
            for (int i = 0; i < 200; i++) {
                String id = gen.generate();
                assertTrue(STEUER_FORMAT.matcher(id).matches(), "Bad format: " + id);
            }
        }

        @Test
        @DisplayName("check digit satisfies ISO 7064 Mod 11,10")
        void checkDigitValid() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.GERMANY, 30L);
            for (int i = 0; i < 200; i++) {
                String id = gen.generate();
                int[] digits = new int[11];
                for (int j = 0; j < 11; j++) digits[j] = id.charAt(j) - '0';
                int expected = DeNationalIdProvider.computeCheckDigit(digits);
                assertEquals(expected, digits[10], "Wrong check digit in: " + id);
            }
        }

        @Test
        @DisplayName("computeCheckDigit sum==0 branch: sum=(0+product)%10==0 triggers sum=10")
        void sumZeroBranch() {
            // product starts at 10; first digit 5: sum=(5+10)%10=5→product=(10)%11=10
            // second digit 5: sum=(5+10)%10=5→product=10
            // ... if we force sum to be 0: (d+product)%10==0 when d+product is divisible by 10
            // Start: product=10; d0=0: sum=(0+10)%10=0 → sum=10 (branch hit!) → product=(20)%11=9
            int[] digits = new int[11];
            // digits[0]=0 forces sum=0 branch on first iteration
            // But wait, the first digit of Steuer-ID must be 1-9. In the generate() method we
            // enforce this. But computeCheckDigit() itself just takes any int array.
            // Let's use digits[0]=0 to trigger the branch directly.
            digits[0] = 0; // forces (0+10)%10=0 → sum=10 branch
            // Fill rest with 0s
            int result = DeNationalIdProvider.computeCheckDigit(digits);
            // product after digit[0]: sum=(0+10)%10=0→10, product=(20)%11=9
            // digits[1..9]=0: sum=(0+9)%10=9→product=(18)%11=7; sum=(0+7)%10=7→product=(14)%11=3
            // sum=(0+3)%10=3→product=6; sum=(0+6)%10=6→product=12%11=1;
            // sum=(0+1)%10=1→product=2; sum=(0+2)%10=2→product=4; sum=(0+4)%10=4→product=8;
            // sum=(0+8)%10=8→product=16%11=5; sum=(0+5)%10=5→product=10%11=10
            // check_digit=11-10=1
            assertEquals(1, result);
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.GERMANY, 40L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.GERMANY, 40L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── JP (My Number) ────────────────────────────────────────────────────────


    @Nested
    @DisplayName("JP My Number")
    class JpTests {

        private static final Pattern MY_NUMBER_FORMAT = Pattern.compile("^[1-9]\\d{11}$");

        @Test
        @DisplayName("format: 12 digits, first non-zero")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.JAPAN);
            for (int i = 0; i < 200; i++) {
                String id = gen.generate();
                assertTrue(MY_NUMBER_FORMAT.matcher(id).matches(), "Bad format: " + id);
            }
        }

        @Test
        @DisplayName("check digit is valid (Q < 10 branch)")
        void checkDigitQBelow10() {
            // Construct 11 digits where Q = 11 - (sum%11) < 10
            // Use digits all 0 except first=1: sum=1*6=6, Q=11-6=5 (<10)
            int[] digits = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            int cd = JpNationalIdProvider.computeCheckDigit(digits);
            assertEquals(5, cd);
        }

        @Test
        @DisplayName("check digit is 0 when Q >= 10 (both Q=10 and Q=11 cases)")
        void checkDigitQGeq10() {
            // Q=11-(sum%11)>=10 when sum%11==1 (Q=10) or sum%11==0 (Q=11)
            // Case Q=10: sum%11=1 → need sum=1; weights={6,5,4,...}: d0*6=6≠1 unless d0=0
            // With d0=0 (first weight=6): sum starts at 0; let's try:
            // d[0]=0,d[1]=0,...: sum=0, Q=11-0=11 → cd=0
            int[] zeros = new int[11];
            assertEquals(0, JpNationalIdProvider.computeCheckDigit(zeros)); // Q=11→0

            // Q=10: sum%11=1 → weights={6,5,4,3,2,7,6,5,4,3,2}
            // d[10]=1: contribution=1*2=2; still need sum%11=1
            // sum=2: 2%11=2≠1. Try d[10]=1,d[9]=1: sum=3%11=3≠1
            // Try d[10]=6 (6*2=12): 12%11=1 ✓
            int[] digits2 = new int[11];
            digits2[10] = 6;
            assertEquals(0, JpNationalIdProvider.computeCheckDigit(digits2)); // Q=10→0
        }

        @Test
        @DisplayName("generated My Numbers always have valid check digit")
        void checksumValid() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.JAPAN, 50L);
            for (int i = 0; i < 200; i++) {
                String id = gen.generate();
                int[] digits = new int[11];
                for (int j = 0; j < 11; j++) digits[j] = id.charAt(j) - '0';
                int expected = JpNationalIdProvider.computeCheckDigit(digits);
                assertEquals(expected, id.charAt(11) - '0', "Wrong check digit in: " + id);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.JAPAN, 55L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.JAPAN, 55L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── ES (DNI) ──────────────────────────────────────────────────────────────


    @Nested
    @DisplayName("ES DNI")
    class EsTests {

        private static final Pattern DNI_FORMAT      = Pattern.compile("^\\d{8}[A-Z]$");
        private static final Pattern DNI_DASH_FORMAT = Pattern.compile("^\\d{8}-[A-Z]$");
        private static final String  LETTER_MAP      = "TRWAGMYFPDXBNJZSQVHLCKE";

        @Test
        @DisplayName("default format NNNNNNNNA")
        void defaultFormat() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("es", "ES"));
            for (int i = 0; i < 200; i++) {
                assertTrue(DNI_FORMAT.matcher(gen.generate()).matches());
            }
        }

        @Test
        @DisplayName("withDash() format NNNNNNNN-A")
        void withDashFormat() {
            EsNationalIdProvider provider = new EsNationalIdProvider().withDash();
            Random rng = new Random(1L);
            for (int i = 0; i < 50; i++) {
                assertTrue(DNI_DASH_FORMAT.matcher(provider.generate(rng)).matches());
            }
        }

        @Test
        @DisplayName("check letter is correct modulo 23")
        void checkLetterValid() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("es", "ES"), 60L);
            for (int i = 0; i < 200; i++) {
                String dni = gen.generate();
                int n = Integer.parseInt(dni.substring(0, 8));
                char expected = LETTER_MAP.charAt(n % 23);
                assertEquals(expected, dni.charAt(8), "Wrong letter in DNI: " + dni);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.of("es", "ES"), 70L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.of("es", "ES"), 70L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── IT (Codice Fiscale) ───────────────────────────────────────────────────


    @Nested
    @DisplayName("IT Codice Fiscale")
    class ItTests {

        private static final Pattern CF_FORMAT = Pattern.compile("^[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]$");

        @Test
        @DisplayName("format: 16 chars matching pattern")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.ITALY);
            for (int i = 0; i < 200; i++) {
                String cf = gen.generate();
                assertTrue(CF_FORMAT.matcher(cf).matches(), "Bad CF format: " + cf);
            }
        }

        @Test
        @DisplayName("day field: male 01-31, female 41-71 — both appear over 1000 samples")
        void dayBothGenders() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.ITALY, 80L);
            boolean seenMale = false, seenFemale = false;
            for (int i = 0; i < 1000; i++) {
                String cf = gen.generate();
                int day = Integer.parseInt(cf.substring(9, 11));
                if (day >= 1 && day <= 31) seenMale = true;
                if (day >= 41 && day <= 71) seenFemale = true;
                if (seenMale && seenFemale) break;
            }
            assertTrue(seenMale, "Male day range [1,31] should appear");
            assertTrue(seenFemale, "Female day range [41,71] should appear");
        }

        @Test
        @DisplayName("month code is always from ABCDEHLMPRST")
        void monthCode() {
            Set<Character> validMonths = new HashSet<>();
            for (char c : "ABCDEHLMPRST".toCharArray()) validMonths.add(c);
            NationalIdGenerator gen = new NationalIdGenerator(Locale.ITALY, 81L);
            for (int i = 0; i < 200; i++) {
                char month = gen.generate().charAt(8);
                assertTrue(validMonths.contains(month), "Invalid month code: " + month);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.ITALY, 85L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.ITALY, 85L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── BR (CPF) ──────────────────────────────────────────────────────────────


    @Nested
    @DisplayName("BR CPF")
    class BrTests {

        private static final Pattern CPF_FORMAT = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
        private static final Pattern CPF_PLAIN  = Pattern.compile("^\\d{11}$");

        @Test
        @DisplayName("default format NNN.NNN.NNN-NN")
        void defaultFormat() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("pt", "BR"));
            for (int i = 0; i < 200; i++) {
                assertTrue(CPF_FORMAT.matcher(gen.generate()).matches());
            }
        }

        @Test
        @DisplayName("withoutFormatting() produces 11-digit plain string")
        void withoutFormatting() {
            BrNationalIdProvider provider = new BrNationalIdProvider().withoutFormatting();
            Random rng = new Random(1L);
            for (int i = 0; i < 50; i++) {
                assertTrue(CPF_PLAIN.matcher(provider.generate(rng)).matches());
            }
        }

        @Test
        @DisplayName("verifier digits are correct")
        void verifierDigitsValid() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.of("pt", "BR"), 90L);
            for (int i = 0; i < 200; i++) {
                String cpf = gen.generate().replaceAll("[^\\d]", "");
                int[] d = new int[9];
                for (int j = 0; j < 9; j++) d[j] = cpf.charAt(j) - '0';
                int v1 = BrNationalIdProvider.computeVerifier(d, 10);
                int[] d10 = new int[10];
                System.arraycopy(d, 0, d10, 0, 9);
                d10[9] = v1;
                int v2 = BrNationalIdProvider.computeVerifier(d10, 11);
                assertEquals(v1, cpf.charAt(9) - '0', "Wrong v1 in CPF: " + cpf);
                assertEquals(v2, cpf.charAt(10) - '0', "Wrong v2 in CPF: " + cpf);
            }
        }

        @Test
        @DisplayName("computeVerifier sum%11==0 and sum%11==1 both produce 0")
        void computeVerifierZeroBranches() {
            // sum%11=0 → 11-0=11 → >=10 → return 0
            // sum%11=1 → 11-1=10 → >=10 → return 0
            // For sum=0: all digits zero with any startWeight → sum=0
            int[] zeros = new int[9];
            assertEquals(0, BrNationalIdProvider.computeVerifier(zeros, 10)); // 11-0=11≥10→0

            // For sum%11=1: e.g., d[0]=1, startWeight=10 → 1*10=10 → 10%11=10 → 11-10=1 (normal)
            // Need sum%11=1: startWeight=1, d[0]=1: 1*1=1 → 1%11=1 → 11-1=10 ≥ 10 → 0
            int[] one = { 1 };
            assertEquals(0, BrNationalIdProvider.computeVerifier(one, 1)); // 11-1=10≥10→0
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.of("pt", "BR"), 95L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.of("pt", "BR"), 95L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── CN (Resident ID) ──────────────────────────────────────────────────────


    @Nested
    @DisplayName("CN Resident ID")
    class CnTests {

        private static final Pattern CN_FORMAT = Pattern.compile("^\\d{17}[0-9X]$");

        @Test
        @DisplayName("format: 18 chars, last may be X")
        void format() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.CHINA);
            for (int i = 0; i < 200; i++) {
                String id = gen.generate();
                assertTrue(CN_FORMAT.matcher(id).matches(), "Bad CN ID format: " + id);
            }
        }

        @Test
        @DisplayName("region code is in range [100000, 658999]")
        void regionRange() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.CHINA, 100L);
            for (int i = 0; i < 200; i++) {
                int region = Integer.parseInt(gen.generate().substring(0, 6));
                assertTrue(region >= 100000 && region <= 658999, "Region out of range: " + region);
            }
        }

        @Test
        @DisplayName("birth year is in range [1940, 2005]")
        void birthYearRange() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.CHINA, 101L);
            for (int i = 0; i < 200; i++) {
                int year = Integer.parseInt(gen.generate().substring(6, 10));
                assertTrue(year >= 1940 && year <= 2005, "Year out of range: " + year);
            }
        }

        @Test
        @DisplayName("check character is valid ISO 7064 Mod 11,2 result")
        void checkCharValid() {
            int[] weights = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
            String checkChars = "10X98765432";
            NationalIdGenerator gen = new NationalIdGenerator(Locale.CHINA, 102L);
            for (int i = 0; i < 200; i++) {
                String id = gen.generate();
                int sum = 0;
                for (int j = 0; j < 17; j++) {
                    sum += (id.charAt(j) - '0') * weights[j];
                }
                char expected = checkChars.charAt((12 - (sum % 11)) % 11);
                assertEquals(expected, id.charAt(17), "Wrong check char in: " + id);
            }
        }

        @Test
        @DisplayName("seeded generator produces reproducible output")
        void seededReproducibility() {
            NationalIdGenerator g1 = new NationalIdGenerator(Locale.CHINA, 110L);
            NationalIdGenerator g2 = new NationalIdGenerator(Locale.CHINA, 110L);
            assertEquals(g1.generateList(20), g2.generateList(20));
        }
    }

    // ── Registry / extensibility ──────────────────────────────────────────────


    @Nested
    @DisplayName("NationalIdRegistry")
    class RegistryTests {

        @Test
        @DisplayName("custom provider for new locale is used by generator")
        void customLocaleRegistration() {
            Locale esperanto = Locale.of("eo", "EO");
            NationalIdRegistry.register(new NationalIdProvider() {

                @Override
                public Locale getLocale() {
                    return esperanto;
                }

                @Override
                public String generate(Random r) {
                    return "EO-TEST";
                }
            });
            NationalIdGenerator gen = new NationalIdGenerator(esperanto);
            assertEquals("EO-TEST", gen.generate());
        }

        @Test
        @DisplayName("custom provider overrides built-in locale")
        void customProviderOverridesBuiltIn() {
            Locale us = Locale.US;
            NationalIdRegistry.register(new NationalIdProvider() {

                @Override
                public Locale getLocale() {
                    return us;
                }

                @Override
                public String generate(Random r) {
                    return "CUSTOM-US";
                }
            });
            NationalIdGenerator gen = new NationalIdGenerator(us);
            assertEquals("CUSTOM-US", gen.generate());
            // Restore
            NationalIdRegistry.register(new UsNationalIdProvider());
        }

        @Test
        @DisplayName("language-only fallback works")
        void languageOnlyFallback() {
            // "en_ZZ" should fall back to the "en" entry seeded from UsNationalIdProvider
            Locale enZz = Locale.of("en", "ZZ");
            assertTrue(NationalIdRegistry.isRegistered(enZz));
            NationalIdGenerator gen = new NationalIdGenerator(enZz);
            assertNotNull(gen.generate());
        }

        @Test
        @DisplayName("unsupported locale throws UnsupportedOperationException")
        void unsupportedLocaleThrows() {
            UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> new NationalIdGenerator(Locale.of("xx", "YY")));
            assertTrue(ex.getMessage().contains("not supported"));
        }

        @Test
        @DisplayName("register rejects null provider")
        void registerRejectsNull() {
            assertThrows(NullPointerException.class, () -> NationalIdRegistry.register(null));
        }

        @Test
        @DisplayName("isRegistered returns false for null locale")
        void isRegisteredNullReturnsFalse() {
            assertFalse(NationalIdRegistry.isRegistered(null));
        }

        @Test
        @DisplayName("forLocale returns null for null locale")
        void forLocaleNullReturnsNull() {
            assertNull(NationalIdRegistry.forLocale(null));
        }

        @Test
        @DisplayName("forLocale returns null for completely unknown locale")
        void forLocaleUnknownReturnsNull() {
            assertNull(NationalIdRegistry.forLocale(Locale.of("xx", "YY")));
        }

        @Test
        @DisplayName("register with language-only locale sets the language-level entry")
        void registerLanguageOnlyLocale() {
            Locale langOnly = Locale.of("tl");
            NationalIdRegistry.register(new NationalIdProvider() {

                @Override
                public Locale getLocale() {
                    return langOnly;
                }

                @Override
                public String generate(Random r) {
                    return "TL-ONLY";
                }
            });
            assertTrue(NationalIdRegistry.isRegistered(langOnly));
            NationalIdProvider found = NationalIdRegistry.forLocale(langOnly);
            assertNotNull(found);
            assertEquals("TL-ONLY", found.generate(null));
        }

        @Test
        @DisplayName("isRegistered with language-only locale checks language key")
        void isRegisteredLanguageOnlyLocale() {
            // "en" is registered as language fallback from UsNationalIdProvider
            assertTrue(NationalIdRegistry.isRegistered(Locale.of("en")));
        }

        @Test
        @DisplayName("isRegistered returns true for exact built-in locale key")
        void isRegisteredExactLocale() {
            assertTrue(NationalIdRegistry.isRegistered(Locale.US));
        }

        @Test
        @DisplayName("forLocale with language-only locale returns language-level entry")
        void forLocaleLanguageOnlyLocale() {
            // "en" is registered as language fallback from UsNationalIdProvider
            NationalIdProvider provider = NationalIdRegistry.forLocale(Locale.of("en"));
            assertNotNull(provider);
        }

        @Test
        @DisplayName("registeredKeys contains all built-in locales")
        void registeredKeysContainsBuiltIns() {
            Set<String> keys = NationalIdRegistry.registeredKeys();
            assertTrue(keys.contains("en_US"));
            assertTrue(keys.contains("en_GB"));
            assertTrue(keys.contains("en_AU"));
            assertTrue(keys.contains("fr_FR"));
            assertTrue(keys.contains("de_DE"));
            assertTrue(keys.contains("ja_JP"));
            assertTrue(keys.contains("es_ES"));
            assertTrue(keys.contains("it_IT"));
            assertTrue(keys.contains("pt_BR"));
            assertTrue(keys.contains("zh_CN"));
            assertTrue(keys.contains("nl_NL"));
            assertTrue(keys.contains("pl_PL"));
            assertTrue(keys.contains("ru_RU"));
            assertTrue(keys.contains("ko_KR"));
            assertTrue(keys.contains("tr_TR"));
            assertTrue(keys.contains("sv_SE"));
            assertTrue(keys.contains("nb_NO"));
            assertTrue(keys.contains("cs_CZ"));
            assertTrue(keys.contains("ar_SA"));
            assertTrue(keys.contains("hi_IN"));
        }

        @Test
        @DisplayName("getLocale() returns the configured locale")
        void getLocale() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.GERMANY);
            assertEquals(Locale.GERMANY, gen.getLocale());
        }

        @Test
        @DisplayName("null locale constructor throws NullPointerException")
        void nullLocaleThrows() {
            assertThrows(NullPointerException.class,
                         () -> new NationalIdGenerator((Locale) null));
        }

        @Test
        @DisplayName("null config constructor throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                         () -> new NationalIdGenerator((GeneratorConfig) null));
        }

        @Test
        @DisplayName("config constructor uses scoped registry context")
        void configConstructorUsesScopedContext() {
            Locale locale = Locale.CANADA;
            DataRegistryContext context = DataRegistryContext.builder()
                                                             .isolated()
                                                             .registerNationalIdProvider(new NationalIdProvider() {
                                                                 @Override
                                                                 public Locale getLocale() {
                                                                     return locale;
                                                                 }

                                                                 @Override
                                                                 public String generate(Random random) {
                                                                     return "CA-ID";
                                                                 }
                                                             })
                                                             .build();

            GeneratorConfig config = GeneratorConfig.builder()
                                                    .locale(locale)
                                                    .registryContext(context)
                                                    .seed(7L)
                                                    .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                    .build();
            NationalIdGenerator generator = new NationalIdGenerator(config);
            assertEquals(locale, generator.getLocale());
            assertEquals("CA-ID", generator.generate());
        }

        @Test
        @DisplayName("config constructor fails when locale is not registered in scoped context")
        void configConstructorRejectsMissingLocale() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .locale(Locale.US)
                                                    .registryContext(DataRegistryContext.builder().isolated().build())
                                                    .build();

            UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                                                            () -> new NationalIdGenerator(config));
            assertTrue(ex.getMessage().contains("Locale en_US is not supported"));
        }

        @Test
        @DisplayName("generateList returns correct count")
        void generateListCount() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.US);
            List<String> ids = gen.generateList(25);
            assertEquals(25, ids.size());
            ids.forEach(id -> assertNotNull(id));
        }

        @Test
        @DisplayName("stream generates continuous values")
        void streamGeneration() {
            NationalIdGenerator gen = new NationalIdGenerator(Locale.US);
            List<String> ids = gen.stream().limit(20).toList();
            assertEquals(20, ids.size());
        }
    }
}
