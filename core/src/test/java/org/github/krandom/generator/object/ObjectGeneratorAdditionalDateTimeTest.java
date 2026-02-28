/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithAdditionalDateTimes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator — additional JSR-310 support")
class ObjectGeneratorAdditionalDateTimeTest {

    private static final int SAMPLES = 50;
    private static final LocalDate MIN = LocalDate.of(2021, 1, 1);
    private static final LocalDate MAX = LocalDate.of(2022, 12, 31);

    @Test
    @DisplayName("additional JSR-310 fields are auto-populated")
    void additionalDateTimeFieldsPopulated() {
        PersonWithAdditionalDateTimes p = new ObjectGenerator<>(PersonWithAdditionalDateTimes.class).generate();
        assertNotNull(p.getOffsetDateTime());
        assertNotNull(p.getOffsetTime());
        assertNotNull(p.getYear());
        assertNotNull(p.getYearMonth());
        assertNotNull(p.getMonthDay());
        assertNotNull(p.getDuration());
        assertNotNull(p.getPeriod());
        assertNotNull(p.getZoneId());
        assertNotNull(p.getZoneOffset());
    }

    @Test
    @DisplayName("dateRange constrains offsetDateTime/year/yearMonth fields")
    void dateRangeConstrainsAdditionalTypes() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder().dateRange(MIN, MAX).build();
        ObjectGenerator<PersonWithAdditionalDateTimes> gen = new ObjectGenerator<>(PersonWithAdditionalDateTimes.class, config);

        for (int i = 0; i < SAMPLES; i++) {
            PersonWithAdditionalDateTimes p = gen.generate();
            LocalDate offsetDate = p.getOffsetDateTime().toLocalDate();
            Year year = p.getYear();
            YearMonth yearMonth = p.getYearMonth();

            assertFalse(offsetDate.isBefore(MIN));
            assertFalse(offsetDate.isAfter(MAX));
            assertFalse(year.isBefore(Year.from(MIN)));
            assertFalse(year.isAfter(Year.from(MAX)));
            assertFalse(yearMonth.isBefore(YearMonth.from(MIN)));
            assertFalse(yearMonth.isAfter(YearMonth.from(MAX)));
        }
    }
}
