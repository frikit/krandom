/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithDateTimes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectGenerator — global date range configuration")
class ObjectGeneratorDateRangeTest {

    private static final LocalDate MIN     = LocalDate.of(2020, 1, 1);
    private static final LocalDate MAX     = LocalDate.of(2023, 12, 31);
    private static final int       SAMPLES = 50;

    private final ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                                      .dateRange(MIN, MAX)
                                                                      .build();

    @Test
    @DisplayName("LocalDate field is within configured range")
    void localDateInRange() {
        ObjectGenerator<PersonWithDateTimes> gen = new ObjectGenerator<>(PersonWithDateTimes.class, config);
        for (int i = 0; i < SAMPLES; i++) {
            LocalDate v = gen.generate().getDob();
            assertNotNull(v);
            assertFalse(v.isBefore(MIN), "LocalDate " + v + " < min " + MIN);
            assertFalse(v.isAfter(MAX), "LocalDate " + v + " > max " + MAX);
        }
    }

    @Test
    @DisplayName("LocalDateTime field date part is within configured range")
    void localDateTimeInRange() {
        ObjectGenerator<PersonWithDateTimes> gen = new ObjectGenerator<>(PersonWithDateTimes.class, config);
        for (int i = 0; i < SAMPLES; i++) {
            LocalDate d = gen.generate().getCreatedAt().toLocalDate();
            assertFalse(d.isBefore(MIN), "LocalDateTime date " + d + " < min");
            assertFalse(d.isAfter(MAX), "LocalDateTime date " + d + " > max");
        }
    }

    @Test
    @DisplayName("Instant date part is within configured range")
    void instantInRange() {
        ObjectGenerator<PersonWithDateTimes> gen = new ObjectGenerator<>(PersonWithDateTimes.class, config);
        for (int i = 0; i < SAMPLES; i++) {
            LocalDate d = gen.generate().getUpdatedAt().atOffset(ZoneOffset.UTC).toLocalDate();
            assertFalse(d.isBefore(MIN), "Instant date " + d + " < min");
            assertFalse(d.isAfter(MAX), "Instant date " + d + " > max");
        }
    }

    @Test
    @DisplayName("ZonedDateTime date part is within configured range")
    void zonedDateTimeInRange() {
        ObjectGenerator<PersonWithDateTimes> gen = new ObjectGenerator<>(PersonWithDateTimes.class, config);
        for (int i = 0; i < SAMPLES; i++) {
            LocalDate d = gen.generate().getScheduledAt().toLocalDate();
            assertFalse(d.isBefore(MIN), "ZonedDateTime date " + d + " < min");
            assertFalse(d.isAfter(MAX), "ZonedDateTime date " + d + " > max");
        }
    }

    @Test
    @DisplayName("dateRange(min > max) throws IllegalArgumentException")
    void invalidRangeThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().dateRange(MAX, MIN).build());
    }
}
