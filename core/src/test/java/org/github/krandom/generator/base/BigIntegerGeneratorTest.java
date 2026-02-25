/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.github.krandom.generator.Generators;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BigIntegerGenerator")
class BigIntegerGeneratorTest {

    private static final int SAMPLES = 50;

    @Nested
    @DisplayName("Default construction")
    class DefaultTest {

        @Test
        @DisplayName("generate() returns non-null")
        void notNull() {
            assertNotNull(new BigIntegerGenerator().generate());
        }

        @Test
        @DisplayName("generated value is in [0, Long.MAX_VALUE]")
        void inDefaultRange() {
            BigInteger lo = BigInteger.ZERO;
            BigInteger hi = BigInteger.valueOf(Long.MAX_VALUE);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger v = new BigIntegerGenerator().generate();
                assertTrue(v.compareTo(lo) >= 0 && v.compareTo(hi) <= 0,
                        "Expected [0, Long.MAX_VALUE], got: " + v);
            }
        }
    }

    @Nested
    @DisplayName("Custom range")
    class CustomRangeTest {

        @Test
        @DisplayName("generated value is within [min, max]")
        void customRange() {
            BigInteger lo = BigInteger.valueOf(1_000_000L);
            BigInteger hi = BigInteger.valueOf(9_999_999L);
            BigIntegerGenerator gen = new BigIntegerGenerator(lo, hi);
            for (int i = 0; i < SAMPLES; i++) {
                BigInteger v = gen.generate();
                assertTrue(v.compareTo(lo) >= 0 && v.compareTo(hi) <= 0,
                        "Expected [" + lo + ", " + hi + "], got: " + v);
            }
        }

        @Test
        @DisplayName("seeded generator is reproducible")
        void seededReproducibility() {
            BigInteger lo = BigInteger.ZERO;
            BigInteger hi = BigInteger.valueOf(1_000_000L);
            BigIntegerGenerator a = new BigIntegerGenerator(lo, hi, 42L);
            BigIntegerGenerator b = new BigIntegerGenerator(lo, hi, 42L);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }
    }

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofBigInteger() returns non-null value")
        void ofBigIntegerDefault() {
            assertNotNull(Generators.ofBigInteger().generate());
        }

        @Test
        @DisplayName("Generators.ofBigInteger(min, max) stays in range")
        void ofBigIntegerRange() {
            BigInteger lo = BigInteger.valueOf(100L);
            BigInteger hi = BigInteger.valueOf(999L);
            BigInteger v = Generators.ofBigInteger(lo, hi).generate();
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
            BigInteger v = BigInteger.TEN;
            assertThrows(IllegalArgumentException.class,
                    () -> new BigIntegerGenerator(v, v));
        }
    }
}
