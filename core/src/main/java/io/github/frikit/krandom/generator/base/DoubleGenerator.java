/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Generates random {@link Double} values.
 *
 * <p>Default range: [{@code 0.0}, {@code 1.0}) — matching Java's {@code Random.nextDouble()}.
 * Specify a custom range via the two-/three-arg constructors.
 *
 * <p>Supports fixed decimal precision via {@link #withPrecision(int)}:
 *
 * <pre>{@code
 *   double unit        = new DoubleGenerator().generate();          // [0, 1)
 *   double probability = new DoubleGenerator(0.0, 1.0).generate();
 *   double coordinate  = new DoubleGenerator(-180.0, 180.0).generate();
 *   double price       = new DoubleGenerator(0.0, 100.0).withPrecision(2).generate(); // 2 decimals
 * }</pre>
 */
public final class DoubleGenerator extends AbstractBoundedGenerator<Double> {

    private final Integer precision;

    public DoubleGenerator() {
        super(0.0, 1.0, null);
        this.precision = null;
    }

    public DoubleGenerator(double min, double max) {
        super(min, max, null);
        this.precision = null;
    }

    public DoubleGenerator(double min, double max, long seed) {
        super(min, max, seed);
        this.precision = null;
    }

    private DoubleGenerator(double min, double max, Long seed, Integer precision) {
        super(min, max, seed);
        this.precision = precision;
    }

    /**
     * Return a new generator that rounds generated values to the specified number of decimal places.
     *
     * <p>Uses {@link RoundingMode#HALF_UP} for rounding.
     *
     * @param decimals number of decimal places (0-15)
     * @return new generator with fixed precision
     * @throws IllegalArgumentException if decimals is negative or greater than 15
     */
    public DoubleGenerator withPrecision(int decimals) {
        if (decimals < 0 || decimals > 15) {
            throw new IllegalArgumentException(
                "Precision must be between 0 and 15, got: " + decimals);
        }
        Long seed = null; // Cannot extract seed from existing generator
        return new DoubleGenerator(getMin(), getMax(), seed, decimals);
    }

    /**
     * Generate a double in the half-open range [{@code min}, {@code max}).
     *
     * <p>If precision is set via {@link #withPrecision(int)}, the result is rounded
     * to the specified number of decimal places.
     *
     * @throws IllegalArgumentException if {@code min >= max}
     */
    @Override
    public Double generate(Double min, Double max) {
        validate(min, max);
                double value = random.nextDouble(min, max);

        if (precision != null) {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }

        return value;
    }
}
