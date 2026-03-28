/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithLegacyDateTimes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("ObjectGenerator — legacy date/time auto-population")
class ObjectGeneratorLegacyDateTimeTest {

    private static final int       SAMPLES = 50;
    private static final LocalDate MIN     = LocalDate.of(2020, 1, 1);
    private static final LocalDate MAX     = LocalDate.of(2023, 12, 31);

    @Test
    @DisplayName("legacy date/time fields are auto-populated")
    void legacyTypesPopulated() {
        PersonWithLegacyDateTimes p = new ObjectGenerator<>(PersonWithLegacyDateTimes.class).generate();
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getBornOn());
        assertNotNull(p.getWakeUpTime());
        assertNotNull(p.getUpdatedAt());
    }

    @Test
    @DisplayName("global dateRange applies to util.Date, sql.Date and sql.Timestamp")
    void globalRangeAppliesToLegacyDateTypes() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .dateRange(MIN, MAX)
                                                            .build();
        ObjectGenerator<PersonWithLegacyDateTimes> gen = new ObjectGenerator<>(PersonWithLegacyDateTimes.class, config);

        for (int i = 0; i < SAMPLES; i++) {
            PersonWithLegacyDateTimes p = gen.generate();

            LocalDate utilDate = p.getCreatedAt().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate sqlDate = p.getBornOn().toLocalDate();
            LocalDate sqlTimestamp = p.getUpdatedAt().toInstant().atZone(ZoneOffset.UTC).toLocalDate();

            assertFalse(utilDate.isBefore(MIN), "util.Date < min");
            assertFalse(utilDate.isAfter(MAX), "util.Date > max");
            assertFalse(sqlDate.isBefore(MIN), "sql.Date < min");
            assertFalse(sqlDate.isAfter(MAX), "sql.Date > max");
            assertFalse(sqlTimestamp.isBefore(MIN), "sql.Timestamp < min");
            assertFalse(sqlTimestamp.isAfter(MAX), "sql.Timestamp > max");
        }
    }
}
