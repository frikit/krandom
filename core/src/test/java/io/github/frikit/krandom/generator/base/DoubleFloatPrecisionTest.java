/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Double and float precision generators")
class DoubleFloatPrecisionTest {

    @Test
    @DisplayName("identically seeded double precision generators replay the same values")
    void seededDoublePrecisionGeneratorsReplay() {
        List<Double> first = new DoubleGenerator(0, 10, 42L).withPrecision(2).generateList(200);
        List<Double> second = new DoubleGenerator(0, 10, 42L).withPrecision(2).generateList(200);

        assertEquals(first, second);
        for (double value : first) {
            assertTrue(value >= 0 && value < 10, Double.toString(value));
            assertTrue(BigDecimal.valueOf(value).scale() <= 2, Double.toString(value));
        }
    }

    @Test
    @DisplayName("a reseeded double generator passes its latest seed to the precision generator")
    void reseededDoubleGeneratorPassesLatestSeed() {
        DoubleGenerator reseeded = new DoubleGenerator(0, 10, 1L);
        reseeded.reseed(9L);

        assertEquals(new DoubleGenerator(0, 10, 9L).withPrecision(3).generateList(50),
                     reseeded.withPrecision(3).generateList(50));
        DoubleGenerator unseeded = new DoubleGenerator(0, 10);
        unseeded.reseed(9L);
        assertEquals(new DoubleGenerator(0, 10, 9L).withPrecision(3).generateList(50),
                     unseeded.withPrecision(3).generateList(50));
    }

    @Test
    @DisplayName("double rounding never reaches the exclusive max or falls below min")
    void doubleRoundingStaysInsideTheRange() {
        Set<Double> zeroDecimals = new TreeSet<>(new DoubleGenerator(0, 1, 5L).withPrecision(0).generateList(1_000));
        Set<Double> oneDecimal = new TreeSet<>(new DoubleGenerator(0.14, 0.3, 5L).withPrecision(1).generateList(1_000));
        Set<Double> explicit = new TreeSet<>(new DoubleGenerator(0, 100, 5L).withPrecision(1).generateList(0));
        DoubleGenerator explicitBounds = new DoubleGenerator(0, 100, 5L).withPrecision(1);
        for (int i = 0; i < 1_000; i++) {
            explicit.add(explicitBounds.generate(0.14, 0.3));
        }

        assertEquals(Set.of(0.0), zeroDecimals);
        assertEquals(Set.of(0.2), oneDecimal);
        assertEquals(Set.of(0.2), explicit);
    }

    @Test
    @DisplayName("double ranges without a value at the requested precision are rejected")
    void doubleRangesWithoutRepresentableValuesAreRejected() {
        DoubleGenerator none = new DoubleGenerator(0.5, 0.9, 3L).withPrecision(0);
        DoubleGenerator explicit = new DoubleGenerator(0, 10, 3L).withPrecision(1);

        assertThrows(IllegalArgumentException.class, none::generate);
        assertThrows(IllegalArgumentException.class, () -> explicit.generate(0.41, 0.49));
        assertEquals(0.1, new DoubleGenerator(0.1, 0.15, 3L).withPrecision(1).generate());
    }

    @Test
    @DisplayName("identically seeded float precision generators replay the same values")
    void seededFloatPrecisionGeneratorsReplay() {
        List<Float> first = new FloatGenerator(0f, 10f, 42L).withPrecision(2).generateList(200);
        List<Float> second = new FloatGenerator(0f, 10f, 42L).withPrecision(2).generateList(200);

        assertEquals(first, second);
        for (float value : first) {
            assertTrue(value >= 0f && value < 10f, Float.toString(value));
            assertTrue(new BigDecimal(Float.toString(value)).scale() <= 2, Float.toString(value));
        }
    }

