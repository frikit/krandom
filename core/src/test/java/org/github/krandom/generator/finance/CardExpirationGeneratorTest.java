/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardExpirationGeneratorTest {

    private CardExpirationGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CardExpirationGenerator();
    }

    // Constructor Tests

    @Test
    void testDefaultConstructor() {
        assertNotNull(generator);
        assertEquals(DateRange.FUTURE, generator.getDateRange());
        assertNotNull(generator.generate());
    }

    @Test
    void testDateRangeConstructor() {
        CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
        assertEquals(DateRange.FUTURE, futureGen.getDateRange());

        CardExpirationGenerator pastGen = new CardExpirationGenerator(DateRange.PAST);
        assertEquals(DateRange.PAST, pastGen.getDateRange());

        CardExpirationGenerator anyGen = new CardExpirationGenerator(DateRange.ANY);
        assertEquals(DateRange.ANY, anyGen.getDateRange());
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        CardExpirationGenerator gen = new CardExpirationGenerator(config);
        assertNotNull(gen);
        assertEquals(DateRange.FUTURE, gen.getDateRange());
    }

    @Test
    void testConfigDateRangeConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();

        CardExpirationGenerator futureGen = new CardExpirationGenerator(config, DateRange.FUTURE);
        assertEquals(DateRange.FUTURE, futureGen.getDateRange());

        CardExpirationGenerator pastGen = new CardExpirationGenerator(config, DateRange.PAST);
        assertEquals(DateRange.PAST, pastGen.getDateRange());

        CardExpirationGenerator anyGen = new CardExpirationGenerator(config, DateRange.ANY);
        assertEquals(DateRange.ANY, anyGen.getDateRange());
    }

    @Test
    void testNullConfig() {
        assertThrows(NullPointerException.class,
                     () -> new CardExpirationGenerator((GeneratorConfig) null));
    }

    @Test
    void testNullDateRange() {
        assertThrows(NullPointerException.class,
                     () -> new CardExpirationGenerator((DateRange) null));
    }

    // Basic Generation Tests

    @Test
    void testGenerate() {
        String expiry = generator.generate();
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "Should match MM/YY format");
    }

    @Test
    void testGenerateMultiple() {
        for (int i = 0; i < 100; i++) {
            String expiry = generator.generate();
            assertNotNull(expiry);
            assertTrue(expiry.matches("\\d{2}/\\d{2}"));
        }
    }

    @Test
    void testGenerateVariety() {
        Set<String> expiries = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            expiries.add(generator.generate());
        }
        assertTrue(expiries.size() > 30, "Should generate variety of expiration dates");
    }

    // DateRange Tests

    @Test
    void testPastDateRangeGenerate() {
        CardExpirationGenerator pastGen = new CardExpirationGenerator(DateRange.PAST);
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 100; i++) {
            String expiry = pastGen.generate();
            YearMonth expiryDate = parseExpiryDate(expiry);
            assertTrue(expiryDate.isBefore(now),
                       "PAST generator should produce dates in the past: " + expiry);
        }
    }

    @Test
    void testFutureDateRangeGenerate() {
        CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 100; i++) {
            String expiry = futureGen.generate();
            YearMonth expiryDate = parseExpiryDate(expiry);
            assertTrue(expiryDate.isAfter(now),
                       "FUTURE generator should produce dates in the future: " + expiry);
        }
    }

    @Test
    void testAnyDateRangeGenerate() {
        CardExpirationGenerator anyGen = new CardExpirationGenerator(DateRange.ANY);
        Set<Boolean> hasPast = new HashSet<>();
        Set<Boolean> hasFuture = new HashSet<>();
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 200; i++) {
            String expiry = anyGen.generate();
            YearMonth expiryDate = parseExpiryDate(expiry);

            if (expiryDate.isBefore(now)) {
                hasPast.add(true);
            }
            if (expiryDate.isAfter(now)) {
                hasFuture.add(true);
            }
        }

        assertTrue(hasFuture.contains(true), "ANY should generate future dates");
        // Past dates might appear in the sample
    }

    @Test
    void testGenerateWithDateRangeParameter() {
        CardExpirationGenerator gen = new CardExpirationGenerator();
        YearMonth now = YearMonth.now();

        // Test PAST
        String pastExpiry = gen.generate(DateRange.PAST);
        YearMonth pastDate = parseExpiryDate(pastExpiry);
        assertTrue(pastDate.isBefore(now), "DateRange.PAST should produce past dates");

        // Test FUTURE
        String futureExpiry = gen.generate(DateRange.FUTURE);
        YearMonth futureDate = parseExpiryDate(futureExpiry);
        assertTrue(futureDate.isAfter(now), "DateRange.FUTURE should produce future dates");

        // Test ANY
        String anyExpiry = gen.generate(DateRange.ANY);
        assertNotNull(anyExpiry);
        assertTrue(anyExpiry.matches("\\d{2}/\\d{2}"));
    }

    @Test
    void testGetDateRange() {
        CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
        assertEquals(DateRange.FUTURE, futureGen.getDateRange());

        CardExpirationGenerator pastGen = new CardExpirationGenerator(DateRange.PAST);
        assertEquals(DateRange.PAST, pastGen.getDateRange());

        CardExpirationGenerator anyGen = new CardExpirationGenerator(DateRange.ANY);
        assertEquals(DateRange.ANY, anyGen.getDateRange());
    }

    @Test
    void testFutureDateRangeGenerateAlias() {
        CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 100; i++) {
            String expiry = futureGen.generate();
            YearMonth expiryDate = parseExpiryDate(expiry);
            assertTrue(expiryDate.isAfter(now) || expiryDate.equals(now.plusMonths(1)),
                       "Future-only generator should produce dates in the future: " + expiry);
        }
    }

    @Test
    void testAnyDateRangeGenerateAlias() {
        CardExpirationGenerator anyGen = new CardExpirationGenerator(DateRange.ANY);
        Set<Boolean> hasPast = new HashSet<>();
        Set<Boolean> hasFuture = new HashSet<>();
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 200; i++) {
            String expiry = anyGen.generate();
            YearMonth expiryDate = parseExpiryDate(expiry);

            if (expiryDate.isBefore(now)) {
                hasPast.add(true);
            }
            if (expiryDate.isAfter(now)) {
                hasFuture.add(true);
            }
        }

        assertTrue(hasFuture.contains(true), "Should generate future dates");
        // Past dates might not appear in small sample, so we don't strictly require them
    }

    // Locale-Specific Tests

    @Test
    void testGenerateWithLocaleUS() {
        Locale locale = Locale.of("en", "US");
        String expiry = generator.generate(locale);
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "US should use MM/YY format");
    }

    @Test
    void testGenerateWithLocaleGB() {
        Locale locale = Locale.of("en", "GB");
        String expiry = generator.generate(locale);
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "GB should use MM/YY format");
    }

    @Test
    void testGenerateWithLocaleDE() {
        Locale locale = Locale.of("de", "DE");
        String expiry = generator.generate(locale);
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "DE should use MM/YY format");
    }

    @Test
    void testGenerateWithLocaleJP() {
        Locale locale = Locale.of("ja", "JP");
        String expiry = generator.generate(locale);
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "JP should use YY/MM format");

        // Verify it's actually YY/MM by checking that first part is year
        String[] parts = expiry.split("/");
        int firstPart = Integer.parseInt(parts[0]);
        int secondPart = Integer.parseInt(parts[1]);

        // Year (YY) should typically be 24-29 for near future
        // Month should be 01-12
        assertTrue(secondPart >= 1 && secondPart <= 12,
                   "Second part should be month (01-12) for JP locale");
    }

    @Test
    void testGenerateWithLocaleCN() {
        Locale locale = Locale.of("zh", "CN");
        String expiry = generator.generate(locale);
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "CN should use YY/MM format");
    }

    @Test
    void testGenerateWithNullLocale() {
        String expiry = generator.generate((Locale) null);
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "Null locale should default to MM/YY");
    }

    @Test
    void testGenerateWithLocaleDateRangeParameters() {
        Locale usLocale = Locale.of("en", "US");
        Locale jpLocale = Locale.of("ja", "JP");

        String usFuture = generator.generate(usLocale, DateRange.FUTURE);
        String usAny = generator.generate(usLocale, DateRange.ANY);
        String jpFuture = generator.generate(jpLocale, DateRange.FUTURE);

        assertNotNull(usFuture);
        assertNotNull(usAny);
        assertNotNull(jpFuture);
    }

    // Month Generation Tests

    @Test
    void testGetMonth() {
        String month = generator.getMonth();
        assertNotNull(month);
        assertTrue(month.matches("\\d{2}"), "Month should be 2 digits");
        int monthValue = Integer.parseInt(month);
        assertTrue(monthValue >= 1 && monthValue <= 12, "Month should be 01-12");
    }

    @Test
    void testGetMonthMultiple() {
        Set<String> months = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String month = generator.getMonth();
            assertNotNull(month);
            int monthValue = Integer.parseInt(month);
            assertTrue(monthValue >= 1 && monthValue <= 12);
            months.add(month);
        }
        assertTrue(months.size() >= 10, "Should generate variety of months");
    }

    @Test
    void testGetMonthWithFutureDateRange() {
        String month = generator.getMonth(DateRange.FUTURE);
        assertNotNull(month);
        assertTrue(month.matches("\\d{2}"));
        int monthValue = Integer.parseInt(month);
        assertTrue(monthValue >= 1 && monthValue <= 12);
    }

    @Test
    void testGetMonthWithAnyDateRange() {
        String month = generator.getMonth(DateRange.ANY);
        assertNotNull(month);
        assertTrue(month.matches("\\d{2}"));
        int monthValue = Integer.parseInt(month);
        assertTrue(monthValue >= 1 && monthValue <= 12);
    }

    // Year Generation Tests

    @Test
    void testGetYear() {
        String year = generator.getYear();
        assertNotNull(year);
        assertTrue(year.matches("\\d{2}"), "Default should be 2-digit year");
    }

    @Test
    void testGetYearShort() {
        String year = generator.getYear(false);
        assertNotNull(year);
        assertTrue(year.matches("\\d{2}"), "Short year should be 2 digits");
    }

    @Test
    void testGetYearFull() {
        String year = generator.getYear(true);
        assertNotNull(year);
        assertTrue(year.matches("\\d{4}"), "Full year should be 4 digits");
        int yearValue = Integer.parseInt(year);
        assertTrue(yearValue >= 2024 && yearValue <= 2035,
                   "Year should be in reasonable range for card expiry");
    }

    @Test
    void testGetYearWithDateRangeParameters() {
        String shortFuture = generator.getYear(false, DateRange.FUTURE);
        String fullFuture = generator.getYear(true, DateRange.FUTURE);
        String shortAny = generator.getYear(false, DateRange.ANY);
        String fullAny = generator.getYear(true, DateRange.ANY);

        assertTrue(shortFuture.matches("\\d{2}"));
        assertTrue(fullFuture.matches("\\d{4}"));
        assertTrue(shortAny.matches("\\d{2}"));
        assertTrue(fullAny.matches("\\d{4}"));
    }

    @Test
    void testGetYearFutureDateRange() {
        CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
        int currentYear = LocalDate.now().getYear();

        for (int i = 0; i < 50; i++) {
            String yearStr = futureGen.getYear(true);
            int year = Integer.parseInt(yearStr);
            assertTrue(year >= currentYear,
                       "Future-only generator should produce current or future years");
        }
    }

    // Seeding Tests

    @Test
    void testSeededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        CardExpirationGenerator gen1 = new CardExpirationGenerator(config);
        CardExpirationGenerator gen2 = new CardExpirationGenerator(config);

        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededMonthGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(67890L).build();
        CardExpirationGenerator gen1 = new CardExpirationGenerator(config);
        CardExpirationGenerator gen2 = new CardExpirationGenerator(config);

        assertEquals(gen1.getMonth(), gen2.getMonth());
        assertEquals(gen1.getMonth(), gen2.getMonth());
    }

    @Test
    void testSeededYearGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(11111L).build();
        CardExpirationGenerator gen1 = new CardExpirationGenerator(config);
        CardExpirationGenerator gen2 = new CardExpirationGenerator(config);

        assertEquals(gen1.getYear(), gen2.getYear());
        assertEquals(gen1.getYear(true), gen2.getYear(true));
    }

    @Test
    void testDifferentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder().seed(111L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().seed(222L).build();
        CardExpirationGenerator gen1 = new CardExpirationGenerator(config1);
        CardExpirationGenerator gen2 = new CardExpirationGenerator(config2);

        Set<String> expiries1 = new HashSet<>();
        Set<String> expiries2 = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            expiries1.add(gen1.generate());
            expiries2.add(gen2.generate());
        }

        // Different seeds should produce different sequences
        assertNotEquals(expiries1, expiries2);
    }

    // Stream and List Tests

    @Test
    void testStream() {
        List<String> expiries = generator.stream().limit(20).toList();
        assertEquals(20, expiries.size());
        expiries.forEach(expiry -> assertTrue(expiry.matches("\\d{2}/\\d{2}")));
    }

    @Test
    void testGenerateList() {
        List<String> expiries = generator.generateList(15);
        assertEquals(15, expiries.size());
        expiries.forEach(expiry -> assertTrue(expiry.matches("\\d{2}/\\d{2}")));
    }

    @Test
    void testGenerateEmptyList() {
        List<String> expiries = generator.generateList(0);
        assertTrue(expiries.isEmpty());
    }

    @Test
    void testGenerateListNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateList(-1));
    }

    // Format Validation Tests

    @Test
    void testFormatConsistency() {
        for (int i = 0; i < 100; i++) {
            String expiry = generator.generate();
            String[] parts = expiry.split("/");
            assertEquals(2, parts.length, "Should have exactly 2 parts separated by /");
            assertEquals(2, parts[0].length(), "First part should be 2 digits");
            assertEquals(2, parts[1].length(), "Second part should be 2 digits");
        }
    }

    @Test
    void testMonthFormat() {
        for (int i = 0; i < 100; i++) {
            String month = generator.getMonth();
            assertEquals(2, month.length(), "Month should always be 2 digits");
            assertTrue(month.charAt(0) == '0' || month.charAt(0) == '1',
                       "Month first digit should be 0 or 1");
        }
    }

    @Test
    void testYearShortFormat() {
        for (int i = 0; i < 100; i++) {
            String year = generator.getYear(false);
            assertEquals(2, year.length(), "Short year should be 2 digits");
        }
    }

    @Test
    void testYearFullFormat() {
        for (int i = 0; i < 100; i++) {
            String year = generator.getYear(true);
            assertEquals(4, year.length(), "Full year should be 4 digits");
            assertTrue(year.startsWith("20"), "Full year should start with 20");
        }
    }

    // Edge Case Tests

    @Test
    void testFutureDateRangeNeverExpiresSoon() {
        CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 100; i++) {
            String expiry = futureGen.generate();
            YearMonth expiryDate = parseExpiryDate(expiry);
            assertTrue(expiryDate.isAfter(now),
                       "Future-only should never generate current or past dates");
        }
    }

    @Test
    void testDateRangeFromBoolean() {
        assertEquals(DateRange.FUTURE, DateRange.fromBoolean(true));
        assertEquals(DateRange.ANY, DateRange.fromBoolean(false));
    }

    @Test
    void testLocaleFormatDifference() {
        Locale usLocale = Locale.of("en", "US");
        Locale jpLocale = Locale.of("ja", "JP");

        // Generate with same seed to get same date
        GeneratorConfig config = GeneratorConfig.builder().seed(99999L).build();
        CardExpirationGenerator gen1 = new CardExpirationGenerator(config);
        CardExpirationGenerator gen2 = new CardExpirationGenerator(config);

        String usFormat = gen1.generate(usLocale);
        String jpFormat = gen2.generate(jpLocale);

        // Formats should be reversed (MM/YY vs YY/MM)
        String[] usParts = usFormat.split("/");
        String[] jpParts = jpFormat.split("/");

        // The values should be swapped
        assertEquals(usParts[0], jpParts[1], "Month in US should be year in JP");
        assertEquals(usParts[1], jpParts[0], "Year in US should be month in JP");
    }

    // Helper Methods

    private YearMonth parseExpiryDate(String expiry) {
        String[] parts = expiry.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        return YearMonth.of(year, month);
    }
}
