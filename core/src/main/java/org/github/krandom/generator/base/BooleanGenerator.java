/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random {@link Boolean} values with configurable probability.
 *
 * <p>By default, generates {@code true} and {@code false} with equal probability (50%).
 * Use {@link #withLikelihood(int)} to bias the probability toward {@code true}.
 *
 * <pre>{@code
 *   // 50/50 coin flip
 *   boolean coinFlip = new BooleanGenerator().generate();
 *
 *   // 80% chance of true (weighted)
 *   boolean biased = new BooleanGenerator().withLikelihood(80).generate();
 *
 *   // Deterministic for tests
 *   List<Boolean> flips = new BooleanGenerator(12345L).withLikelihood(75).generateList(100);
 * }</pre>
 */
public final class BooleanGenerator implements Generator<Boolean> {

    private final RandomGenerator random;
    private final int likelihood; // 0-100: probability of returning true

    /** Uses {@link SecureRandom} — non-deterministic, 50% likelihood. */
    public BooleanGenerator() {
        this.random = new SecureRandom();
        this.likelihood = 50;
    }

    /** Uses a seeded {@link Random} for deterministic, reproducible output with 50% likelihood. */
    public BooleanGenerator(long seed) {
        this.random = new Random(seed);
        this.likelihood = 50;
    }

    /**
     * Private constructor for creating instances with custom likelihood.
     *
     * @param random the random generator
     * @param likelihood probability of returning true (0-100)
     */
    private BooleanGenerator(RandomGenerator random, int likelihood) {
        this.random = random;
        this.likelihood = likelihood;
    }

    /**
     * Returns a new {@link BooleanGenerator} that produces {@code true} with the specified probability.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code withLikelihood(0)} — always returns {@code false}
     *   <li>{@code withLikelihood(50)} — 50% chance (equal probability)
     *   <li>{@code withLikelihood(80)} — 80% chance of {@code true}
     *   <li>{@code withLikelihood(100)} — always returns {@code true}
     * </ul>
     *
     * <p>Note: Cannot preserve seed when creating a new instance. The returned generator
     * will use the same underlying random source but cannot be independently seeded.
     *
     * @param likelihood probability of returning {@code true} (0-100 inclusive)
     * @return a new generator with the specified likelihood
     * @throws IllegalArgumentException if likelihood is not in range [0, 100]
     */
    public BooleanGenerator withLikelihood(int likelihood) {
        if (likelihood < 0 || likelihood > 100) {
            throw new IllegalArgumentException(
                    "Likelihood must be between 0 and 100, got: " + likelihood);
        }
        return new BooleanGenerator(this.random, likelihood);
    }

    @Override
    public Boolean generate() {
        if (likelihood == 0) {
            return false;
        }
        if (likelihood == 100) {
            return true;
        }
        // Generate random int in [0, 100) and return true if < likelihood
        return random.nextInt(100) < likelihood;
    }
}