    @Test
    @DisplayName("a reseeded float generator passes its latest seed to the precision generator")
    void reseededFloatGeneratorPassesLatestSeed() {
        FloatGenerator reseeded = new FloatGenerator(0f, 10f, 1L);
        reseeded.reseed(9L);

        assertEquals(new FloatGenerator(0f, 10f, 9L).withPrecision(3).generateList(50),
                     reseeded.withPrecision(3).generateList(50));
    }

    @Test
    @DisplayName("float rounding never reaches the exclusive max or falls below min")
    void floatRoundingStaysInsideTheRange() {
        Set<Float> zeroDecimals = new TreeSet<>(new FloatGenerator(0f, 1f, 5L).withPrecision(0).generateList(1_000));
        Set<Float> oneDecimal = new TreeSet<>(new FloatGenerator(0.14f, 0.3f, 5L).withPrecision(1).generateList(1_000));

        assertEquals(Set.of(0.0f), zeroDecimals);
        assertEquals(Set.of(0.2f), oneDecimal);
        assertEquals(0.1f, new FloatGenerator(0.1f, 0.15f, 3L).withPrecision(1).generate());
    }

    @Test
    @DisplayName("a seeded configuration produces the values of the equivalent raw seed")
    void seededConfigurationMatchesRawSeed() {
        GeneratorConfig seeded = GeneratorConfig.builder().seed(42L).build();

        assertEquals(new DoubleGenerator(0, 10, 42L).generateList(20), new DoubleGenerator(0, 10, seeded).generateList(20));
        assertEquals(new DoubleGenerator(0, 10, 42L).withPrecision(2).generateList(20),
                     Generators.ofDouble(0, 10, seeded).withPrecision(2).generateList(20));
        assertEquals(new DoubleGenerator(0, 1, 42L).generateList(5), Generators.ofDouble(seeded).generateList(5));
        assertEquals(new FloatGenerator(0f, 10f, 42L).generateList(20), new FloatGenerator(0f, 10f, seeded).generateList(20));
        assertEquals(new FloatGenerator(0f, 10f, 42L).withPrecision(2).generateList(20),
                     Generators.ofFloat(0f, 10f, seeded).withPrecision(2).generateList(20));
        assertEquals(new FloatGenerator(0f, 1f, 42L).generateList(5), Generators.ofFloat(seeded).generateList(5));
    }

    @Test
    @DisplayName("a precision generator keeps an unseeded configuration's caller-owned source")
    void precisionKeepsCallerOwnedSource() {
        assertEquals(new DoubleGenerator(0, 10, callerOwned(7L)).withPrecision(2).generateList(20),
                     new DoubleGenerator(0, 10, callerOwned(7L)).withPrecision(2).generateList(20));
        assertEquals(new FloatGenerator(0f, 10f, callerOwned(7L)).withPrecision(2).generateList(20),
                     new FloatGenerator(0f, 10f, callerOwned(7L)).withPrecision(2).generateList(20));

        Random caller = new Random(7L);
        new DoubleGenerator(GeneratorConfig.builder().random(caller).build()).withPrecision(1).generate();
        assertNotEquals(new Random(7L).nextLong(), caller.nextLong(), "the caller-owned random supplies the draws");
        Random floatCaller = new Random(7L);
        new FloatGenerator(GeneratorConfig.builder().random(floatCaller).build()).withPrecision(1).generate();
        assertNotEquals(new Random(7L).nextLong(), floatCaller.nextLong(), "the caller-owned random supplies the draws");
    }

    private static GeneratorConfig callerOwned(long seed) {
        return GeneratorConfig.builder().random(new Random(seed)).build();
    }

    @Test
    @DisplayName("float ranges without a value at the requested precision are rejected")
    void floatRangesWithoutRepresentableValuesAreRejected() {
        FloatGenerator none = new FloatGenerator(0.5f, 0.9f, 3L).withPrecision(0);
        FloatGenerator explicit = new FloatGenerator(0f, 10f, 3L).withPrecision(1);

        assertThrows(IllegalArgumentException.class, none::generate);
        assertThrows(IllegalArgumentException.class, () -> explicit.generate(0.41f, 0.49f));
    }
}
