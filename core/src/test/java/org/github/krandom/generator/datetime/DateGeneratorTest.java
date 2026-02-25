/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DateGeneratorTest {

    @Test
    void testDefaultConstructor() {
        DateGenerator generator = new DateGenerator();
        assertNotNull(generator);
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        DateGenerator generator = new DateGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new DateGenerator(null));
    }

    @Test
    void testGenerateNotNull() {
        DateGenerator generator = new DateGenerator();
        LocalDate date = generator.generate();
        assertNotNull(date);
    }

    @Test
    void testGenerateInValidRange() {
        DateGenerator generator = new DateGenerator();
        for (int i = 0; i < 100; i++) {
            LocalDate date = generator.generate();
            assertTrue(date.getYear() >= 1970 && date.getYear() <= 2100,
                    "Year out of range: " + date.getYear());
        }
    }

    @Test
    void testGenerateWithYear() {
        DateGenerator generator = new DateGenerator();
        LocalDate date = generator.generateWithYear(2020);
        assertEquals(2020, date.getYear());
    }

    @Test
    void testGenerateWithMonth() {
        DateGenerator generator = new DateGenerator();
        LocalDate date = generator.generateWithMonth(5);
        assertEquals(5, date.getMonthValue());
    }

    @Test
    void testGenerateWithDay() {
        DateGenerator generator = new DateGenerator();
        LocalDate date = generator.generateWithDay(15);
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testGenerateWithDayHandlesFebruary() {
        DateGenerator generator = new DateGenerator(GeneratorConfig.builder().seed(42L).build());
        // Test with day 30 - should be capped to February's max
        for (int i = 0; i < 50; i++) {
            LocalDate date = generator.generateWithDay(30);
            if (date.getMonthValue() == 2) {
                assertTrue(date.getDayOfMonth() <= 29, 
                    "February day should be <= 29: " + date);
            }
        }
    }

    @Test
    void testGenerateString() {
        DateGenerator generator = new DateGenerator();
        String dateStr = generator.generateString();
        assertNotNull(dateStr);
        assertTrue(dateStr.matches("\\d{4}-\\d{2}-\\d{2}"), 
            "Expected ISO format: " + dateStr);
    }

    @Test
    void testGenerateAmerican() {
        DateGenerator generator = new DateGenerator();
        String dateStr = generator.generateAmerican();
        assertNotNull(dateStr);
        assertTrue(dateStr.matches("\\d{2}/\\d{2}/\\d{4}"), 
            "Expected MM/DD/YYYY format: " + dateStr);
    }

    @Test
    void testGenerateEuropean() {
        DateGenerator generator = new DateGenerator();
        String dateStr = generator.generateEuropean();
        assertNotNull(dateStr);
        assertTrue(dateStr.matches("\\d{2}/\\d{2}/\\d{4}"), 
            "Expected DD/MM/YYYY format: " + dateStr);
    }

    @Test
    void testAmericanVsEuropeanFormat() {
        DateGenerator generator = new DateGenerator(GeneratorConfig.builder().seed(123L).build());
        LocalDate date = generator.generate();
        
        String american = String.format("%02d/%02d/%04d", 
            date.getMonthValue(), date.getDayOfMonth(), date.getYear());
        String european = String.format("%02d/%02d/%04d", 
            date.getDayOfMonth(), date.getMonthValue(), date.getYear());
        
        // Verify month and day positions are swapped
        String[] americanParts = american.split("/");
        String[] europeanParts = european.split("/");
        
        assertEquals(americanParts[0], europeanParts[1], "Month should be first in American");
        assertEquals(americanParts[1], europeanParts[0], "Day should be first in European");
    }

    @Test
    void testGenerateYear() {
        DateGenerator generator = new DateGenerator();
        int year = generator.generateYear();
        assertTrue(year >= 1970 && year <= 2100, "Year out of range: " + year);
    }

    @Test
    void testGenerateYearWithRange() {
        DateGenerator generator = new DateGenerator();
        int year = generator.generateYear(2000, 2010);
        assertTrue(year >= 2000 && year <= 2010, "Year out of range: " + year);
    }

    @Test
    void testGenerateMonth() {
        DateGenerator generator = new DateGenerator();
        int month = generator.generateMonth();
        assertTrue(month >= 1 && month <= 12, "Month out of range: " + month);
    }

    @Test
    void testGenerateMonthName() {
        DateGenerator generator = new DateGenerator();
        String monthName = generator.generateMonthName();
        assertNotNull(monthName);
        assertTrue(monthName.matches("January|February|March|April|May|June|July|August|September|October|November|December"),
            "Invalid month name: " + monthName);
    }

    @Test
    void testGenerateTimestamp() {
        DateGenerator generator = new DateGenerator();
        long timestamp = generator.generateTimestamp();
        assertTrue(timestamp > 0, "Timestamp should be positive");
        // Should be between 1970 and 2100
        assertTrue(timestamp >= 0 && timestamp <= 4102444800L, 
            "Timestamp out of range: " + timestamp);
    }

    @Test
    void testSeededGeneratorProducesSameResults() {
        DateGenerator gen1 = new DateGenerator(GeneratorConfig.builder().seed(42L).build());
        DateGenerator gen2 = new DateGenerator(GeneratorConfig.builder().seed(42L).build());
        
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededYearProducesSameResults() {
        DateGenerator gen1 = new DateGenerator(GeneratorConfig.builder().seed(999L).build());
        DateGenerator gen2 = new DateGenerator(GeneratorConfig.builder().seed(999L).build());
        
        assertEquals(gen1.generateYear(), gen2.generateYear());
        assertEquals(gen1.generateYear(), gen2.generateYear());
    }

    @Test
    void testSeededMonthProducesSameResults() {
        DateGenerator gen1 = new DateGenerator(GeneratorConfig.builder().seed(777L).build());
        DateGenerator gen2 = new DateGenerator(GeneratorConfig.builder().seed(777L).build());
        
        assertEquals(gen1.generateMonth(), gen2.generateMonth());
        assertEquals(gen1.generateMonth(), gen2.generateMonth());
    }

    @Test
    void testDifferentSeedsProduceDifferentResults() {
        DateGenerator gen1 = new DateGenerator(GeneratorConfig.builder().seed(100L).build());
        DateGenerator gen2 = new DateGenerator(GeneratorConfig.builder().seed(200L).build());
        
        assertNotEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testGenerateMultipleDates() {
        DateGenerator generator = new DateGenerator();
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            dates.add(generator.generate());
        }
        assertTrue(dates.size() > 50, "Should generate diverse dates");
    }

    @Test
    void testGenerateMultipleYears() {
        DateGenerator generator = new DateGenerator();
        Set<Integer> years = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            years.add(generator.generateYear());
        }
        assertTrue(years.size() > 50, "Should generate diverse years");
    }

    @Test
    void testGenerateAllMonths() {
        DateGenerator generator = new DateGenerator();
        Set<Integer> months = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            months.add(generator.generateMonth());
        }
        assertTrue(months.size() == 12, "Should eventually generate all 12 months");
    }

    @Test
    void testGenerateAllMonthNames() {
        DateGenerator generator = new DateGenerator();
        Set<String> monthNames = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            monthNames.add(generator.generateMonthName());
        }
        assertTrue(monthNames.size() == 12, "Should eventually generate all 12 month names");
    }

    @Test
    void testDateComponentsInValidRange() {
        DateGenerator generator = new DateGenerator();
        for (int i = 0; i < 100; i++) {
            LocalDate date = generator.generate();
            
            assertTrue(date.getYear() >= 1970 && date.getYear() <= 2100);
            assertTrue(date.getMonthValue() >= 1 && date.getMonthValue() <= 12);
            assertTrue(date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 31);
        }
    }

    @Test
    void testGenerateWithYearProducesDifferentDates() {
        DateGenerator generator = new DateGenerator();
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            dates.add(generator.generateWithYear(2020));
        }
        assertTrue(dates.size() > 20, "Should generate different dates in same year");
    }

    @Test
    void testGenerateWithMonthProducesDifferentDates() {
        DateGenerator generator = new DateGenerator();
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            dates.add(generator.generateWithMonth(5));
        }
        assertTrue(dates.size() > 20, "Should generate different dates in same month");
    }

    @Test
    void testTimestampIsReproducible() {
        DateGenerator gen1 = new DateGenerator(GeneratorConfig.builder().seed(555L).build());
        DateGenerator gen2 = new DateGenerator(GeneratorConfig.builder().seed(555L).build());
        
        assertEquals(gen1.generateTimestamp(), gen2.generateTimestamp());
    }

    @Test
    void testYearRangeCoversEntireRange() {
        DateGenerator generator = new DateGenerator(GeneratorConfig.builder().seed(123L).build());
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        
        for (int i = 0; i < 1000; i++) {
            int year = generator.generateYear();
            min = Math.min(min, year);
            max = Math.max(max, year);
        }
        
        assertTrue(min <= 1980, "Should generate years near 1970");
        assertTrue(max >= 2090, "Should generate years near 2100");
    }

    @Test
    void testLeapYearHandling() {
        DateGenerator generator = new DateGenerator();
        // Generate dates in February of leap years
        for (int i = 0; i < 20; i++) {
            LocalDate date = generator.generateWithYear(2020); // Leap year
            if (date.getMonthValue() == 2) {
                assertTrue(date.getDayOfMonth() <= 29, 
                    "February in leap year should have <= 29 days");
            }
        }
    }

    @Test
    void testNonLeapYearHandling() {
        DateGenerator generator = new DateGenerator();
        // Generate dates in February of non-leap years
        for (int i = 0; i < 20; i++) {
            LocalDate date = generator.generateWithYear(2019); // Non-leap year
            if (date.getMonthValue() == 2) {
                assertTrue(date.getDayOfMonth() <= 28, 
                    "February in non-leap year should have <= 28 days");
            }
        }
    }
}
