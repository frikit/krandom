/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InstantGenerator")
class InstantGeneratorTest {

    private static final int SAMPLES = 50;

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
            assertTrue(gen.generate().getEpochSecond() >= 0,
                       "epoch second must be non-negative");
        }
    }

    @Test
    @DisplayName("generated epoch second is at most 2100-12-31 in UTC")
    void epochSecondBeforeYear2101() {
        // 2101-01-01T00:00:00Z in epoch seconds ≈ 4133980800
        long maxEpoch = Instant.parse("2101-01-01T00:00:00Z").getEpochSecond();
        InstantGenerator gen = new InstantGenerator();
        for (int i = 0; i < SAMPLES; i++) {
            assertTrue(gen.generate().getEpochSecond() < maxEpoch,
                       "epoch second must be before year 2101");
        }
    }

    @Test
    @DisplayName("seeded generator produces reproducible output")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(55L).build();
        InstantGenerator g1 = new InstantGenerator(cfg);
        InstantGenerator g2 = new InstantGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(g1.generate(), g2.generate(),
                         "seeded generators must produce identical output");
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
