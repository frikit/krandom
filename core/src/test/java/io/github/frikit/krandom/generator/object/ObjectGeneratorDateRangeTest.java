/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.core.model.PersonWithDateTimes;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectGenerator — global date range configuration")
class ObjectGeneratorDateRangeTest {

    private static final LocalDate MIN = LocalDate.of(2020, 1, 1);
    private static final LocalDate MAX = LocalDate.of(2023, 12, 31);
    private static final LocalDate DAY = LocalDate.of(2024, 2, 29);
    private static final int SAMPLES = 50;

    private final ObjectGeneratorConfig config =
            ObjectGeneratorConfig.builder().dateRange(MIN, MAX).build();

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
    @DisplayName("Instant fields are drawn anywhere in a configured UTC day, seeded or not")
    void instantFieldsAreDrawnAnywhereInAConfiguredDay() {
        GeneratorConfig unseeded =
                GeneratorConfig.builder().objectDateRange(DAY, DAY).build();

        for (GeneratorConfig config :
                List.of(unseeded, unseeded.toBuilder().seed(42L).build())) {
            ObjectGenerator<InstantFields> gen = new ObjectGenerator<>(InstantFields.class, config);
            Set<Integer> structuralHours = new HashSet<>();
            Set<Integer> semanticHours = new HashSet<>();
            for (int i = 0; i < 1_000; i++) {
                InstantFields value = gen.generate();
                assertEquals(DAY, value.stamp.atOffset(ZoneOffset.UTC).toLocalDate(), value.stamp.toString());
                assertEquals(DAY, value.createdAt.atOffset(ZoneOffset.UTC).toLocalDate(), value.createdAt.toString());
                structuralHours.add(value.stamp.atOffset(ZoneOffset.UTC).getHour());
                semanticHours.add(value.createdAt.atOffset(ZoneOffset.UTC).getHour());
            }
            assertEquals(24, structuralHours.size(), "a plain Instant field: " + structuralHours);
            assertEquals(24, semanticHours.size(), "a semantic Instant field: " + semanticHours);
        }
    }

    @Test
    @DisplayName("a configured maximum includes its whole UTC day, even past a fixed clock")
    void configuredMaximumIncludesItsWholeDayPastAFixedClock() {
        Instant noon = DAY.atTime(12, 0).toInstant(ZoneOffset.UTC);
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .clock(Clock.fixed(noon, ZoneOffset.UTC))
                .objectDateRange(DAY, DAY)
                .build();
        ObjectGenerator<InstantFields> gen = new ObjectGenerator<>(InstantFields.class, config);
        boolean passedTheClock = false;

        for (int i = 0; i < 200; i++) {
            Instant createdAt = gen.generate().createdAt;
            assertEquals(DAY, createdAt.atOffset(ZoneOffset.UTC).toLocalDate(), createdAt.toString());
            passedTheClock |= createdAt.isAfter(noon);
        }

        assertTrue(passedTheClock, "the afternoon of the configured day must be reachable");
    }

    @Test
    @DisplayName("zoned and offset fields stay on a one-day range that a zone skipped, seeded or not")
    void zonedAndOffsetFieldsStayOnAOneDayRangeThatAZoneSkipped() {
        // Pacific/Apia and Pacific/Fakaofo went from 2011-12-29 to 2011-12-31.
        LocalDate skipped = LocalDate.of(2011, 12, 30);
        GeneratorConfig unseeded =
                GeneratorConfig.builder().objectDateRange(skipped, skipped).build();

        for (GeneratorConfig config :
                List.of(unseeded, unseeded.toBuilder().seed(42L).build())) {
            ObjectGenerator<SkippedDayFields> gen = new ObjectGenerator<>(SkippedDayFields.class, config);
            for (int i = 0; i < 20_000; i++) {
                SkippedDayFields value = gen.generate();
                assertEquals(skipped, value.stamp.toLocalDate(), value.stamp.toString());
                assertEquals(skipped, value.moment.toLocalDate(), value.moment.toString());
                assertEquals(skipped, value.createdAt.toLocalDate(), value.createdAt.toString());
                assertEquals(skipped, value.dateOfBirth.toLocalDate(), value.dateOfBirth.toString());
            }
        }
    }

    @Test
    @DisplayName("a java.sql.Date member reads back as a day in the range, seeded or not")
    void sqlDateMemberReadsBackInTheRange() {
        // 5 to 14 October 1582 read back ten days later, as the Julian days they are.
        assertSqlDatesWithin(LocalDate.of(1582, 10, 1), LocalDate.of(1582, 10, 15), LocalDate.of(1582, 10, 15));
        assertSqlDatesWithin(LocalDate.of(1582, 10, 1), LocalDate.of(1582, 10, 10), LocalDate.of(1582, 10, 4));
    }

    /**
     * Both a plain member and a semantic one, whose clock-relative range {@code objectDateRange} replaces, read
     * back in the range, and a drawn day that would not gives {@code lastHeld}.
     */
    private static void assertSqlDatesWithin(LocalDate min, LocalDate max, LocalDate lastHeld) {
        GeneratorConfig unseeded =
                GeneratorConfig.builder().objectDateRange(min, max).build();
        for (GeneratorConfig config :
                List.of(unseeded, unseeded.toBuilder().seed(42L).build())) {
            ObjectGenerator<SqlDateFields> gen = new ObjectGenerator<>(SqlDateFields.class, config);
            Set<LocalDate> days = new HashSet<>();
            for (int i = 0; i < 2_000; i++) {
                SqlDateFields value = gen.generate();
                for (java.sql.Date date : List.of(value.day, value.createdAt)) {
                    LocalDate day = date.toLocalDate();
                    assertFalse(day.isBefore(min) || day.isAfter(max), day.toString());
                    days.add(day);
                }
            }
            assertTrue(days.contains(lastHeld), days.toString());
        }
    }

