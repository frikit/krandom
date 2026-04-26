/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NaturalNumberGenerator")
class NaturalNumberGeneratorTest {

    @Nested
    @DisplayName("Default Constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("should generate non-negative integers")
        void shouldGenerateNonNegativeIntegers() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator();
            for (int i = 0; i < 200; i++) {
                int value = generator.generate();
                assertTrue(value >= 0, "Natural number must be >= 0, got: " + value);
            }
        }

        @Test
        @DisplayName("should use default range [0, Integer.MAX_VALUE)")
        void shouldUseDefaultRange() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator();
            assertEquals(0, generator.getMin());
            assertEquals(Integer.MAX_VALUE, generator.getMax());
        }
    }


    @Nested
    @DisplayName("Custom Range")
    class CustomRange {

        @Test
        @DisplayName("should generate within custom range")
        void shouldGenerateWithinCustomRange() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(10, 20);
            for (int i = 0; i < 200; i++) {
                int value = generator.generate();
                assertTrue(value >= 10, "Value must be >= 10, got: " + value);
                assertTrue(value < 20, "Value must be < 20, got: " + value);
            }
        }

        @Test
        @DisplayName("should accept min=0")
        void shouldAcceptMinZero() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 100);
            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(value >= 0 && value < 100);
            }
        }

        @Test
        @DisplayName("should throw when min == max")
        void shouldThrowWhenMinEqualsMax() {
            assertThrows(IllegalArgumentException.class,
                         () -> new NaturalNumberGenerator(5, 5).generate());
        }
    }


    @Nested
    @DisplayName("Seeded Generation")
    class SeededGeneration {

        @Test
        @DisplayName("should produce identical sequences with same seed")
        void shouldProduceIdenticalSequences() {
            NaturalNumberGenerator gen1 = new NaturalNumberGenerator(0, 100, 42L);
            NaturalNumberGenerator gen2 = new NaturalNumberGenerator(0, 100, 42L);

            for (int i = 0; i < 50; i++) {
                assertEquals(gen1.generate(), gen2.generate(),
                             "Generators with same seed should produce identical values");
            }
        }

        @Test
        @DisplayName("should produce different sequences with different seeds")
        void shouldProduceDifferentSequences() {
            NaturalNumberGenerator gen1 = new NaturalNumberGenerator(0, 1000, 42L);
            NaturalNumberGenerator gen2 = new NaturalNumberGenerator(0, 1000, 99L);

            List<Integer> seq1 = gen1.generateList(20);
            List<Integer> seq2 = gen2.generateList(20);

            assertNotEquals(seq1, seq2, "Different seeds should produce different sequences");
        }
    }


    @Nested
    @DisplayName("Exclusion Support")
    class ExclusionSupport {

        @Test
        @DisplayName("should exclude single value")
        void shouldExcludeSingleValue() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 10).excluding(5);
            for (int i = 0; i < 100; i++) {
                int value = generator.generate();
                assertNotEquals(5, value, "Value 5 should be excluded");
                assertTrue(value >= 0 && value < 10, "Value should be in range [0, 10)");
            }
        }

        @Test
        @DisplayName("should exclude multiple values")
        void shouldExcludeMultipleValues() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 20).excluding(5, 10, 15);
            Set<Integer> excluded = Set.of(5, 10, 15);

            for (int i = 0; i < 200; i++) {
                int value = generator.generate();
                assertFalse(excluded.contains(value),
                            "Value " + value + " should be excluded");
                assertTrue(value >= 0 && value < 20, "Value should be in range [0, 20)");
            }
        }

        @Test
        @DisplayName("should handle empty exclusion list")
        void shouldHandleEmptyExclusionList() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 10).excluding();
            // Should work normally without exclusions
            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(value >= 0 && value < 10);
            }
        }

        @Test
        @DisplayName("should generate all non-excluded values eventually")
        void shouldGenerateAllNonExcludedValues() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 10).excluding(5);
            Set<Integer> generated = new HashSet<>();
            Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 6, 7, 8, 9);

            for (int i = 0; i < 1000 && generated.size() < expected.size(); i++) {
                generated.add(generator.generate());
            }

            assertTrue(generated.containsAll(expected),
                       "Should eventually generate all non-excluded values");
        }

        @Test
        @DisplayName("should throw when all values are excluded")
        void shouldThrowWhenAllValuesExcluded() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 3)
                .excluding(0, 1, 2);

            assertThrows(IllegalStateException.class, generator::generate,
                         "Should throw when all possible values are excluded");
        }

        @Test
        @DisplayName("should throw after max retry attempts when RNG always selects excluded value")
        void shouldThrowAfterMaxRetries() throws Exception {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 2).excluding(0);
            Field randomField = generator.getClass().getSuperclass().getDeclaredField("random");
            randomField.setAccessible(true);
            randomField.set(generator, new RandomGenerator() {

                @Override
                public long nextLong() {
                    return 0L;
                }

                @Override
                public int nextInt(int origin, int bound) {
                    return origin; // always excluded in this test
                }
            });

            assertThrows(IllegalStateException.class, generator::generate);
        }

        @Test
        @DisplayName("should handle exclusion of values outside range")
        void shouldHandleExclusionOutsideRange() {
            // Excluding 100 and 200 which are outside [0, 10) range
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 10).excluding(100, 200);

            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(value >= 0 && value < 10);
            }
        }

        @Test
        @DisplayName("should handle sparse exclusions efficiently")
        void shouldHandleSparseExclusionsEfficiently() {
            // Test with many exclusions but still some valid values
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 100)
                .excluding(1, 3, 5, 7, 9, 11, 13, 15, 17, 19);

            for (int i = 0; i < 100; i++) {
                int value = generator.generate();
                assertFalse(Set.of(1, 3, 5, 7, 9, 11, 13, 15, 17, 19).contains(value));
                assertTrue(value >= 0 && value < 100);
            }
        }

        @Test
        @DisplayName("should work with dense exclusions")
        void shouldWorkWithDenseExclusions() {
            // Exclude 95% of range, leaving only a few valid values
            Set<Integer> excluded = new HashSet<>();
            for (int i = 5; i < 100; i++) {
                excluded.add(i);
            }
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 100)
                .excluding(excluded.stream().mapToInt(Integer::intValue).toArray());

            // Should only generate 0, 1, 2, 3, 4
            Set<Integer> expected = Set.of(0, 1, 2, 3, 4);
            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(expected.contains(value),
                           "Value should be in {0,1,2,3,4}, got: " + value);
            }
        }

        @Test
        @DisplayName("should work with negative excluded values")
        void shouldWorkWithNegativeExcludedValues() {
            // Negative values outside range should be ignored
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 10).excluding(-1, -5);

            for (int i = 0; i < 50; i++) {
                int value = generator.generate();
                assertTrue(value >= 0 && value < 10);
            }
        }
    }


    @Nested
    @DisplayName("Generate with Custom Bounds")
    class GenerateWithCustomBounds {

        @Test
        @DisplayName("should generate within provided bounds")
        void shouldGenerateWithinProvidedBounds() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator();
            for (int i = 0; i < 100; i++) {
                int value = generator.generate(50, 60);
                assertTrue(value >= 50 && value < 60);
            }
        }

        @Test
        @DisplayName("should apply exclusions to custom bounds")
        void shouldApplyExclusionsToCustomBounds() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator().excluding(55);
            for (int i = 0; i < 100; i++) {
                int value = generator.generate(50, 60);
                assertNotEquals(55, value, "Value 55 should be excluded");
                assertTrue(value >= 50 && value < 60);
            }
        }
    }


    @Nested
    @DisplayName("List Generation")
    class ListGeneration {

        @Test
        @DisplayName("should generate list of requested size")
        void shouldGenerateListOfRequestedSize() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 100);
            List<Integer> list = generator.generateList(50);
            assertEquals(50, list.size());
        }

        @Test
        @DisplayName("should apply exclusions in list generation")
        void shouldApplyExclusionsInListGeneration() {
            NaturalNumberGenerator generator = new NaturalNumberGenerator(0, 20).excluding(10);
            List<Integer> list = generator.generateList(100);

            assertFalse(list.contains(10), "List should not contain excluded value 10");
        }
    }
}
