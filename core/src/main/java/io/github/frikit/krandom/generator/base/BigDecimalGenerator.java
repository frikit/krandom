/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;

import java.math.BigDecimal;
import java.security.SecureRandom;
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

    private final BigDecimal min;
    private final BigDecimal max;
    private final int        scale;
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
        this.min = min;
        this.max = max;
        this.scale = scale;
        this.random = seed != null ? new Random(seed) : new SecureRandom();
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
        long lo = min.scaleByPowerOfTen(scale).longValue();
        long hi = max.scaleByPowerOfTen(scale).longValue();
        long v = random.nextLong(lo, hi + 1);
        return BigDecimal.valueOf(v, scale);
    }
}