    @Test
    @DisplayName("a java.sql.Date member of a range beyond the instants it holds reads back in the range")
    void sqlDateMemberOfARangeBeyondItsInstantsReadsBackInTheRange() {
        // Seeded, past the last instant a java.sql.Date holds, where a day reads back as another or as no date.
        assertSqlDatesWithin(
                GeneratorConfig.builder()
                        .seed(42L)
                        .objectDateRange(LocalDate.of(292_278_990, 1, 1), LocalDate.of(292_278_999, 12, 31))
                        .build(),
                LocalDate.of(292_278_990, 1, 1),
                LocalDate.of(292_278_999, 12, 31));
        // Unseeded, every draw the first day: 29 February 5 BC reads back as 29 February of the year 5, no date.
        java.util.Random first = new java.util.Random() {
            private static final long serialVersionUID = 1L;

            @Override
            public long nextLong(long origin, long bound) {
                return origin;
            }
        };
        SqlDateFields value = new ObjectGenerator<>(
                        SqlDateFields.class,
                        GeneratorConfig.builder()
                                .random(first)
                                .objectDateRange(LocalDate.of(-4, 2, 29), LocalDate.of(1, 1, 1))
                                .build())
                .generate();
        assertEquals(LocalDate.of(1, 1, 1), value.day.toLocalDate());
        assertEquals(LocalDate.of(1, 1, 1), value.createdAt.toLocalDate());
    }

    private static void assertSqlDatesWithin(GeneratorConfig config, LocalDate min, LocalDate max) {
        ObjectGenerator<SqlDateFields> gen = new ObjectGenerator<>(SqlDateFields.class, config);
        for (int i = 0; i < 500; i++) {
            LocalDate day = gen.generate().day.toLocalDate();
            assertFalse(day.isBefore(min) || day.isAfter(max), day.toString());
        }
    }

    @Test
    @DisplayName("a @Fake java.sql.Date member of a range that holds no such day fails as its constraint conflict")
    void fakeSqlDateMemberOfARangeWithoutAHeldDayFails() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .objectDateRange(LocalDate.of(0, 1, 1), LocalDate.of(0, 12, 31))
                .build();
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();

        ObjectGenerationException failure = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(FakeSqlDateFields.class, config).generate());
        FakeSqlDateFields lenient = new ObjectGenerator<>(
                        FakeSqlDateFields.class,
                        config.toBuilder()
                                .objectIgnoreErrors(true)
                                .generationFailureListener(diagnostics::add)
                                .build())
                .generate();

        assertEquals(
                "FakeSqlDateFields.stamp", failure.getContext().orElseThrow().path());
        assertNull(lenient.stamp);
        assertEquals(
                List.of("FakeSqlDateFields.stamp"),
                diagnostics.stream().map(d -> d.context().path()).toList());
    }

    @Test
    @DisplayName("a range that holds no day a java.sql.Date holds fails as a constraint conflict of the member")
    void rangeWithoutAHeldDayFailsForASqlDateMember() {
        for (LocalDate[] range : List.of(
                new LocalDate[] {LocalDate.of(0, 1, 1), LocalDate.of(0, 12, 31)},
                new LocalDate[] {LocalDate.of(1582, 10, 5), LocalDate.of(1582, 10, 14)})) {
            GeneratorConfig unseeded = GeneratorConfig.builder()
                    .objectDateRange(range[0], range[1])
                    .build();
            for (GeneratorConfig config :
                    List.of(unseeded, unseeded.toBuilder().seed(42L).build())) {
                ObjectGenerationException failure = assertThrows(
                        ObjectGenerationException.class,
                        () -> new ObjectGenerator<>(SqlDateFields.class, config).generate());
                GenerationFailureContext context = failure.getContext().orElseThrow();
                assertEquals("SqlDateFields.createdAt", context.path());
                assertTrue(
                        failure.getCause().getMessage().startsWith("no day from " + range[0] + " to " + range[1]),
                        failure.getCause().getMessage());

                List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
                SqlDateFields lenient = new ObjectGenerator<>(
                                SqlDateFields.class,
                                config.toBuilder()
                                        .objectIgnoreErrors(true)
                                        .generationFailureListener(diagnostics::add)
                                        .build())
                        .generate();
                assertNull(lenient.day);
                assertNull(lenient.createdAt);
                assertEquals(
                        List.of("SqlDateFields.createdAt", "SqlDateFields.day"),
                        diagnostics.stream().map(d -> d.context().path()).toList());
                assertEquals(
                        range[0].getYear(),
                        new ObjectGenerator<>(LocalDateFields.class, config)
                                .generate()
                                .day
                                .getYear());
            }
        }
    }

    @Test
    @DisplayName("dateRange(min > max) throws IllegalArgumentException")
    void invalidRangeThrows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ObjectGeneratorConfig.builder().dateRange(MAX, MIN).build());
    }

    static class InstantFields {

        Instant stamp;
        Instant createdAt;
    }

    static class SqlDateFields {

        java.sql.Date createdAt;
        java.sql.Date day;
    }

    static class FakeSqlDateFields {

        @Fake("createdAt")
        java.sql.Date stamp;
    }

    static class LocalDateFields {

        LocalDate day;
    }

    static class SkippedDayFields {

        ZonedDateTime stamp;
        OffsetDateTime moment;
        ZonedDateTime createdAt;
        OffsetDateTime dateOfBirth;
    }
}
