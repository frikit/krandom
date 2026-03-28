/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NormalDistributionGenerator")
class NormalDistributionGeneratorTest {

    @Test
    @DisplayName("generate retries when first u1 is zero")
    void retriesWhenUniformZero() throws Exception {
        NormalDistributionGenerator generator = new NormalDistributionGenerator(0.0, 1.0);
        Field randomField = NormalDistributionGenerator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(generator, new RandomGenerator() {

            private int call;

            @Override
            public long nextLong() {
                return 0L;
            }

            @Override
            public double nextDouble() {
                // u1 (0.0) -> retry branch, then u1 (0.5), then u2 (0.25)
                return switch (call++) {
                    case 0 -> 0.0;
                    case 1 -> 0.5;
                    default -> 0.25;
                };
            }
        });

        assertNotNull(generator.generate());
    }


    @Nested
    @DisplayName("Standard Normal Distribution")
    class StandardNormalDistribution {

        @Test
        @DisplayName("should use mean=0 and stdDev=1 by default")
        void shouldUseDefaultParameters() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator();
            assertEquals(0.0, generator.getMean());
            assertEquals(1.0, generator.getStandardDeviation());
        }

        @Test
        @DisplayName("should generate values around mean=0")
        void shouldGenerateAroundZero() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator();
            List<Double> values = new ArrayList<>();

            for (int i = 0; i < 1000; i++) {
                values.add(generator.generate());
            }

