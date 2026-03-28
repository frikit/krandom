/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates random prime numbers within a specified range.
 *
 * <p>Uses the Sieve of Eratosthenes to efficiently compute all primes up to the maximum value,
 * then randomly selects from that set.
 *
 * <p>Default range: [2, 1000) — primes less than 1000.
 *
 * <pre>{@code
 *   int prime = new PrimeGenerator().generate();              // [2, 1000)
 *   int small = new PrimeGenerator(2, 100).generate();        // primes < 100
 *   int large = new PrimeGenerator(1000, 10000).generate();   // primes in [1000, 10000)
 * }</pre>
 *
 * <p><strong>Performance note:</strong> The sieve is computed once during construction,
 * so instantiation cost is O(n log log n) where n = max value. Subsequent {@link #generate()}
 * calls are O(1) (random selection from precomputed list).
 *
 * <p><strong>Note:</strong> Construction throws {@link IllegalStateException} if no primes
 * exist in the specified range.
 */
public final class PrimeGenerator extends AbstractBoundedGenerator<Integer> {

    private final List<Integer> primes;

    public PrimeGenerator() {
        super(2, 1000, null);
        this.primes = computePrimesInRange(2, 1000);
        validatePrimes();
    }

    public PrimeGenerator(int min, int max) {
        super(min, max, null);
        this.primes = computePrimesInRange(lo(min, max), hi(min, max));
        validatePrimes();
    }

    public PrimeGenerator(int min, int max, long seed) {
        super(min, max, seed);
        this.primes = computePrimesInRange(lo(min, max), hi(min, max));
        validatePrimes();
    }

    /**
     * Compute all prime numbers in the range [min, max) using the Sieve of Eratosthenes.
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (exclusive)
     * @return list of primes in range
     */
    private static List<Integer> computePrimesInRange(int min, int max) {
        if (max <= 2) {
            return new ArrayList<>(); // No primes below 2
        }

        // Sieve of Eratosthenes up to max-1
        boolean[] isPrime = new boolean[max];
        for (int i = 2; i < max; i++) {
            isPrime[i] = true;
        }

        for (int i = 2; i * i < max; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j < max; j += i) {
                    isPrime[j] = false;
                }
            }
        }

        // Collect primes in range [min, max)
        List<Integer> primes = new ArrayList<>();
        for (int i = Math.max(2, min); i < max; i++) {
            if (isPrime[i]) {
                primes.add(i);
            }
        }

        return primes;
    }

    private void validatePrimes() {
        if (primes.isEmpty()) {
            throw new IllegalStateException(
                "No prime numbers exist in range [" + getMin() + ", " + getMax() + ")");
        }
    }

    /**
     * Generate a random prime number in the configured range.
     *
     * @return a prime number
     */
    @Override
    public Integer generate() {
        return generate(getMin(), getMax());
    }

    /**
     * Generate a random prime number in the specified range [{@code min}, {@code max}).
     *
     * <p><strong>Note:</strong> This implementation ignores the provided bounds and uses
     * the range configured at construction time, as the prime list is precomputed.
     * To generate primes in a different range, create a new {@code PrimeGenerator} instance.
     *
     * @param min lower bound (ignored, uses constructor range)
     * @param max upper bound (ignored, uses constructor range)
     * @return a prime number from the precomputed list
     */
    @Override
    public Integer generate(Integer min, Integer max) {
        // Select random prime from precomputed list
        int index = random.nextInt(primes.size());
        return primes.get(index);
    }

    /**
     * Get the count of prime numbers in the configured range.
     *
     * @return number of primes available for generation
     */
    public int getPrimeCount() {
        return primes.size();
    }
}
