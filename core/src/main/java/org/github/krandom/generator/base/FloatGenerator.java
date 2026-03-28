/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Generates random {@link Float} values.
 *
 * <p>Default range: [{@code 0.0f}, {@code 1.0f}) — matching Java's {@code Random.nextFloat()}.
 * Specify a custom range via the two-/three-arg constructors.
 *
 * <p><b>Note:</b> avoid ranges where {@code max - min} overflows {@code Float.MAX_VALUE};
 * use {@link DoubleGenerator} for very wide ranges.
 *
 * <p>Supports fixed decimal precision via {@link #withPrecision(int)}:
 *
 * <pre>{@code
 *   float unit   = new FloatGenerator().generate();         // [0, 1)
 *   float celsius = new FloatGenerator(-40f, 50f).generate();
 *   float price = new FloatGenerator(0.0f, 100.0f).withPrecision(2).generate(); // 2 decimals
 * }</pre>
 */
public final class FloatGenerator extends AbstractBoundedGenerator<Float> {

    private final Integer precision;

    public FloatGenerator() {
        super(0f, 1f, null);
        this.precision = null;
    }

    public FloatGenerator(float min, float max) {
        super(min, max, null);
        this.precision = null;
    }

    public FloatGenerator(float min, float max, long seed) {
        super(min, max, seed);
        this.precision = null;
    }

    private FloatGenerator(float min, float max, Long seed, Integer precision) {
        super(min, max, seed);
        this.precision = precision;
    }

    /**
     * Return a new generator that rounds generated values to the specified number of decimal places.
     *
     * <p>Uses {@link RoundingMode#HALF_UP} for rounding.
     *
     * @param decimals number of decimal places (0-7, float precision limit)
     * @return new generator with fixed precision
     * @throws IllegalArgumentException if decimals is negative or greater than 7
     */
    public FloatGenerator withPrecision(int decimals) {
        if (decimals < 0 || decimals > 7) {
            throw new IllegalArgumentException(
                "Precision must be between 0 and 7, got: " + decimals);
        }
        Long seed = null; // Cannot extract seed from existing generator
        return new FloatGenerator(getMin(), getMax(), seed, decimals);
    }

    /**
     * Generate a float in the half-open range [{@code min}, {@code max}).
     *
     * <p>If precision is set via {@link #withPrecision(int)}, the result is rounded
     * to the specified number of decimal places.
     *
     * @throws IllegalArgumentException if {@code min == max}
     */
    @Override
    public Float generate(Float min, Float max) {
        validate(min, max);
        float lo = lo(min, max);
        float hi = hi(min, max);
        float value = random.nextFloat(lo, hi);

        if (precision != null) {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            return bd.floatValue();
        }

        return value;
    }
}
