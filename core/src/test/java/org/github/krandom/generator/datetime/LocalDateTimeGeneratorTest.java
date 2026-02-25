/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
            assertTrue(year >= 1970 && year <= 2100,
                    "year out of range: " + year);
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
            assertEquals(g1.generate(), g2.generate(),
                    "seeded generators must produce identical output");
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
}
