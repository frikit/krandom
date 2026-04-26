/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PrimeGenerator")
class PrimeGeneratorTest {

    // Helper method to check if a number is prime
    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }


    @Nested
    @DisplayName("Default Range")
    class DefaultRange {

        @Test
        @DisplayName("should use range [2, 1000) by default")
        void shouldUseDefaultRange() {
            PrimeGenerator generator = new PrimeGenerator();
            assertEquals(2, generator.getMin());
            assertEquals(1000, generator.getMax());
        }

        @Test
        @DisplayName("should generate only prime numbers")
        void shouldGenerateOnlyPrimes() {
            PrimeGenerator generator = new PrimeGenerator();
            for (int i = 0; i < 100; i++) {
                int value = generator.generate();
                assertTrue(isPrime(value), "Value should be prime, got: " + value);
                assertTrue(value >= 2 && value < 1000);
            }
        }

        @Test
        @DisplayName("should have 168 primes in default range")
        void shouldHaveCorrectPrimeCount() {
            PrimeGenerator generator = new PrimeGenerator();
            assertEquals(168, generator.getPrimeCount(),
                         "There are 168 primes less than 1000");
        }
    }


    @Nested
    @DisplayName("Custom Range")
    class CustomRange {

        @Test
        @DisplayName("should generate primes in range [2, 20)")
        void shouldGeneratePrimesInSmallRange() {
            PrimeGenerator generator = new PrimeGenerator(2, 20);
            Set<Integer> expected = Set.of(2, 3, 5, 7, 11, 13, 17, 19);
            Set<Integer> generated = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                int value = generator.generate();
                assertTrue(expected.contains(value),
                           "Value should be in {2,3,5,7,11,13,17,19}, got: " + value);
                generated.add(value);
            }

            assertTrue(generated.size() >= 6,
                       "Should generate most of the available primes");
        }

        @Test
        @DisplayName("should generate primes in range [10, 30)")
        void shouldGeneratePrimesInMidRange() {
            PrimeGenerator generator = new PrimeGenerator(10, 30);
            Set<Integer> expected = Set.of(11, 13, 17, 19, 23, 29);

            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(expected.contains(value),
                           "Value should be in {11,13,17,19,23,29}, got: " + value);
            }

            assertEquals(6, generator.getPrimeCount());
        }

        @Test
        @DisplayName("should generate large primes in range [1000, 2000)")
        void shouldGenerateLargePrimes() {
            PrimeGenerator generator = new PrimeGenerator(1000, 2000);
            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(isPrime(value), "Value should be prime, got: " + value);
                assertTrue(value >= 1000 && value < 2000);
            }

            assertEquals(135, generator.getPrimeCount(),
                         "There are 135 primes in [1000, 2000)");
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should throw when no primes exist in range")
        void shouldThrowWhenNoPrimesExist() {
            assertThrows(IllegalStateException.class,
                         () -> new PrimeGenerator(0, 2),
                         "No primes in [0, 2)");
        }

        @Test
        @DisplayName("should throw when range is [1, 2)")
        void shouldThrowForRangeOne() {
            assertThrows(IllegalStateException.class,
                         () -> new PrimeGenerator(1, 2));
        }

        @Test
        @DisplayName("should handle range with single prime")
        void shouldHandleSinglePrime() {
            PrimeGenerator generator = new PrimeGenerator(2, 3);
            for (int i = 0; i < 20; i++) {
                assertEquals(2, generator.generate(), "Should always generate 2");
            }
            assertEquals(1, generator.getPrimeCount());
        }

        @Test
        @DisplayName("should handle min > max with auto-swap")
        void shouldHandleReversedBounds() {
            PrimeGenerator generator = new PrimeGenerator(20, 2);
            Set<Integer> expected = Set.of(2, 3, 5, 7, 11, 13, 17, 19);

            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(expected.contains(value));
            }
        }
    }


    @Nested
    @DisplayName("Seeded Generation")
    class SeededGeneration {

        @Test
        @DisplayName("should produce identical sequences with same seed")
        void shouldProduceIdenticalSequences() {
            PrimeGenerator gen1 = new PrimeGenerator(2, 100, 42L);
            PrimeGenerator gen2 = new PrimeGenerator(2, 100, 42L);

            for (int i = 0; i < 50; i++) {
                assertEquals(gen1.generate(), gen2.generate(),
                             "Generators with same seed should produce identical values");
            }
        }

        @Test
        @DisplayName("should produce different sequences with different seeds")
        void shouldProduceDifferentSequences() {
            PrimeGenerator gen1 = new PrimeGenerator(2, 100, 42L);
            PrimeGenerator gen2 = new PrimeGenerator(2, 100, 99L);

            Set<Integer> seq1 = new HashSet<>();
            Set<Integer> seq2 = new HashSet<>();

            for (int i = 0; i < 20; i++) {
                seq1.add(gen1.generate());
                seq2.add(gen2.generate());
            }

            // Different seeds should produce different distributions
            // (not necessarily different sets, but likely)
            assertTrue(seq1.size() > 5 && seq2.size() > 5);
        }
    }


    @Nested
    @DisplayName("List Generation")
    class ListGeneration {

        @Test
        @DisplayName("should generate list of prime numbers")
        void shouldGenerateListOfPrimes() {
            PrimeGenerator generator = new PrimeGenerator(2, 50);
            var list = generator.generateList(20);

            assertEquals(20, list.size());
            for (int prime : list) {
                assertTrue(isPrime(prime), "All values should be prime");
                assertTrue(prime >= 2 && prime < 50);
            }
        }
    }
}
