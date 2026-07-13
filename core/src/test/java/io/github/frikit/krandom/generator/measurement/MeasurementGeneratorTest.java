/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.measurement;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MeasurementGenerator")
class MeasurementGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> EN = Set.of(
        "Meter", "Kilometer", "Centimeter", "Millimeter", "Gram", "Kilogram",
        "Liter", "Milliliter", "Second", "Minute", "Hour", "Celsius");

    private static final Set<String> RU_UNITS = Set.of(
        "Метр", "Километр", "Сантиметр", "Миллиметр", "Грамм", "Килограмм",
        "Литр", "Миллилитр", "Секунда", "Минута", "Час", "Цельсий");

    @RepeatedTest(200)
    @DisplayName("default generate() returns a valid English unit")
    void generateValid() {
        assertTrue(EN.contains(new MeasurementGenerator().generate()));
    }

    @Test
    @DisplayName("default generate() can surface every unit over many draws")
    void generateCoversAll() {
        MeasurementGenerator gen = new MeasurementGenerator(GeneratorConfig.builder().seed(5L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(EN, seen);
    }

    @Test
    @DisplayName("Russian locale generates Russian unit names")
    void russian() {
        MeasurementGenerator gen = new MeasurementGenerator(RU);
        for (int i = 0; i < 200; i++) {
            assertTrue(RU_UNITS.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("unmapped locale falls back to English units")
    void unmappedFallsBackToEnglish() {
        assertTrue(EN.contains(new MeasurementGenerator(Locale.of("is", "IS")).generate()));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new MeasurementGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        List<String> b = new MeasurementGenerator(GeneratorConfig.builder().seed(8L).build()).generateList(30);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new MeasurementGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new MeasurementGenerator((Locale) null));
    }

    @Nested
    @DisplayName("MeasurementDataRegistry")
    class Registry {
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofMeasurement (default / locale / config) produce valid units")
        void facade() {
            assertTrue(EN.contains(Generators.ofMeasurement().generate()));
            assertTrue(RU_UNITS.contains(Generators.ofMeasurement(RU).generate()));
            assertTrue(EN.contains(
                Generators.ofMeasurement(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
