/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CalendarGenerator")
class CalendarGeneratorTest {

    @Test
    @DisplayName("generate returns non-lenient GregorianCalendar")
    void generateReturnsGregorianCalendar() {
        Calendar calendar = new CalendarGenerator().generate();

        assertInstanceOf(GregorianCalendar.class, calendar);
        assertFalse(calendar.isLenient());
    }

    @Test
    @DisplayName("generated year is within default range")
    void generatedYearIsWithinDefaultRange() {
        CalendarGenerator generator = new CalendarGenerator();

        for (int i = 0; i < 50; i++) {
            int year = generator.generate().get(Calendar.YEAR);
            assertTrue(year >= 1970 && year <= 2100, "year out of range: " + year);
        }
    }

    @Test
    @DisplayName("bounded generator respects date range")
    void boundedGeneratorRespectsDateRange() {
        LocalDate min = LocalDate.of(2020, 1, 1);
        LocalDate max = LocalDate.of(2020, 1, 3);
        CalendarGenerator generator = new CalendarGenerator(min, max, GeneratorConfig.builder().seed(9L).build());

        for (int i = 0; i < 20; i++) {
            Calendar calendar = generator.generate();
            LocalDate date = calendar.toInstant().atZone(calendar.getTimeZone().toZoneId()).toLocalDate();
            assertFalse(date.isBefore(min));
            assertFalse(date.isAfter(max));
        }
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGenerationIsReproducible() {
        CalendarGenerator first = new CalendarGenerator(GeneratorConfig.builder().seed(1234L).build());
        CalendarGenerator second = new CalendarGenerator(GeneratorConfig.builder().seed(1234L).build());

        for (int i = 0; i < 10; i++) {
            Calendar left = first.generate();
            Calendar right = second.generate();
            assertEquals(left.toInstant(), right.toInstant());
            assertEquals(left.getTimeZone().getID(), right.getTimeZone().getID());
        }
    }

    @Test
    @DisplayName("forType and facade expose calendar generator")
    void forTypeAndFacadeExposeCalendarGenerator() {
        assertNotNull(Generators.ofCalendar().generate());
        assertNotNull(Generators.ofCalendar(1L).generate());
        assertNotNull(Generators.ofCalendar(GeneratorConfig.defaults()).generate());
        assertNotNull(Generators.ofCalendar(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)).generate());
        assertNotNull(Generators.ofCalendar(LocalDate.of(2024, 1, 1),
                                            LocalDate.of(2024, 1, 2),
                                            GeneratorConfig.builder().seed(2L).build()).generate());
        assertInstanceOf(GregorianCalendar.class, Generators.forType(Calendar.class).generate());
        assertInstanceOf(GregorianCalendar.class, Generators.forType(GregorianCalendar.class).generate());
    }

    @Test
    @DisplayName("invalid constructor arguments fail fast")
    void invalidConstructorArgumentsFailFast() {
        assertThrows(NullPointerException.class, () -> new CalendarGenerator((GeneratorConfig) null));
        assertThrows(IllegalArgumentException.class,
                     () -> new CalendarGenerator(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 1)));
        assertThrows(IllegalArgumentException.class,
                     () -> new CalendarGenerator(LocalDate.of(2024, 1, 1), null, GeneratorConfig.defaults()));
    }
}
