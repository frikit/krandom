/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link BigDecimal} values.
 *
 * <p>Default range: [0, 1&nbsp;000&nbsp;000] with scale 2 (i.e. two decimal places).
 *
 * <pre>{@code
 *   BigDecimal price  = new BigDecimalGenerator().generate();           // 0.00 – 1000000.00
 *   BigDecimal amount = new BigDecimalGenerator(
 *                           new BigDecimal("0.01"),
 *                           new BigDecimal("9.99")).generate();
 * }</pre>
 */
public final class BigDecimalGenerator implements Generator<BigDecimal> {

    private static final BigDecimal DEFAULT_MIN   = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_MAX   = new BigDecimal("1000000");
    private static final int        DEFAULT_SCALE = 2;

    private final int        scale;
    private final long       originInclusive;
    private final long       boundExclusive;
    private final Random     random;

    /**
     * Default range [0, 1&nbsp;000&nbsp;000] with scale 2.
     */
    public BigDecimalGenerator() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_SCALE, null);
    }

    /**
     * Custom range with default scale 2.
     *
     * @param min lower bound (inclusive); must not be {@code null} and must be &lt; {@code max}
     * @param max upper bound (inclusive); must not be {@code null} and must be &gt; {@code min}
     */
    public BigDecimalGenerator(BigDecimal min, BigDecimal max) {
        this(min, max, DEFAULT_SCALE, null);
    }

    /**
     * Custom range and scale.
     *
     * @param min   lower bound (inclusive)
     * @param max   upper bound (inclusive)
     * @param scale number of decimal places (must be &gt;= 0)
     */
    public BigDecimalGenerator(BigDecimal min, BigDecimal max, int scale) {
        this(min, max, scale, null);
    }

    /**
     * Custom range, scale, and PRNG seed for reproducible output.
     *
     * @param min   lower bound (inclusive)
     * @param max   upper bound (inclusive)
     * @param scale number of decimal places (must be &gt;= 0)
     * @param seed  PRNG seed
     */
    public BigDecimalGenerator(BigDecimal min, BigDecimal max, int scale, long seed) {
        this(min, max, scale, (Long) seed);
    }

    private BigDecimalGenerator(BigDecimal min, BigDecimal max, int scale, Long seed) {
        Objects.requireNonNull(min, "min must not be null");
        Objects.requireNonNull(max, "max must not be null");
        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException("min must be < max, got min=" + min + " max=" + max);
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be >= 0, was: " + scale);
        }
        this.scale = scale;
        // Pre-scale the bounds to the integer domain used by generate(), failing fast (at
        // construction) if either bound overflows long once scaled — previously longValue()
        // narrowed silently and could yield an invalid range or out-of-range values.
        this.originInclusive = scaledBound(min);
        this.boundExclusive = boundExclusive(scaledBound(max));
        this.random = seed != null ? new Random(seed) : new Random();
    }

    private long scaledBound(BigDecimal value) {
        // toBigInteger() truncates any precision finer than the configured scale (existing
        // behaviour); longValueExact() then rejects only genuine magnitude overflow.
        try {
            return value.scaleByPowerOfTen(scale).toBigInteger().longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                "bound " + value + " scaled by 10^" + scale + " exceeds the supported long range", e);
        }
    }

    private static long boundExclusive(long scaledMax) {
        try {
            return Math.addExact(scaledMax, 1L);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                "max bound scaled by 10^scale is too close to Long.MAX_VALUE to represent an exclusive bound", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Scales both bounds by {@code 10^scale}, picks a uniform random integer in that range,
     * then converts back to a {@link BigDecimal} with the configured scale.
     *
     * @return a random {@link BigDecimal} in [min, max]; never {@code null}
     */
    @Override
    public BigDecimal generate() {
        long v = random.nextLong(originInclusive, boundExclusive);
        return BigDecimal.valueOf(v, scale);
    }
}