            // Calculate sample mean
            double sampleMean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            // With 1000 samples, sample mean should be close to 0 (within ±0.1)
            assertTrue(Math.abs(sampleMean) < 0.1,
                       "Sample mean should be close to 0, got: " + sampleMean);
        }

        @Test
        @DisplayName("should follow 68-95-99.7 rule (empirical rule)")
        void shouldFollowEmpiricalRule() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator();
            int total = 10000;
            int within1Sigma = 0;
            int within2Sigma = 0;
            int within3Sigma = 0;

            for (int i = 0; i < total; i++) {
                double value = generator.generate();
                double absValue = Math.abs(value);

                if (absValue <= 1.0) within1Sigma++;
                if (absValue <= 2.0) within2Sigma++;
                if (absValue <= 3.0) within3Sigma++;
            }

            double pct1Sigma = (double) within1Sigma / total;
            double pct2Sigma = (double) within2Sigma / total;
            double pct3Sigma = (double) within3Sigma / total;

            // Allow ±5% tolerance
            assertTrue(pct1Sigma > 0.63 && pct1Sigma < 0.73,
                       "~68% should be within ±1σ, got: " + (pct1Sigma * 100) + "%");
            assertTrue(pct2Sigma > 0.90 && pct2Sigma < 1.00,
                       "~95% should be within ±2σ, got: " + (pct2Sigma * 100) + "%");
            assertTrue(pct3Sigma > 0.992 && pct3Sigma < 1.00,
                       "~99.7% should be within ±3σ, got: " + (pct3Sigma * 100) + "%");
        }
    }


    @Nested
    @DisplayName("Custom Mean and Standard Deviation")
    class CustomParameters {

        @Test
        @DisplayName("should use specified mean and standard deviation")
        void shouldUseSpecifiedParameters() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator(100.0, 15.0);
            assertEquals(100.0, generator.getMean());
            assertEquals(15.0, generator.getStandardDeviation());
        }

        @Test
        @DisplayName("should generate IQ-like distribution (mean=100, stdDev=15)")
        void shouldGenerateIQDistribution() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator(100.0, 15.0);
            List<Double> values = new ArrayList<>();

            for (int i = 0; i < 1000; i++) {
                values.add(generator.generate());
            }

            // Calculate sample mean and standard deviation
            double sampleMean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream()
                                    .mapToDouble(v -> Math.pow(v - sampleMean, 2))
                                    .average().orElse(0);
            double sampleStdDev = Math.sqrt(variance);

            // Sample statistics should be close to population parameters
            assertTrue(Math.abs(sampleMean - 100.0) < 2.0,
                       "Sample mean should be close to 100, got: " + sampleMean);
            assertTrue(Math.abs(sampleStdDev - 15.0) < 2.0,
                       "Sample std dev should be close to 15, got: " + sampleStdDev);
        }

        @Test
        @DisplayName("should generate heights distribution (mean=170, stdDev=10)")
        void shouldGenerateHeightsDistribution() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator(170.0, 10.0);
            int total = 1000;
            int count = 0;

            for (int i = 0; i < total; i++) {
                double height = generator.generate();
                // Count values within ±2σ (approximately 95%)
                if (height >= 150.0 && height <= 190.0) {
                    count++;
                }
            }

            double pct = (double) count / total;
            assertTrue(pct > 0.92 && pct < 0.98,
                       "~95% should be in [150, 190], got: " + (pct * 100) + "%");
        }

        @Test
        @DisplayName("should throw when standard deviation is zero")
        void shouldThrowWhenStdDevIsZero() {
            assertThrows(IllegalArgumentException.class,
                         () -> new NormalDistributionGenerator(0.0, 0.0));
        }

        @Test
        @DisplayName("should throw when standard deviation is negative")
        void shouldThrowWhenStdDevIsNegative() {
            assertThrows(IllegalArgumentException.class,
                         () -> new NormalDistributionGenerator(0.0, -1.0));
        }
    }


    @Nested
    @DisplayName("Seeded Generation")
    class SeededGeneration {

        @Test
        @DisplayName("should produce identical sequences with same seed")
        void shouldProduceIdenticalSequences() {
            NormalDistributionGenerator gen1 = new NormalDistributionGenerator(0.0, 1.0, 42L);
            NormalDistributionGenerator gen2 = new NormalDistributionGenerator(0.0, 1.0, 42L);

            for (int i = 0; i < 50; i++) {
                assertEquals(gen1.generate(), gen2.generate(),
                             "Generators with same seed should produce identical values");
            }
        }

        @Test
        @DisplayName("should produce different sequences with different seeds")
        void shouldProduceDifferentSequences() {
            NormalDistributionGenerator gen1 = new NormalDistributionGenerator(0.0, 1.0, 42L);
            NormalDistributionGenerator gen2 = new NormalDistributionGenerator(0.0, 1.0, 99L);

            List<Double> seq1 = new ArrayList<>();
            List<Double> seq2 = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                seq1.add(gen1.generate());
                seq2.add(gen2.generate());
            }

            assertNotEquals(seq1, seq2, "Different seeds should produce different sequences");
        }
    }


    @Nested
    @DisplayName("Negative Mean")
    class NegativeMean {

        @Test
        @DisplayName("should handle negative mean")
        void shouldHandleNegativeMean() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator(-10.0, 2.0);
            List<Double> values = new ArrayList<>();

            for (int i = 0; i < 1000; i++) {
                values.add(generator.generate());
            }

            double sampleMean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            assertTrue(Math.abs(sampleMean - (-10.0)) < 0.5,
                       "Sample mean should be close to -10, got: " + sampleMean);
        }
    }


    @Nested
    @DisplayName("Large Standard Deviation")
    class LargeStandardDeviation {

        @Test
        @DisplayName("should generate wider distribution with large stdDev")
        void shouldGenerateWideDistribution() {
            NormalDistributionGenerator narrow = new NormalDistributionGenerator(0.0, 1.0);
            NormalDistributionGenerator wide = new NormalDistributionGenerator(0.0, 10.0);

            int narrowOutliers = 0;
            int wideOutliers = 0;

            for (int i = 0; i < 1000; i++) {
                if (Math.abs(narrow.generate()) > 3.0) narrowOutliers++;
                if (Math.abs(wide.generate()) > 30.0) wideOutliers++;
            }

            // Both should have ~0.3% outliers beyond ±3σ, but in absolute terms:
            // narrow: beyond ±3, wide: beyond ±30
            assertTrue(narrowOutliers < 10, "Narrow should have few outliers beyond 3");
            assertTrue(wideOutliers < 10, "Wide should have few outliers beyond 30");
        }
    }


    @Nested
    @DisplayName("List Generation")
    class ListGeneration {

        @Test
        @DisplayName("should generate list of requested size")
        void shouldGenerateListOfRequestedSize() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator();
            List<Double> list = generator.generateList(100);
            assertEquals(100, list.size());
        }

        @Test
        @DisplayName("generated list should follow normal distribution")
        void generatedListShouldFollowNormalDistribution() {
            NormalDistributionGenerator generator = new NormalDistributionGenerator(50.0, 5.0);
            List<Double> list = generator.generateList(1000);

            double mean = list.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            assertTrue(Math.abs(mean - 50.0) < 1.0,
                       "List mean should be close to 50, got: " + mean);
        }
    }
}
