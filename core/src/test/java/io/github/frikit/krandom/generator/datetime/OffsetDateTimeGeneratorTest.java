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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OffsetDateTimeGenerator")
class OffsetDateTimeGeneratorTest {

    private static final Set<ZoneOffset> CONVENTIONAL_OFFSETS = IntStream.rangeClosed(-12 * 4, 14 * 4)
            .mapToObj(quarterHours -> ZoneOffset.ofTotalSeconds(quarterHours * 15 * 60))
            .collect(Collectors.toSet());

    @Test
    @DisplayName("generate() returns a non-null offset date-time with a valid offset")
    void generateDefault() {
        OffsetDateTime odt = new OffsetDateTimeGenerator().generate();
        assertNotNull(odt);
        assertTrue(CONVENTIONAL_OFFSETS.contains(odt.getOffset()), odt.toString());
    }

    @Test
    @DisplayName("generate() draws every quarter hour from -12:00 to +14:00, and nothing else")
    void generateDrawsOnlyTheConventionalOffsets() {
        OffsetDateTimeGenerator gen =
                new OffsetDateTimeGenerator(GeneratorConfig.builder().seed(99L).build());
        Set<ZoneOffset> drawn = new HashSet<>();
        for (int i = 0; i < 5_000; i++) {
            drawn.add(gen.generate().getOffset());
        }
        assertEquals(CONVENTIONAL_OFFSETS, drawn);
    }

    @Test
    @DisplayName("between(start,end) re-expresses the instant at a conventional offset, whatever the bounds' offsets")
    void betweenDrawsOnlyTheConventionalOffsets() {
        OffsetDateTimeGenerator gen =
                new OffsetDateTimeGenerator(GeneratorConfig.builder().seed(99L).build());
        ZoneOffset outsideTheConvention = ZoneOffset.of("+18:00");
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(2025, 1, 1, 0, 0), outsideTheConvention);
        OffsetDateTime end = start.plusHours(1);
        Set<ZoneOffset> drawn = new HashSet<>();
        for (int i = 0; i < 5_000; i++) {
            OffsetDateTime sample = gen.between(start, end);
            drawn.add(sample.getOffset());
            assertFalse(sample.toInstant().isBefore(start.toInstant()), sample.toString());
            assertFalse(sample.toInstant().isAfter(end.toInstant()), sample.toString());
        }
        assertEquals(CONVENTIONAL_OFFSETS, drawn);
    }

    @Test
    @DisplayName("between(start,end) stays within instant range")
    void betweenWithinInstantRange() {
        OffsetDateTimeGenerator gen =
                new OffsetDateTimeGenerator(GeneratorConfig.builder().seed(99L).build());
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(2025, 1, 1, 0, 0), ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(LocalDateTime.of(2025, 12, 31, 23, 59), ZoneOffset.UTC);
        for (int i = 0; i < 30; i++) {
            OffsetDateTime sample = gen.between(start, end);
            assertTrue(!sample.toInstant().isBefore(start.toInstant()));
            assertTrue(!sample.toInstant().isAfter(end.toInstant()));
        }
    }

    @Test
    @DisplayName("between(start,end) returns the same instant when bounds are equal")
    void betweenEqualBounds() {
        OffsetDateTimeGenerator gen = new OffsetDateTimeGenerator();
        OffsetDateTime fixed = OffsetDateTime.of(LocalDateTime.of(2030, 6, 15, 12, 0), ZoneOffset.ofHours(2));
        OffsetDateTime result = gen.between(fixed, fixed);
        assertEquals(fixed.toInstant(), result.toInstant());
    }

    @Test
    @DisplayName("between(start,end) validates inputs")
    void betweenValidation() {
        OffsetDateTimeGenerator gen = new OffsetDateTimeGenerator();
        OffsetDateTime ref = OffsetDateTime.of(LocalDateTime.of(2025, 1, 1, 0, 0), ZoneOffset.UTC);
        assertThrows(NullPointerException.class, () -> gen.between(null, ref));
        assertThrows(NullPointerException.class, () -> gen.between(ref, null));
        assertThrows(IllegalArgumentException.class, () -> gen.between(ref.plusDays(1), ref));
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).build();
        OffsetDateTimeGenerator a = new OffsetDateTimeGenerator(cfg);
        OffsetDateTimeGenerator b = new OffsetDateTimeGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new OffsetDateTimeGenerator(null));
    }
}
