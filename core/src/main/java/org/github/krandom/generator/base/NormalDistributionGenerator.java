/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random {@link Double} values following a normal (Gaussian) distribution.
 *
 * <p>Uses the <a href="https://en.wikipedia.org/wiki/Box-Muller_transform">Box-Muller transform</a>
 * to convert uniformly distributed random values into normally distributed values.
 *
 * <p>Default parameters: mean = 0.0, standard deviation = 1.0 (standard normal distribution).
 *
 * <pre>{@code
 *   // Standard normal distribution (mean=0, stdDev=1)
 *   double z = new NormalDistributionGenerator().generate();
 *
 *   // IQ scores: mean=100, stdDev=15
 *   double iq = new NormalDistributionGenerator(100.0, 15.0).generate();
 *
 *   // Heights in cm: mean=170, stdDev=10
 *   double height = new NormalDistributionGenerator(170.0, 10.0).generate();
 * }</pre>
 *
 * <p><strong>Statistical properties:</strong>
 * <ul>
 *   <li>Approximately 68% of values fall within ±1 standard deviation of the mean</li>
 *   <li>Approximately 95% of values fall within ±2 standard deviations of the mean</li>
 *   <li>Approximately 99.7% of values fall within ±3 standard deviations of the mean</li>
 *   <li>Values can theoretically be any real number (no hard bounds)</li>
 * </ul>
 */
public final class NormalDistributionGenerator implements org.github.krandom.generator.Generator<Double> {

    private final RandomGenerator random;
    private final double mean;
    private final double standardDeviation;
    
    // Box-Muller generates two values at once; cache the second for efficiency
    private Double cachedValue = null;

    /**
     * Create a standard normal distribution generator (mean=0, stdDev=1).
     */
    public NormalDistributionGenerator() {
        this(0.0, 1.0, null);
    }

    /**
     * Create a normal distribution generator with specified mean and standard deviation.
     *
     * @param mean the mean (μ) of the distribution
     * @param standardDeviation the standard deviation (σ) of the distribution
     * @throws IllegalArgumentException if standardDeviation is negative or zero
     */
    public NormalDistributionGenerator(double mean, double standardDeviation) {
        this(mean, standardDeviation, null);
    }

    /**
     * Create a seeded normal distribution generator.
     *
     * @param mean the mean (μ) of the distribution
     * @param standardDeviation the standard deviation (σ) of the distribution
     * @param seed optional seed for reproducibility; {@code null} means {@link SecureRandom}
     * @throws IllegalArgumentException if standardDeviation is negative or zero
     */
    public NormalDistributionGenerator(double mean, double standardDeviation, Long seed) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException(
                    "Standard deviation must be positive, got: " + standardDeviation);
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.random = seed != null ? new Random(seed) : new SecureRandom();
    }

    /**
     * Generate a random value from this normal distribution.
     *
     * <p>Uses the Box-Muller transform to generate normally distributed values.
     * The transform generates two independent normal random values per call; this method
     * caches the second value for the next call for efficiency.
     *
     * @return a normally distributed random value
     */
    @Override
    public Double generate() {
        // If we have a cached value from previous Box-Muller transform, use it
        if (cachedValue != null) {
            double result = cachedValue;
            cachedValue = null;
            return result;
        }

        // Box-Muller transform
        // Generate two independent uniform random values in (0, 1)
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();

        // Avoid log(0) by ensuring u1 > 0
        while (u1 == 0.0) {
            u1 = random.nextDouble();
        }

        // Box-Muller transform generates two independent standard normal values
        double magnitude = Math.sqrt(-2.0 * Math.log(u1));
        double z0 = magnitude * Math.cos(2.0 * Math.PI * u2);
        double z1 = magnitude * Math.sin(2.0 * Math.PI * u2);

        // Transform from standard normal (mean=0, stdDev=1) to desired distribution
        cachedValue = mean + z1 * standardDeviation;
        return mean + z0 * standardDeviation;
    }

    /**
     * Get the mean (μ) of this distribution.
     */
    public double getMean() {
        return mean;
    }

    /**
     * Get the standard deviation (σ) of this distribution.
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }
}
