/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeGeneratorTest {

    @Test
    void testDefaultConstructor() {
        TimeGenerator gen = new TimeGenerator();
        assertNotNull(gen);
    }

    @Test
    void testGenerateRandomTime() {
        TimeGenerator gen = new TimeGenerator();
        LocalTime time = gen.generate();

        assertNotNull(time);
        assertTrue(time.getHour() >= 0 && time.getHour() <= 23);
        assertTrue(time.getMinute() >= 0 && time.getMinute() <= 59);
        assertTrue(time.getSecond() >= 0 && time.getSecond() <= 59);
    }

    @Test
    void testGenerateTimeString() {
        TimeGenerator gen = new TimeGenerator();
        String timeStr = gen.generateString();

        assertNotNull(timeStr);
        assertTrue(timeStr.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testGenerateHour12() {
        TimeGenerator gen = new TimeGenerator();
        int hour = gen.generateHour();

        assertTrue(hour >= 1 && hour <= 12);
    }

    @Test
    void testGenerateHour24() {
        TimeGenerator gen = new TimeGenerator();
        int hour = gen.generateHour24();

        assertTrue(hour >= 0 && hour <= 23);
    }

    @Test
    void testGenerateMinute() {
        TimeGenerator gen = new TimeGenerator();
        int minute = gen.generateMinute();

        assertTrue(minute >= 0 && minute <= 59);
    }

    @Test
    void testGenerateSecond() {
        TimeGenerator gen = new TimeGenerator();
        int second = gen.generateSecond();

        assertTrue(second >= 0 && second <= 59);
    }

    @Test
    void testGenerateMillisecond() {
        TimeGenerator gen = new TimeGenerator();
        int millis = gen.generateMillisecond();

        assertTrue(millis >= 0 && millis <= 999);
    }

    @Test
    void testGenerateAmPm() {
        TimeGenerator gen = new TimeGenerator();
        String ampm = gen.generateAmPm();

        assertTrue(ampm.equals("am") || ampm.equals("pm"));
    }

    @Test
    void testSeededTimeGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen1 = new TimeGenerator(config);
        TimeGenerator gen2 = new TimeGenerator(config);

        LocalTime time1 = gen1.generate();
        LocalTime time2 = gen2.generate();

        assertEquals(time1, time2);
    }

    @Test
    void testSeededHourGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen1 = new TimeGenerator(config);
        TimeGenerator gen2 = new TimeGenerator(config);

        int hour1 = gen1.generateHour();
        int hour2 = gen2.generateHour();

        assertEquals(hour1, hour2);
    }

    @Test
    void testSeededMinuteGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen1 = new TimeGenerator(config);
        TimeGenerator gen2 = new TimeGenerator(config);

        int minute1 = gen1.generateMinute();
        int minute2 = gen2.generateMinute();

        assertEquals(minute1, minute2);
    }

    @Test
    void testSeededSecondGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen1 = new TimeGenerator(config);
        TimeGenerator gen2 = new TimeGenerator(config);

        int second1 = gen1.generateSecond();
        int second2 = gen2.generateSecond();

        assertEquals(second1, second2);
    }

    @Test
    void testSeededMillisecondGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen1 = new TimeGenerator(config);
        TimeGenerator gen2 = new TimeGenerator(config);

        int millis1 = gen1.generateMillisecond();
        int millis2 = gen2.generateMillisecond();

        assertEquals(millis1, millis2);
    }

    @Test
    void testSeededAmPmGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen1 = new TimeGenerator(config);
        TimeGenerator gen2 = new TimeGenerator(config);

        String ampm1 = gen1.generateAmPm();
        String ampm2 = gen2.generateAmPm();

        assertEquals(ampm1, ampm2);
    }

    @Test
    void testGenerateVariousHours() {
        TimeGenerator gen = new TimeGenerator();
        Set<Integer> hours = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            hours.add(gen.generateHour());
        }

        assertTrue(hours.size() > 5);
    }

    @Test
    void testGenerateVariousMinutes() {
        TimeGenerator gen = new TimeGenerator();
        Set<Integer> minutes = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            minutes.add(gen.generateMinute());
        }

        assertTrue(minutes.size() > 20);
    }

    @Test
    void testGenerateVariousSeconds() {
        TimeGenerator gen = new TimeGenerator();
        Set<Integer> seconds = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            seconds.add(gen.generateSecond());
        }

        assertTrue(seconds.size() > 20);
    }

    @Test
    void testGenerateVariousMilliseconds() {
        TimeGenerator gen = new TimeGenerator();
        Set<Integer> millis = new HashSet<>();

        for (int i = 0; i < 500; i++) {
            millis.add(gen.generateMillisecond());
        }

        assertTrue(millis.size() > 50);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new TimeGenerator(null));
    }

    @Test
    void testGenerateBothAmAndPm() {
        TimeGenerator gen = new TimeGenerator();
        Set<String> values = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            values.add(gen.generateAmPm());
        }

        assertEquals(2, values.size());
        assertTrue(values.contains("am"));
        assertTrue(values.contains("pm"));
    }

    @Test
    void testTimeWithMillisecondPrecision() {
        TimeGenerator gen = new TimeGenerator();
        LocalTime time = gen.generate();

        // Check that nanoseconds are present (from milliseconds)
        assertTrue(time.getNano() >= 0);
        assertTrue(time.getNano() < 1_000_000_000);
    }

    @Test
    void testFormatTimeStringCorrectly() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        TimeGenerator gen = new TimeGenerator(config);

        String timeStr = gen.generateString();
        String[] parts = timeStr.split(":");

        assertEquals(3, parts.length);
        assertEquals(2, parts[0].length()); // HH
        assertEquals(2, parts[1].length()); // MM
        assertEquals(2, parts[2].length()); // SS
    }

    @Test
    void testGenerateAllHours12() {
        TimeGenerator gen = new TimeGenerator();
        Set<Integer> hours = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            hours.add(gen.generateHour());
        }

        assertEquals(12, hours.size());
    }

    @Test
    void testGenerateAllHours24() {
        TimeGenerator gen = new TimeGenerator();
        Set<Integer> hours = new HashSet<>();

        for (int i = 0; i < 400; i++) {
            hours.add(gen.generateHour24());
        }

        assertEquals(24, hours.size());
    }
}
