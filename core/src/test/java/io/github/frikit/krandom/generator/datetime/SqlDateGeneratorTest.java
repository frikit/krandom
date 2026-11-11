/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("a range that holds no day a java.sql.Date holds in the default time zone fails at construction")
    void rangeWithoutAHeldDayFailsAtConstruction() {
        LocalDate yearZero = LocalDate.of(0, 6, 1);

        assertEquals(
                "no day from 0000-06-01 to 0000-06-01 is one that a java.sql.Date holds in the default time zone UTC",
                assertThrows(IllegalArgumentException.class, () -> new SqlDateGenerator(yearZero, yearZero))
                        .getMessage());
        assertThrows(
                IllegalArgumentException.class,
                () -> new SqlDateGenerator(LocalDate.of(1582, 10, 5), LocalDate.of(1582, 10, 14)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SqlDateGenerator(LocalDate.of(292_278_995, 1, 1), LocalDate.of(292_278_995, 12, 31)));

        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
        LocalDate skipped = LocalDate.of(2011, 12, 30);
        assertThrows(IllegalArgumentException.class, () -> new SqlDateGenerator(skipped, skipped));
    }

    @Test
    @DisplayName("every value reads back as a day in the range, and one that would not gives its last held day")
    void everyValueReadsBackInTheRange() {
        assertReadsBackWithin(LocalDate.of(1582, 10, 1), LocalDate.of(1582, 10, 15), LocalDate.of(1582, 10, 15));
        // Its bound is in the gap, so a drawn day of the gap, which reads back ten days later, gives 4 October.
        assertReadsBackWithin(LocalDate.of(1582, 10, 1), LocalDate.of(1582, 10, 10), LocalDate.of(1582, 10, 4));
        // Past the year 292278994 a java.sql.Date overflows and reads back as an earlier day, before the range.
        assertReadsBackWithin(
                LocalDate.of(292_278_990, 1, 1),
                LocalDate.of(292_278_999, 12, 31),
                new java.sql.Date(Long.MAX_VALUE).toLocalDate());

        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));
        assertReadsBackWithin(LocalDate.of(2011, 12, 29), LocalDate.of(2011, 12, 31), LocalDate.of(2011, 12, 31));
        assertReadsBackWithin(LocalDate.of(2011, 12, 29), LocalDate.of(2011, 12, 30), LocalDate.of(2011, 12, 29));
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

    /** Every value reads back in the range, and some, from drawn days that would not, as {@code lastHeld}. */
    private static void assertReadsBackWithin(LocalDate min, LocalDate max, LocalDate lastHeld) {
        SqlDateGenerator generator = new SqlDateGenerator(min, max);
        generator.reseed(42L);
        boolean gaveLastHeld = false;
        for (int i = 0; i < 2_000; i++) {
            LocalDate value = generator.generate().toLocalDate();
            assertFalse(value.isBefore(min) || value.isAfter(max), value + " is outside " + min + ".." + max);
            gaveLastHeld |= value.equals(lastHeld);
        }
        assertTrue(gaveLastHeld, lastHeld + " was never given in " + min + ".." + max);
    }
}
