/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.core.model.PersonWithAdditionalDateTimes;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        ObjectGeneratorConfig config =
                ObjectGeneratorConfig.builder().dateRange(MIN, MAX).build();
        ObjectGenerator<PersonWithAdditionalDateTimes> gen =
                new ObjectGenerator<>(PersonWithAdditionalDateTimes.class, config);

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

    @Test
    @DisplayName("offset fields draw every quarter hour from -12:00 to +14:00, and nothing else")
    void offsetFieldsDrawOnlyTheConventionalOffsets() {
        Set<ZoneOffset> conventional = IntStream.rangeClosed(-12 * 4, 14 * 4)
                .mapToObj(quarterHours -> ZoneOffset.ofTotalSeconds(quarterHours * 15 * 60))
                .collect(Collectors.toSet());
        // In 1880 most zones' rules gave an offset with seconds, which an OffsetDateTime must not inherit.
        LocalDate min = LocalDate.of(1880, 1, 1);
        LocalDate max = LocalDate.of(1880, 12, 31);
        GeneratorConfig config =
                GeneratorConfig.builder().seed(42L).objectDateRange(min, max).build();
        ObjectGenerator<PersonWithAdditionalDateTimes> gen =
                new ObjectGenerator<>(PersonWithAdditionalDateTimes.class, config);
        Set<ZoneOffset> offsetDateTimes = new HashSet<>();
        Set<ZoneOffset> offsetTimes = new HashSet<>();
        Set<ZoneOffset> zoneOffsets = new HashSet<>();

        for (int i = 0; i < 5_000; i++) {
            PersonWithAdditionalDateTimes p = gen.generate();
            LocalDate offsetDate = p.getOffsetDateTime().toLocalDate();
            assertFalse(offsetDate.isBefore(min), offsetDate.toString());
            assertFalse(offsetDate.isAfter(max), offsetDate.toString());
            offsetDateTimes.add(p.getOffsetDateTime().getOffset());
            offsetTimes.add(p.getOffsetTime().getOffset());
            zoneOffsets.add(p.getZoneOffset());
        }

        assertEquals(conventional, offsetDateTimes);
        assertEquals(conventional, offsetTimes);
        assertEquals(conventional, zoneOffsets);
    }
}
