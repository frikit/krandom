/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link BigInteger} values.
 *
 * <p>Default range: [0, {@link Long#MAX_VALUE}].
 *
 * <pre>{@code
 *   BigInteger id   = new BigIntegerGenerator().generate();
 *   BigInteger big  = new BigIntegerGenerator(
 *                         BigInteger.ZERO,
 *                         BigInteger.TWO.pow(128)).generate();
 * }</pre>
 */
public final class BigIntegerGenerator implements Generator<BigInteger> {

    private static final BigInteger DEFAULT_MIN = BigInteger.ZERO;
    private static final BigInteger DEFAULT_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final BigInteger min;
    private final BigInteger max;
    private final Random     random;

    /** Default range [0, {@link Long#MAX_VALUE}]. */
    public BigIntegerGenerator() {
        this(DEFAULT_MIN, DEFAULT_MAX, null);
    }

    /**
     * Custom range.
     *
     * @param min lower bound (inclusive); must not be {@code null} and must be &lt; {@code max}
     * @param max upper bound (inclusive); must not be {@code null} and must be &gt; {@code min}
     */
    public BigIntegerGenerator(BigInteger min, BigInteger max) {
        this(min, max, null);
    }

    /**
     * Custom range with PRNG seed for reproducible output.
     *
     * @param min  lower bound (inclusive)
     * @param max  upper bound (inclusive)
     * @param seed PRNG seed
     */
    public BigIntegerGenerator(BigInteger min, BigInteger max, long seed) {
        this(min, max, (Long) seed);
    }

    private BigIntegerGenerator(BigInteger min, BigInteger max, Long seed) {
        Objects.requireNonNull(min, "min must not be null");
        Objects.requireNonNull(max, "max must not be null");
        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException("min must be < max, got min=" + min + " max=" + max);
        }
        this.min    = min;
        this.max    = max;
        this.random = seed != null ? new Random(seed) : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses rejection sampling: repeatedly draws a random non-negative {@link BigInteger}
     * with {@code range.bitLength()} bits until the value falls within [0, range], then
     * shifts by {@code min}.
     *
     * @return a random {@link BigInteger} in [min, max]; never {@code null}
     */
    @Override
    public BigInteger generate() {
        BigInteger range = max.subtract(min);
        BigInteger v;
        do {
            v = new BigInteger(range.bitLength(), random);
        } while (v.compareTo(range) > 0);
        return min.add(v);
    }
}
