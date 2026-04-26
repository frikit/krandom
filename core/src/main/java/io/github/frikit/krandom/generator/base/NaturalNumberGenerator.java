/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import java.util.HashSet;
import java.util.Set;

/**
 * Generates random natural numbers (non-negative integers).
 *
 * <p>Default range: [0, {@code Integer.MAX_VALUE}) — all non-negative 32-bit integers.
 *
 * <p>Supports value exclusion via {@link #excluding(int...)} to prevent specific values
 * from being generated:
 *
 * <pre>{@code
 *   int natural     = new NaturalNumberGenerator().generate();           // [0, Integer.MAX_VALUE)
 *   int zeroToHun   = new NaturalNumberGenerator(0, 100).generate();     // [0, 100)
 *   int noFive      = new NaturalNumberGenerator(0, 10).excluding(5).generate();
 *   int noMultiple  = new NaturalNumberGenerator(0, 20).excluding(5, 10, 15).generate();
 * }</pre>
 *
 * <p><strong>Exclusion behavior:</strong>
 * <ul>
 *   <li>Excluded values are never generated, even if in range</li>
 *   <li>Exclusions outside the range are silently ignored</li>
 *   <li>Throws {@link IllegalStateException} if all possible values are excluded</li>
 *   <li>Uses retry logic with max attempts to prevent infinite loops</li>
 * </ul>
 */
public final class NaturalNumberGenerator extends AbstractBoundedGenerator<Integer> {

    private static final int MAX_RETRY_ATTEMPTS = 10000;

    private final Set<Integer> excludedValues;

    public NaturalNumberGenerator() {
        super(0, Integer.MAX_VALUE, null);
        this.excludedValues = new HashSet<>();
    }

    public NaturalNumberGenerator(int min, int max) {
        super(min, max, null);
        this.excludedValues = new HashSet<>();
    }

    public NaturalNumberGenerator(int min, int max, long seed) {
        super(min, max, seed);
        this.excludedValues = new HashSet<>();
    }

    private NaturalNumberGenerator(int min, int max, Long seed, Set<Integer> excludedValues) {
        super(min, max, seed);
        this.excludedValues = new HashSet<>(excludedValues);
    }

    /**
     * Exclude specific values from generation.
     *
     * <p>Returns a new generator with the exclusion list applied. The original generator
     * is not modified.
     *
     * @param values values to exclude (can be outside range, will be ignored)
     * @return new generator with exclusions
     */
    public NaturalNumberGenerator excluding(int... values) {
        Set<Integer> newExclusions = new HashSet<>(this.excludedValues);
        for (int value : values) {
            newExclusions.add(value);
        }

        // Create new instance with updated exclusions
        Long seed = null; // Cannot extract seed from existing generator
        return new NaturalNumberGenerator(getMin(), getMax(), seed, newExclusions);
    }

    /**
     * Generate a natural number in the half-open range [{@code min}, {@code max}).
     *
     * <p>Excluded values within the range will be skipped.
     *
     * @throws IllegalArgumentException if {@code min == max}
     * @throws IllegalStateException    if all values in range are excluded
     */
    @Override
    public Integer generate(Integer min, Integer max) {
        validate(min, max);
        int lo = lo(min, max);
        int hi = hi(min, max);

        // If no exclusions, use simple generation
        if (excludedValues.isEmpty()) {
            return random.nextInt(lo, hi);
        }

        // Count how many values in range are excluded
        long rangeSize = (long) hi - lo;
        long excludedInRange = excludedValues.stream()
                                             .filter(v -> v >= lo && v < hi)
                                             .count();

        if (excludedInRange >= rangeSize) {
            throw new IllegalStateException(
                "All values in range [" + lo + ", " + hi + ") are excluded");
        }

        // Generate with retry for excluded values
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            int value = random.nextInt(lo, hi);
            if (!excludedValues.contains(value)) {
                return value;
            }
            attempts++;
        }

        throw new IllegalStateException(
            "Failed to generate non-excluded value after " + MAX_RETRY_ATTEMPTS + " attempts");
    }
}
