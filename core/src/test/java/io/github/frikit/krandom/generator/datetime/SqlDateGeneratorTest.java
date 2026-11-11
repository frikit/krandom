/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Random;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A {@code java.sql.Date} holds a day as the start of that day in the default time zone, in the Julian
 * calendar before 15 October 1582, so it cannot hold every {@link LocalDate}: such a day reads back as
 * another one.
 */
@DisplayName("SqlDateGenerator")
class SqlDateGeneratorTest {

    private TimeZone originalTimeZone;

    @BeforeEach
    void useUtc() {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    void restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    @DisplayName("a bound that a java.sql.Date cannot hold in the default time zone fails at construction")
    void unheldBoundFailsAtConstruction() {
        LocalDate yearZero = LocalDate.of(0, 6, 1);

        assertEquals(
                "min 0000-06-01 is not a day that a java.sql.Date holds in the default time zone UTC, "
                        + "which reads it back as 0001-06-01",
                assertThrows(IllegalArgumentException.class, () -> new SqlDateGenerator(yearZero, yearZero))
                        .getMessage());
        assertEquals(
                "max 1582-10-10 is not a day that a java.sql.Date holds in the default time zone UTC, "
                        + "which reads it back as 1582-10-20",
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new SqlDateGenerator(LocalDate.of(1582, 10, 1), LocalDate.of(1582, 10, 10)))
                        .getMessage());
        assertThrows(
                IllegalArgumentException.class,
                () -> new SqlDateGenerator(LocalDate.of(2000, 1, 1), LocalDate.of(292_278_995, 1, 1)));

        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
        LocalDate skipped = LocalDate.of(2011, 12, 30);
        assertThrows(IllegalArgumentException.class, () -> new SqlDateGenerator(skipped, skipped));
    }

    @Test
    @DisplayName("every value reads back as a day in the range, where a drawn day the zone cannot hold reads later")
    void everyValueReadsBackInTheRange() {
        assertReadsBackWithin(LocalDate.of(1582, 10, 1), LocalDate.of(1582, 10, 15));

        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
        assertReadsBackWithin(LocalDate.of(2011, 12, 29), LocalDate.of(2011, 12, 31));
    }

    @Test
    @DisplayName("a value that reads back in the range is the drawn day's java.sql.Date, as before")
    void valuesInTheRangeAreKept() {
        LocalDate min = LocalDate.of(1582, 9, 20);
        LocalDate max = LocalDate.of(1582, 10, 20);
        SqlDateGenerator generator = new SqlDateGenerator(min, max);
        generator.reseed(42L);
        Random reference = new Random(42L);
        int pastMax = 0;

        for (int i = 0; i < 2_000; i++) {
            LocalDate drawn = LocalDate.ofEpochDay(min.toEpochDay() + reference.nextLong(31));
            java.sql.Date before = java.sql.Date.valueOf(drawn);
            boolean kept = !before.toLocalDate().isAfter(max);
            pastMax += kept ? 0 : 1;
            assertEquals(kept ? before : java.sql.Date.valueOf(max), generator.generate(), drawn.toString());
        }
        assertFalse(pastMax == 0, "some drawn days of the gap read back past max");
    }

    private static void assertReadsBackWithin(LocalDate min, LocalDate max) {
        SqlDateGenerator generator = new SqlDateGenerator(min, max);
        generator.reseed(42L);
        for (int i = 0; i < 2_000; i++) {
            LocalDate value = generator.generate().toLocalDate();
            assertFalse(value.isBefore(min) || value.isAfter(max), value + " is outside " + min + ".." + max);
        }
    }
}
