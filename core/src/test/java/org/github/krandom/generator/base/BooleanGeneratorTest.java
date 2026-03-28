/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for {@link BooleanGenerator}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Basic generation (default 50% likelihood)
 *   <li>Seeded generation (deterministic)
 *   <li>Likelihood-based generation (0%, 25%, 50%, 75%, 100%)
 *   <li>Edge cases (boundary values)
 *   <li>Validation (invalid likelihood)
 *   <li>Statistical distribution (empirical probability)
 * </ul>
 */
@DisplayName("BooleanGenerator")
class BooleanGeneratorTest {

    @Nested
    @DisplayName("Basic Generation")
    class BasicGeneration {

        @Test
        @DisplayName("should generate boolean values")
        void shouldGenerateBooleans() {
            BooleanGenerator generator = new BooleanGenerator();
            Boolean result = generator.generate();
            assertNotNull(result);
        }

        @Test
        @DisplayName("should generate both true and false eventually")
        void shouldGenerateBothValues() {
            BooleanGenerator generator = new BooleanGenerator();
            Set<Boolean> values = new HashSet<>();

            // Generate 100 values - should see both true and false
            for (int i = 0; i < 100; i++) {
                values.add(generator.generate());
            }

            assertEquals(2, values.size(), "Should generate both true and false");
            assertTrue(values.contains(true));
            assertTrue(values.contains(false));
        }

        @Test
        @DisplayName("should generate list of booleans")
        void shouldGenerateList() {
            BooleanGenerator generator = new BooleanGenerator();
            List<Boolean> values = generator.generateList(50);

            assertEquals(50, values.size());
            assertTrue(values.stream().allMatch(v -> v != null));
        }
    }


    @Nested
    @DisplayName("Seeded Generation")
    class SeededGeneration {

        @Test
        @DisplayName("should be deterministic with same seed")
        void shouldBeDeterministicWithSameSeed() {
            long seed = 12345L;
            BooleanGenerator gen1 = new BooleanGenerator(seed);
            BooleanGenerator gen2 = new BooleanGenerator(seed);

            List<Boolean> values1 = gen1.generateList(100);
            List<Boolean> values2 = gen2.generateList(100);

            assertEquals(values1, values2, "Same seed should produce identical sequences");
        }

        @Test
        @DisplayName("should be different with different seeds")
        void shouldBeDifferentWithDifferentSeeds() {
            BooleanGenerator gen1 = new BooleanGenerator(11111L);
            BooleanGenerator gen2 = new BooleanGenerator(22222L);

            List<Boolean> values1 = gen1.generateList(100);
            List<Boolean> values2 = gen2.generateList(100);

            assertNotEquals(values1, values2, "Different seeds should produce different sequences");
        }

        @Test
        @DisplayName("should generate both values with seeded generator")
        void shouldGenerateBothValuesWithSeed() {
            BooleanGenerator generator = new BooleanGenerator(99999L);
            Set<Boolean> values = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                values.add(generator.generate());
            }

