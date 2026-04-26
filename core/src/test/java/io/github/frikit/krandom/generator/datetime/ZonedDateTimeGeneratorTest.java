/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ZonedDateTimeGenerator")
class ZonedDateTimeGeneratorTest {

    private static final int SAMPLES = 50;

    @Test
    @DisplayName("generate() returns a non-null ZonedDateTime")
    void generatesNonNull() {
        assertNotNull(new ZonedDateTimeGenerator().generate());
    }

    @Test
    @DisplayName("generated ZonedDateTime has a non-null zone")
    void hasNonNullZone() {
        ZonedDateTimeGenerator gen = new ZonedDateTimeGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            assertNotNull(gen.generate().getZone(), "zone must not be null");
        }
    }

    @Test
    @DisplayName("generated zone is a valid known zone ID")
    void zoneIsValid() {
        Set<String> available = ZoneId.getAvailableZoneIds();
        ZonedDateTimeGenerator gen = new ZonedDateTimeGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            ZoneId zone = gen.generate().getZone();
            assertTrue(available.contains(zone.getId()),
                       "unknown zone ID: " + zone.getId());
        }
    }

    @Test
    @DisplayName("generated year is in [1970, 2100]")
    void yearInRange() {
        ZonedDateTimeGenerator gen = new ZonedDateTimeGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            int year = gen.generate().getYear();
            assertTrue(year >= 1970 && year <= 2100, "year out of range: " + year);
        }
    }

    @Test
    @DisplayName("multiple zone IDs appear over many calls")
    void multipleZonesGenerated() {
        Set<String> zones = new HashSet<>();
        ZonedDateTimeGenerator gen = new ZonedDateTimeGenerator();
        for (int i = 0; i < 200; i++) zones.add(gen.generate().getZone().getId());
        assertTrue(zones.size() > 1, "expected multiple zones, only got: " + zones);
    }

    @Test
    @DisplayName("seeded generator produces reproducible output")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(33L).build();
        ZonedDateTimeGenerator g1 = new ZonedDateTimeGenerator(cfg);
        ZonedDateTimeGenerator g2 = new ZonedDateTimeGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            ZonedDateTime z1 = g1.generate();
            ZonedDateTime z2 = g2.generate();
            assertEquals(z1.toInstant(), z2.toInstant(), "timestamps must match for same seed");
            assertEquals(z1.getZone(), z2.getZone(), "zones must match for same seed");
        }
    }

    @Test
    @DisplayName("generateList returns correct count of non-null values")
    void generateList() {
        var list = new ZonedDateTimeGenerator().generateList(10);
        assertEquals(10, list.size());
        list.forEach(zdt -> assertNotNull(zdt));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new ZonedDateTimeGenerator(null));
    }
}
