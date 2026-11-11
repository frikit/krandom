/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocalDateTimeGenerator")
class LocalDateTimeGeneratorTest {

    private static final int SAMPLES = 50;

    @Test
    @DisplayName("generate() returns a non-null value")
    void generatesNonNull() {
        assertNotNull(new LocalDateTimeGenerator().generate());
    }

    @Test
    @DisplayName("generated year is in [1970, 2100]")
    void yearInRange() {
        LocalDateTimeGenerator gen = new LocalDateTimeGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            int year = gen.generate().getYear();
            assertTrue(year >= 1970 && year <= 2100, "year out of range: " + year);
        }
    }

    @Test
    @DisplayName("generated month is in [1, 12]")
    void monthInRange() {
        LocalDateTimeGenerator gen = new LocalDateTimeGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            int month = gen.generate().getMonthValue();
            assertTrue(month >= 1 && month <= 12, "month out of range: " + month);
        }
    }

    @Test
    @DisplayName("generated hour is in [0, 23]")
    void hourInRange() {
        LocalDateTimeGenerator gen = new LocalDateTimeGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            int hour = gen.generate().getHour();
            assertTrue(hour >= 0 && hour <= 23, "hour out of range: " + hour);
        }
    }

    @Test
    @DisplayName("seeded generator produces reproducible output")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(77L).build();
        LocalDateTimeGenerator g1 = new LocalDateTimeGenerator(cfg);
        LocalDateTimeGenerator g2 = new LocalDateTimeGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(g1.generate(), g2.generate(), "seeded generators must produce identical output");
        }
    }

    @Test
    @DisplayName("generateList returns correct count of non-null values")
    void generateList() {
        var list = new LocalDateTimeGenerator().generateList(10);
        assertEquals(10, list.size());
        list.forEach(dt -> assertNotNull(dt));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new LocalDateTimeGenerator(null));
    }

    @Test
    @DisplayName("between(start,end) returns value in inclusive range")
    void between() {
        LocalDateTimeGenerator gen =
                new LocalDateTimeGenerator(GeneratorConfig.builder().seed(91L).build());
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 2, 0, 0, 0);
        for (int i = 0; i < 40; i++) {
            LocalDateTime value = gen.between(start, end);
            assertFalse(value.isBefore(start));
            assertFalse(value.isAfter(end));
        }
        assertEquals(start, gen.between(start, start));
        assertThrows(IllegalArgumentException.class, () -> gen.between(end, start));
        assertThrows(NullPointerException.class, () -> gen.between(null, end));
        assertThrows(NullPointerException.class, () -> gen.between(start, null));
    }

    @Test
    @DisplayName("before(reference) and after(reference) obey strict bounds")
    void beforeAfter() {
        LocalDateTimeGenerator gen =
                new LocalDateTimeGenerator(GeneratorConfig.builder().seed(92L).build());
        LocalDateTime ref = LocalDateTime.of(2050, 1, 1, 0, 0, 0);
        for (int i = 0; i < 40; i++) {
            assertTrue(gen.before(ref).isBefore(ref));
            assertTrue(gen.after(ref).isAfter(ref));
        }
        assertThrows(NullPointerException.class, () -> gen.before(null));
        assertThrows(NullPointerException.class, () -> gen.after(null));
        assertThrows(IllegalArgumentException.class, () -> gen.before(LocalDateTime.of(1970, 1, 1, 0, 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> gen.after(LocalDateTime.of(2100, 12, 31, 23, 59, 59)));
    }

    @Test
    @DisplayName("generateIso8601 returns UTC offset date-time")
    void iso8601Format() {
        String value =
                new LocalDateTimeGenerator(GeneratorConfig.builder().seed(93L).build()).generateIso8601();
        assertTrue(value.endsWith("Z") || value.matches(".*[+-]\\d{2}:\\d{2}$"));
        assertTrue(value.contains("T"));
    }

    @Test
    @DisplayName("future/past helpers obey relative ranges")
    void futurePast() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC"));
        LocalDateTimeGenerator gen = new LocalDateTimeGenerator(
                GeneratorConfig.builder().seed(94L).clock(clock).build());
        LocalDateTime now = LocalDateTime.now(clock);
        assertTrue(gen.future().isAfter(now));
        assertTrue(gen.past().isBefore(now));
        assertThrows(IllegalArgumentException.class, () -> gen.future(0));
        assertThrows(IllegalArgumentException.class, () -> gen.past(0));
    }

    @Test
    @DisplayName("future(days) stays in [now + 1 s, now + days] and past(days) in [now - days, now - 1 s]")
    void futureAndPastStayInTheirDocumentedRanges() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC"));
        LocalDateTime now = LocalDateTime.now(clock);

        // The draw one past the range gives the range's end; every other draw keeps its value.
        LocalDateTimeGenerator future = scripted(clock, 86_400L, 86_399L, 0L);
        assertEquals(now.plusDays(1), future.future(1));
        assertEquals(now.plusDays(1), future.future(1));
        assertEquals(now.plusSeconds(1), future.future(1));

        LocalDateTimeGenerator past = scripted(clock, 0L, 1L, 86_400L);
        assertEquals(now.minusDays(1), past.past(1));
        assertEquals(now.minusDays(1), past.past(1));
        assertEquals(now.minusSeconds(1), past.past(1));
    }

    @Test
    @DisplayName("future and past keep the value of every draw in their range, as before")
    void futureAndPastKeepTheirDrawsInRange() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC"));
        LocalDateTime now = LocalDateTime.now(clock);
        GeneratorConfig config =
                GeneratorConfig.builder().seed(94L).clock(clock).build();
        LocalDateTimeGenerator gen = new LocalDateTimeGenerator(config);
        java.util.Random reference = config.createRandom();

        for (int i = 0; i < 1_000; i++) {
            int days = 1 + i % 3;
            LocalDateTime later = now.plusSeconds(1 + reference.nextLong(days * 86_400L + 1));
            assertEquals(later.isAfter(now.plusDays(days)) ? now.plusDays(days) : later, gen.future(days));

            LocalDateTime earlier =
                    now.minusSeconds(1 + days * 86_400L).plusSeconds(reference.nextLong(days * 86_400L + 1));
            assertEquals(earlier.isBefore(now.minusDays(days)) ? now.minusDays(days) : earlier, gen.past(days));
        }
    }

    /** A generator whose bounded long draws are {@code draws}, in order. */
    private static LocalDateTimeGenerator scripted(Clock clock, long... draws) {
        java.util.Random source = new java.util.Random() {
            private static final long serialVersionUID = 1L;
            private int next;

            @Override
            public long nextLong(long bound) {
                return draws[next++];
            }
        };
        return new LocalDateTimeGenerator(
                GeneratorConfig.builder().random(source).clock(clock).build());
    }
}
