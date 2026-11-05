/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        CalendarGenerator generator = new CalendarGenerator(
                min, max, GeneratorConfig.builder().seed(9L).build());

        for (int i = 0; i < 20; i++) {
            Calendar calendar = generator.generate();
            LocalDate date = calendar.toInstant()
                    .atZone(calendar.getTimeZone().toZoneId())
                    .toLocalDate();
            assertFalse(date.isBefore(min));
            assertFalse(date.isAfter(max));
        }
    }

    @Test
    @DisplayName("a day a zone skipped stays in a range that ends on it")
    void aDayAZoneSkippedStaysInARangeThatEndsOnIt() {
        // Pacific/Apia and Pacific/Fakaofo went from 2011-12-29 to 2011-12-31.
        LocalDate skipped = LocalDate.of(2011, 12, 30);
        CalendarGenerator seeded = new CalendarGenerator(
                skipped, skipped, GeneratorConfig.builder().seed(42L).build());
        CalendarGenerator unseeded = Generators.ofCalendar(skipped, skipped);
        Set<String> zones = new HashSet<>();

        for (int i = 0; i < 5_000; i++) {
            Calendar value = seeded.generate();
            assertEquals(skipped, localDate(value), describe(value));
            zones.add(value.getTimeZone().getID());
            Calendar other = unseeded.generate();
            assertEquals(skipped, localDate(other), describe(other));
        }

        assertFalse(zones.contains("Pacific/Apia"), zones.toString());
        assertFalse(zones.contains("Pacific/Fakaofo"), zones.toString());
    }

    @Test
    @DisplayName("a date-time a zone skips is held at UTC where moving it on would pass the range")
    void aDateTimeAZoneSkipsIsHeldAtUtcWhereMovingItOnWouldPassTheRange() {
        // The whole of 2011-12-30 in Pacific/Apia, and 23:00 to midnight on 2024-03-30, a DST gap in America/Nuuk.
        LocalDate skippedDay = LocalDate.of(2011, 12, 30);
        Calendar apia = new CalendarGenerator(skippedDay, skippedDay, scripted(10, 0, 0, "Pacific/Apia")).generate();
        assertEquals(Instant.parse("2011-12-30T10:00:00Z"), apia.toInstant());
        assertEquals("UTC", apia.getTimeZone().getID());
        assertInstanceOf(GregorianCalendar.class, apia);
        assertFalse(apia.isLenient());

        LocalDate gapDay = LocalDate.of(2024, 3, 30);
        Calendar nuuk = new CalendarGenerator(gapDay, gapDay, scripted(23, 30, 30, "America/Nuuk")).generate();
        assertEquals(Instant.parse("2024-03-30T23:30:30Z"), nuuk.toInstant());
        assertEquals("UTC", nuuk.getTimeZone().getID());
    }

    @Test
    @DisplayName("a date-time a zone skips moves on in its zone where the range includes the next day")
    void aDateTimeAZoneSkipsMovesOnInItsZoneWhereTheRangeIncludesTheNextDay() {
        LocalDate gapDay = LocalDate.of(2024, 3, 30);
        Calendar nuuk =
                new CalendarGenerator(gapDay, gapDay.plusDays(1), scripted(23, 30, 30, "America/Nuuk")).generate();
        assertEquals(
                ZonedDateTime.parse("2024-03-31T00:30:30-01:00[America/Nuuk]").toInstant(), nuuk.toInstant());
        assertEquals("America/Nuuk", nuuk.getTimeZone().getID());

        LocalDate skippedDay = LocalDate.of(2011, 12, 30);
        Calendar apia = new CalendarGenerator(skippedDay, skippedDay.plusDays(1), scripted(10, 0, 0, "Pacific/Apia"))
                .generate();
        assertEquals(LocalDate.of(2011, 12, 31), localDate(apia));
        assertEquals("Pacific/Apia", apia.getTimeZone().getID());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGenerationIsReproducible() {
        CalendarGenerator first =
                new CalendarGenerator(GeneratorConfig.builder().seed(1234L).build());
        CalendarGenerator second =
                new CalendarGenerator(GeneratorConfig.builder().seed(1234L).build());

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
        assertNotNull(Generators.ofCalendar(seeded(1L)).generate());
        assertNotNull(Generators.ofCalendar(GeneratorConfig.defaults()).generate());
        assertNotNull(Generators.ofCalendar(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2))
                .generate());
        assertNotNull(Generators.ofCalendar(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 2),
                        GeneratorConfig.builder().seed(2L).build())
                .generate());
        assertInstanceOf(
                GregorianCalendar.class, Generators.forType(Calendar.class).generate());
        assertInstanceOf(
                GregorianCalendar.class,
                Generators.forType(GregorianCalendar.class).generate());
    }

    @Test
    @DisplayName("invalid constructor arguments fail fast")
    void invalidConstructorArgumentsFailFast() {
        assertThrows(NullPointerException.class, () -> new CalendarGenerator((GeneratorConfig) null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CalendarGenerator(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CalendarGenerator(LocalDate.of(2024, 1, 1), null, GeneratorConfig.defaults()));
    }

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).build();
    }

    /** A configuration that draws the first date of a range, at this time of day, in this zone. */
    private static GeneratorConfig scripted(int hour, int minute, int second, String zone) {
        int zoneIndex =
                new TreeSet<>(ZoneId.getAvailableZoneIds()).headSet(zone).size();
        return GeneratorConfig.builder()
                .random(new ScriptedRandom(hour, minute, second, zoneIndex))
                .build();
    }

    private static LocalDate localDate(Calendar calendar) {
        return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    private static String describe(Calendar calendar) {
        return calendar.toInstant() + " in " + calendar.getTimeZone().getID();
    }

    /** Returns 0 from {@link #nextLong(long)}, and the scripted values from {@link #nextInt(int)} in order. */
    private static final class ScriptedRandom extends Random {

        private final int[] values;
        private int next;

        ScriptedRandom(int... values) {
            this.values = values;
        }

        @Override
        public long nextLong(long bound) {
            return 0L;
        }

        @Override
        public int nextInt(int bound) {
            return values[next++];
        }
    }
}
