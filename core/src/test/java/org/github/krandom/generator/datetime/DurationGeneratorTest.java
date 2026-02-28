/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DurationGenerator")
class DurationGeneratorTest {

    @Test
    @DisplayName("generate() returns positive duration")
    void generateDefault() {
        Duration d = new DurationGenerator().generate();
        assertNotNull(d);
        assertTrue(d.getSeconds() >= 1);
    }

    @Test
    @DisplayName("betweenSeconds(min,max) respects inclusive range")
    void betweenSeconds() {
        DurationGenerator gen = new DurationGenerator(GeneratorConfig.builder().seed(41L).build());
        for (int i = 0; i < 30; i++) {
            Duration d = gen.betweenSeconds(10, 20);
            assertTrue(d.getSeconds() >= 10 && d.getSeconds() <= 20);
        }
        assertEquals(Duration.ofSeconds(15), gen.betweenSeconds(15, 15));
    }

    @Test
    @DisplayName("betweenSeconds validates range")
    void betweenSecondsValidation() {
        DurationGenerator gen = new DurationGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.betweenSeconds(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> gen.betweenSeconds(10, 9));
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).build();
        DurationGenerator a = new DurationGenerator(cfg);
        DurationGenerator b = new DurationGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new DurationGenerator(null));
    }
}