            assertEquals(2, values.size());
        }
    }


    @Nested
    @DisplayName("Likelihood-Based Generation")
    class LikelihoodBasedGeneration {

        @Test
        @DisplayName("withLikelihood(0) should always return false")
        void likelihood0ShouldAlwaysReturnFalse() {
            BooleanGenerator generator = new BooleanGenerator().withLikelihood(0);

            for (int i = 0; i < 1000; i++) {
                assertFalse(generator.generate(), "Likelihood 0 should always return false");
            }
        }

        @Test
        @DisplayName("withLikelihood(100) should always return true")
        void likelihood100ShouldAlwaysReturnTrue() {
            BooleanGenerator generator = new BooleanGenerator().withLikelihood(100);

            for (int i = 0; i < 1000; i++) {
                assertTrue(generator.generate(), "Likelihood 100 should always return true");
            }
        }

        @Test
        @DisplayName("withLikelihood(50) should generate both values")
        void likelihood50ShouldGenerateBothValues() {
            BooleanGenerator generator = new BooleanGenerator(42L).withLikelihood(50);
            Set<Boolean> values = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                values.add(generator.generate());
            }

            assertEquals(2, values.size(), "50% likelihood should produce both true and false");
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 10, 25, 50, 75, 90, 100 })
        @DisplayName("should accept valid likelihood values")
        void shouldAcceptValidLikelihood(int likelihood) {
            assertDoesNotThrow(() -> {
                BooleanGenerator generator = new BooleanGenerator().withLikelihood(likelihood);
                generator.generate();
            });
        }

        @Test
        @DisplayName("withLikelihood should return new instance")
        void withLikelihoodShouldReturnNewInstance() {
            BooleanGenerator original = new BooleanGenerator();
            BooleanGenerator modified = original.withLikelihood(80);

            assertNotSame(original, modified, "Should return new instance");
        }

        @Test
        @DisplayName("chaining withLikelihood should work")
        void chainingWithLikelihood() {
            BooleanGenerator generator = new BooleanGenerator()
                .withLikelihood(75)
                .withLikelihood(25);

            // Last likelihood should win
            assertNotNull(generator.generate());
        }
    }


    @Nested
    @DisplayName("Seeded Likelihood Generation")
    class SeededLikelihoodGeneration {

        @Test
        @DisplayName("seeded generator with likelihood should be deterministic")
        void seededWithLikelihoodShouldBeDeterministic() {
            long seed = 54321L;
            BooleanGenerator gen1 = new BooleanGenerator(seed).withLikelihood(75);
            BooleanGenerator gen2 = new BooleanGenerator(seed).withLikelihood(75);

            List<Boolean> values1 = gen1.generateList(100);
            List<Boolean> values2 = gen2.generateList(100);

            assertEquals(values1, values2, "Seeded likelihood generators should be deterministic");
        }

        @Test
        @DisplayName("likelihood 0 with seed should always be false")
        void likelihood0WithSeedShouldAlwaysBeFalse() {
            BooleanGenerator generator = new BooleanGenerator(777L).withLikelihood(0);

            List<Boolean> values = generator.generateList(100);
            assertTrue(values.stream().allMatch(v -> !v), "All should be false");
        }

        @Test
        @DisplayName("likelihood 100 with seed should always be true")
        void likelihood100WithSeedShouldAlwaysBeTrue() {
            BooleanGenerator generator = new BooleanGenerator(888L).withLikelihood(100);

            List<Boolean> values = generator.generateList(100);
            assertTrue(values.stream().allMatch(v -> v), "All should be true");
        }
    }


    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should reject negative likelihood")
        void shouldRejectNegativeLikelihood() {
            BooleanGenerator generator = new BooleanGenerator();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> generator.withLikelihood(-1));

            assertTrue(ex.getMessage().contains("must be between 0 and 100"));
            assertTrue(ex.getMessage().contains("-1"));
        }

        @Test
        @DisplayName("should reject likelihood > 100")
        void shouldRejectLikelihoodOver100() {
            BooleanGenerator generator = new BooleanGenerator();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> generator.withLikelihood(101));

            assertTrue(ex.getMessage().contains("must be between 0 and 100"));
            assertTrue(ex.getMessage().contains("101"));
        }

        @ParameterizedTest
        @ValueSource(ints = { -100, -50, -1, 101, 150, 200 })
        @DisplayName("should reject out-of-range likelihood values")
        void shouldRejectOutOfRangeLikelihood(int likelihood) {
            BooleanGenerator generator = new BooleanGenerator();

            assertThrows(IllegalArgumentException.class,
                         () -> generator.withLikelihood(likelihood));
        }
    }


    @Nested
    @DisplayName("Statistical Distribution")
    class StatisticalDistribution {

        private static final int    SAMPLE_SIZE = 10_000;
        private static final double TOLERANCE   = 0.05; // 5% tolerance

        @Test
        @DisplayName("default likelihood should be approximately 50%")
        void defaultLikelihoodShouldBe50Percent() {
            BooleanGenerator generator = new BooleanGenerator(42L);

            long trueCount = generator.stream()
                                      .limit(SAMPLE_SIZE)
                                      .filter(v -> v)
                                      .count();

            double actualPercentage = (double) trueCount / SAMPLE_SIZE;
            assertEquals(0.50, actualPercentage, TOLERANCE,
                         "Default likelihood should be ~50%");
        }

        @Test
        @DisplayName("likelihood 25 should produce ~25% true")
        void likelihood25ShouldProduce25PercentTrue() {
            BooleanGenerator generator = new BooleanGenerator(123L).withLikelihood(25);

            long trueCount = generator.stream()
                                      .limit(SAMPLE_SIZE)
                                      .filter(v -> v)
                                      .count();

            double actualPercentage = (double) trueCount / SAMPLE_SIZE;
            assertEquals(0.25, actualPercentage, TOLERANCE,
                         "Likelihood 25 should produce ~25% true");
        }

        @Test
        @DisplayName("likelihood 75 should produce ~75% true")
        void likelihood75ShouldProduce75PercentTrue() {
            BooleanGenerator generator = new BooleanGenerator(456L).withLikelihood(75);

            long trueCount = generator.stream()
                                      .limit(SAMPLE_SIZE)
                                      .filter(v -> v)
                                      .count();

            double actualPercentage = (double) trueCount / SAMPLE_SIZE;
            assertEquals(0.75, actualPercentage, TOLERANCE,
                         "Likelihood 75 should produce ~75% true");
        }

        @Test
        @DisplayName("likelihood 80 should produce ~80% true")
        void likelihood80ShouldProduce80PercentTrue() {
            BooleanGenerator generator = new BooleanGenerator(789L).withLikelihood(80);

            long trueCount = generator.stream()
                                      .limit(SAMPLE_SIZE)
                                      .filter(v -> v)
                                      .count();

            double actualPercentage = (double) trueCount / SAMPLE_SIZE;
            assertEquals(0.80, actualPercentage, TOLERANCE,
                         "Likelihood 80 should produce ~80% true");
        }

        @Test
        @DisplayName("likelihood 10 should produce ~10% true")
        void likelihood10ShouldProduce10PercentTrue() {
            BooleanGenerator generator = new BooleanGenerator(321L).withLikelihood(10);

            long trueCount = generator.stream()
                                      .limit(SAMPLE_SIZE)
                                      .filter(v -> v)
                                      .count();

            double actualPercentage = (double) trueCount / SAMPLE_SIZE;
            assertEquals(0.10, actualPercentage, TOLERANCE,
                         "Likelihood 10 should produce ~10% true");
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("likelihood 1 should produce very few true values")
        void likelihood1ShouldProduceVeryFewTrue() {
            BooleanGenerator generator = new BooleanGenerator(111L).withLikelihood(1);

            long trueCount = generator.stream()
                                      .limit(1000)
                                      .filter(v -> v)
                                      .count();

            assertTrue(trueCount < 50, "Likelihood 1% should produce < 50 true in 1000");
            assertTrue(trueCount > 0, "Likelihood 1% should produce at least some true values");
        }

        @Test
        @DisplayName("likelihood 99 should produce very few false values")
        void likelihood99ShouldProduceVeryFewFalse() {
            BooleanGenerator generator = new BooleanGenerator(222L).withLikelihood(99);

            long falseCount = generator.stream()
                                       .limit(1000)
                                       .filter(v -> !v)
                                       .count();

            assertTrue(falseCount < 50, "Likelihood 99% should produce < 50 false in 1000");
            assertTrue(falseCount > 0, "Likelihood 99% should produce at least some false values");
        }

        @Test
        @DisplayName("multiple calls should be independent")
        void multipleCallsShouldBeIndependent() {
            BooleanGenerator generator = new BooleanGenerator(555L).withLikelihood(75);

            Boolean first = generator.generate();
            Boolean second = generator.generate();

            // Just verify they're valid booleans (could be same or different)
            assertNotNull(first);
            assertNotNull(second);
        }

        @Test
        @DisplayName("empty list generation should work")
        void emptyListGenerationShouldWork() {
            BooleanGenerator generator = new BooleanGenerator().withLikelihood(50);
            List<Boolean> values = generator.generateList(0);

            assertEquals(0, values.size());
        }
    }


    @Nested
    @DisplayName("Integration")
    class Integration {

        @Test
        @DisplayName("should work with stream operations")
        void shouldWorkWithStreamOperations() {
            BooleanGenerator generator = new BooleanGenerator(999L).withLikelihood(60);

            long trueCount = generator.stream()
                                      .limit(1000)
                                      .filter(v -> v)
                                      .count();

            assertTrue(trueCount > 0, "Should have some true values");
            assertTrue(trueCount < 1000, "Should have some false values");
        }

        @Test
        @DisplayName("should work with multiple generators")
        void shouldWorkWithMultipleGenerators() {
            BooleanGenerator gen1 = new BooleanGenerator().withLikelihood(25);
            BooleanGenerator gen2 = new BooleanGenerator().withLikelihood(75);

            // Just verify both work independently
            assertNotNull(gen1.generate());
            assertNotNull(gen2.generate());
        }
    }
}
