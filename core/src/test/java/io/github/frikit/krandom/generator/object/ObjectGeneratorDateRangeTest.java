/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.core.model.PersonWithDateTimes;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
}
