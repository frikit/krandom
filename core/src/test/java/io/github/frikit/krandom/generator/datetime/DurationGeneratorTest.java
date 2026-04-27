/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("between(Duration,Duration) respects inclusive range")
    void betweenDuration() {
        DurationGenerator gen = new DurationGenerator(GeneratorConfig.builder().seed(7L).build());
        Duration min = Duration.ofMinutes(5);
        Duration max = Duration.ofMinutes(15);
        for (int i = 0; i < 30; i++) {
            Duration d = gen.between(min, max);
            assertTrue(d.compareTo(min) >= 0 && d.compareTo(max) <= 0);
        }
        assertEquals(Duration.ofMinutes(10), gen.between(Duration.ofMinutes(10), Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("between(Duration,Duration) truncates sub-second components")
    void betweenDurationTruncatesNanos() {
        DurationGenerator gen = new DurationGenerator();
        Duration min = Duration.ofSeconds(5).plusNanos(900_000_000);
        Duration max = Duration.ofSeconds(6).plusNanos(100_000_000);
        Duration result = gen.between(min, max);
        assertEquals(Duration.ofSeconds(result.toSeconds()), result);
        assertTrue(result.toSeconds() >= 5 && result.toSeconds() <= 6);
    }

    @Test
    @DisplayName("between(Duration,Duration) validates arguments")
    void betweenDurationValidates() {
        DurationGenerator gen = new DurationGenerator();
        assertThrows(NullPointerException.class, () -> gen.between(null, Duration.ofSeconds(1)));
        assertThrows(NullPointerException.class, () -> gen.between(Duration.ofSeconds(1), null));
        assertThrows(IllegalArgumentException.class, () -> gen.between(Duration.ofSeconds(-1), Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> gen.between(Duration.ofSeconds(5), Duration.ofSeconds(1)));
    }
}
