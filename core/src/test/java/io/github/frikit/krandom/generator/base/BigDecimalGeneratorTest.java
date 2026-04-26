/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BigDecimalGenerator")
class BigDecimalGeneratorTest {

    private static final int SAMPLES = 50;


    @Nested
    @DisplayName("Default construction")
    class DefaultTest {

        @Test
        @DisplayName("generate() returns non-null")
        void notNull() {
            assertNotNull(new BigDecimalGenerator().generate());
        }

        @Test
        @DisplayName("generated value has scale 2")
        void scaleIsTwo() {
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(2, new BigDecimalGenerator().generate().scale());
            }
        }

        @Test
        @DisplayName("generated value is in [0, 1_000_000]")
        void inDefaultRange() {
            BigDecimal lo = BigDecimal.ZERO;
            BigDecimal hi = new BigDecimal("1000000");
            for (int i = 0; i < SAMPLES; i++) {
                BigDecimal v = new BigDecimalGenerator().generate();
                assertTrue(v.compareTo(lo) >= 0 && v.compareTo(hi) <= 0,
                           "Expected [0, 1000000], got: " + v);
            }
        }
    }


    @Nested
    @DisplayName("Custom range")
    class CustomRangeTest {

        @Test
        @DisplayName("generated value is in [0.01, 9.99]")
        void customRange() {
            BigDecimal lo = new BigDecimal("0.01");
            BigDecimal hi = new BigDecimal("9.99");
            BigDecimalGenerator gen = new BigDecimalGenerator(lo, hi);
            for (int i = 0; i < SAMPLES; i++) {
                BigDecimal v = gen.generate();
                assertTrue(v.compareTo(lo) >= 0 && v.compareTo(hi) <= 0,
                           "Expected [0.01, 9.99], got: " + v);
            }
        }

        @Test
        @DisplayName("3-arg constructor (min, max, scale) uses custom scale")
        void threeArgConstructorCustomScale() {
            BigDecimal lo = BigDecimal.ZERO;
            BigDecimal hi = new BigDecimal("100");
            BigDecimalGenerator gen = new BigDecimalGenerator(lo, hi, 4);
            for (int i = 0; i < SAMPLES; i++) {
                BigDecimal v = gen.generate();
                assertEquals(4, v.scale(), "Expected scale 4");
                assertTrue(v.compareTo(lo) >= 0 && v.compareTo(hi) <= 0,
                           "Expected [0, 100], got: " + v);
            }
        }

        @Test
        @DisplayName("seeded generator is reproducible")
        void seededReproducibility() {
            BigDecimal lo = BigDecimal.ZERO;
            BigDecimal hi = new BigDecimal("100");
            BigDecimalGenerator a = new BigDecimalGenerator(lo, hi, 2, 42L);
            BigDecimalGenerator b = new BigDecimalGenerator(lo, hi, 2, 42L);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }
    }


    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofBigDecimal() returns non-null value")
        void ofBigDecimalDefault() {
            assertNotNull(Generators.ofBigDecimal().generate());
        }

        @Test
        @DisplayName("Generators.ofBigDecimal(min, max) stays in range")
        void ofBigDecimalRange() {
            BigDecimal lo = new BigDecimal("1.00");
            BigDecimal hi = new BigDecimal("5.00");
            BigDecimal v = Generators.ofBigDecimal(lo, hi).generate();
            assertNotNull(v);
            assertTrue(v.compareTo(lo) >= 0 && v.compareTo(hi) <= 0);
        }
    }


    @Nested
    @DisplayName("Validation")
    class ValidationTest {

        @Test
        @DisplayName("min >= max throws IllegalArgumentException")
        void minGteMaxThrows() {
            BigDecimal v = new BigDecimal("5");
            assertThrows(IllegalArgumentException.class,
                         () -> new BigDecimalGenerator(v, v));
        }

        @Test
        @DisplayName("negative scale throws IllegalArgumentException")
        void negativeScaleThrows() {
            assertThrows(IllegalArgumentException.class,
                         () -> new BigDecimalGenerator(BigDecimal.ZERO, BigDecimal.TEN, -1));
        }
    }
}
