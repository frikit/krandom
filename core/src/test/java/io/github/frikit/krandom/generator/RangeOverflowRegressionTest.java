/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.SqlTimestampGenerator;
import io.github.frikit.krandom.generator.datetime.UtilDateGenerator;
import io.github.frikit.krandom.generator.user.AgeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Range widths that exceed int or long never overflow")
class RangeOverflowRegressionTest {

    private static final LocalDate FAR_PAST = LocalDate.of(-200_000_000, 1, 1);
    private static final LocalDate FAR_FUTURE = LocalDate.of(200_000_000, 12, 31);

    @Test
    @DisplayName("generateYear samples full int ranges, rejects reversed bounds, and keeps legacy values")
    void generateYearHandlesEveryIntRange() {
        DateGenerator generator = new DateGenerator(GeneratorConfig.builder().seed(21L).build());
        Random reference = new Random(21L);

        assertEquals(1990 + reference.nextInt(31), generator.generateYear(1990, 2020));
        for (int i = 0; i < 1_000; i++) {
            int any = generator.generateYear(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int nonNegative = generator.generateYear(0, Integer.MAX_VALUE);
            assertTrue(nonNegative >= 0, Integer.toString(nonNegative));
            assertTrue(any >= Integer.MIN_VALUE);
        }
        assertEquals(7, generator.generateYear(7, 7));
        IllegalArgumentException reversed = assertThrows(IllegalArgumentException.class,
                                                         () -> generator.generateYear(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertTrue(reversed.getMessage().contains("max"), reversed.getMessage());
    }

    @Test
    @DisplayName("an age range starting at zero may end at Integer.MAX_VALUE")
    void ageGeneratorSamplesTheFullNonNegativeIntRange() {
        AgeGenerator generator = new AgeGenerator(0, Integer.MAX_VALUE, GeneratorConfig.builder().seed(5L).build());

        for (int i = 0; i < 1_000; i++) {
            int age = generator.generate();
            assertTrue(age >= 0, Integer.toString(age));
        }
        Random reference = new Random(5L);
        assertEquals(18 + reference.nextInt(48),
                     new AgeGenerator(18, 65, GeneratorConfig.builder().seed(5L).build()).generate());
    }

    @Test
    @DisplayName("legacy java.util.Date and Timestamp ranges wider than Long.MAX_VALUE milliseconds do not overflow")
    void legacyDateRangesWiderThanALongDoNotOverflow() {
        long lo = FAR_PAST.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long hiExclusive = FAR_FUTURE.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        UtilDateGenerator dates = new UtilDateGenerator(FAR_PAST, FAR_FUTURE);
        SqlTimestampGenerator timestamps = new SqlTimestampGenerator(FAR_PAST, FAR_FUTURE);

        for (int i = 0; i < 200; i++) {
            long date = dates.generate().getTime();
            long timestamp = timestamps.generate().getTime();
            assertTrue(date >= lo && date < hiExclusive, Long.toString(date));
            assertTrue(timestamp >= lo && timestamp < hiExclusive, Long.toString(timestamp));
        }
    }
}
