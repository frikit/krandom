/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.LongUnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InstantGenerator")
class InstantGeneratorTest {

    private static final int SAMPLES = 50;

    @Test
    @DisplayName("generate() samples a time of day, with fractional seconds, to the millisecond")
    void samplesATimeOfDayToTheMillisecond() {
        InstantGenerator gen =
                new InstantGenerator(GeneratorConfig.builder().seed(55L).build());
        Set<Integer> hours = new HashSet<>();
        boolean fractionalSecond = false;

        for (int i = 0; i < 2_000; i++) {
            Instant instant = gen.generate();
            assertEquals(0, instant.getNano() % 1_000_000, instant.toString());
            hours.add(instant.atOffset(ZoneOffset.UTC).getHour());
            fractionalSecond |= instant.getNano() != 0;
        }

        assertEquals(24, hours.size(), "every hour of the UTC day must be reachable: " + hours);
        assertTrue(fractionalSecond, "some instants must have a fraction of a second");
    }

    @Test
    @DisplayName("the lowest and highest draws are the first and last milliseconds of the default range")
    void extremeDrawsAreTheFirstAndLastMillisecondsOfTheDefaultRange() {
        assertEquals(Instant.parse("1970-01-01T00:00:00Z"), new InstantGenerator(drawing(bound -> 0L)).generate());
        assertEquals(
                Instant.parse("2100-12-31T23:59:59.999Z"),
                new InstantGenerator(drawing(bound -> bound - 1)).generate());
    }

    @Test
    @DisplayName("a date range runs from the start of its first UTC day through the end of its last")
    void dateRangeRunsFromTheStartOfItsFirstDayThroughTheEndOfItsLast() {
        LocalDate min = LocalDate.of(2024, 2, 28);
        LocalDate max = LocalDate.of(2024, 3, 1);
        InstantGenerator gen = new InstantGenerator(min, max);
        gen.reseed(55L);
        Set<LocalDate> days = new HashSet<>();
        boolean startOfFirstDay = false;
        boolean endOfLastDay = false;

        for (int i = 0; i < 2_000; i++) {
            LocalDateTime utc = LocalDateTime.ofInstant(gen.generate(), ZoneOffset.UTC);
            days.add(utc.toLocalDate());
            startOfFirstDay |= utc.toLocalDate().equals(min) && utc.getHour() == 0;
            endOfLastDay |= utc.toLocalDate().equals(max) && utc.getHour() == 23;
        }

        assertEquals(Set.of(min, LocalDate.of(2024, 2, 29), max), days);
        assertTrue(startOfFirstDay, "the first hour of the first day must be reachable");
        assertTrue(endOfLastDay, "the last hour of the last day must be reachable");
    }

    @Test
    @DisplayName("a single-day range stays on that UTC day, and its instants can differ")
    void singleDayRangeStaysOnThatDayAndItsInstantsCanDiffer() {
        List<LocalDate> days = List.of(
                LocalDate.MIN,
                LocalDate.of(1970, 1, 1),
                LocalDate.of(2024, 2, 29),
                LocalDate.of(2100, 12, 31),
                LocalDate.MAX);

        for (LocalDate day : days) {
            InstantGenerator gen = new InstantGenerator(day, day);
            gen.reseed(55L);
            Set<Instant> instants = new HashSet<>();
            for (int i = 0; i < 200; i++) {
                Instant instant = gen.generate();
                assertEquals(day, instant.atOffset(ZoneOffset.UTC).toLocalDate(), instant.toString());
                instants.add(instant);
            }
            assertTrue(instants.size() > 1, "one day holds more than one instant: " + day);
        }
    }

    /** A configuration whose random source answers each bounded draw with {@code draw} of that bound. */
    private static GeneratorConfig drawing(LongUnaryOperator draw) {
        Random source = new Random() {
            @Override
            public int nextInt(int bound) {
                return (int) draw.applyAsLong(bound);
            }

            @Override
            public long nextLong(long bound) {
                return draw.applyAsLong(bound);
            }
        };
        return GeneratorConfig.builder().random(source).build();
    }

    @Test
    @DisplayName("generate() returns a non-null Instant")
    void generatesNonNull() {
        assertNotNull(new InstantGenerator().generate());
    }

    @Test
    @DisplayName("generated epoch second is non-negative (post-1970)")
    void epochSecondNonNegative() {
        InstantGenerator gen = new InstantGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            assertTrue(gen.generate().getEpochSecond() >= 0, "epoch second must be non-negative");
        }
    }

    @Test
    @DisplayName("generated epoch second is at most 2100-12-31 in UTC")
    void epochSecondBeforeYear2101() {
        // 2101-01-01T00:00:00Z in epoch seconds ≈ 4133980800
        long maxEpoch = Instant.parse("2101-01-01T00:00:00Z").getEpochSecond();
        InstantGenerator gen = new InstantGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            assertTrue(gen.generate().getEpochSecond() < maxEpoch, "epoch second must be before year 2101");
        }
    }

    @Test
    @DisplayName("seeded generator produces reproducible output")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(55L).build();
        InstantGenerator g1 = new InstantGenerator(cfg);
        InstantGenerator g2 = new InstantGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(g1.generate(), g2.generate(), "seeded generators must produce identical output");
        }
    }

    @Test
    @DisplayName("generateList returns correct count of non-null values")
    void generateList() {
        var list = new InstantGenerator().generateList(10);
        assertEquals(10, list.size());
        list.forEach(instant -> assertNotNull(instant));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new InstantGenerator(null));
    }
}
