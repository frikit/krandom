/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.GeneratorConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Generates random {@link Double} values.
 *
 * <p>Default range: [{@code 0.0}, {@code 1.0}) — matching Java's {@code Random.nextDouble()}.
 * Specify a custom range via the two-/three-arg constructors; a {@link GeneratorConfig} supplies the
 * random source (seeded, caller-owned, or secure).
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

    private final Integer         precision;
    /**
     * Most recent seed from the configuration or {@link #reseed(long)}; {@code null} when unseeded.
     */
    private Long                  seed;
    /** Configuration whose random source an unseeded precision generator keeps; may be {@code null}. */
    private final GeneratorConfig config;

    public DoubleGenerator() {
        super(0.0, 1.0);
        this.precision = null;
        this.config = null;
    }

    public DoubleGenerator(double min, double max) {
        super(min, max);
        this.precision = null;
        this.config = null;
    }

    /**
     * Creates a generator over the default range using the configuration's random source.
     *
     * @param config generator configuration; must not be {@code null}
     */
    public DoubleGenerator(GeneratorConfig config) {
        this(0.0, 1.0, config);
    }

    /**
     * Creates a generator over {@code [min, max)} using the configuration's random source, which
     * {@link #withPrecision(int)} keeps.
     *
     * @param min    lower bound (inclusive)
     * @param max    upper bound (exclusive)
     * @param config generator configuration; must not be {@code null}
     */
    public DoubleGenerator(double min, double max, GeneratorConfig config) {
        this(min, max, config, null);
    }

    private DoubleGenerator(double min, double max, Long seed, Integer precision) {
        super(min, max);
        this.precision = precision;
        this.config = null;
        if (seed != null) {
            reseed(seed);
        }
    }

    private DoubleGenerator(double min, double max, GeneratorConfig config, Integer precision) {
        super(min, max, config);
        this.precision = precision;
        this.seed = config.getSeed().isPresent() ? config.getSeed().getAsLong() : null;
        this.config = config;
    }

    /**
     * Return a new generator that rounds generated values to the specified number of decimal places.
     *
     * <p>Uses {@link RoundingMode#HALF_UP} for rounding. Rounded values always stay inside
     * [{@code min}, {@code max}): a draw that rounds onto the exclusive maximum or below the
     * minimum is drawn again.
     *
     * <p>The new generator keeps this generator's seed: it is seeded with the configuration seed or,
     * after {@link #reseed(long)}, with the most recent reseed value, and starts from that seed's
     * initial state. A generator created from an unseeded configuration keeps that configuration's
     * random source; any other unseeded generator produces an unseeded precision generator.
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
        if (seed == null && config != null) {
            return new DoubleGenerator(getMin(), getMax(), config, decimals);
        }
        return new DoubleGenerator(getMin(), getMax(), seed, decimals);
    }

    /**
     * Reseeds this generator and records the seed for {@link #withPrecision(int)}.
     *
     * @param seed new seed
     */
    @Override
    public void reseed(long seed) {
        super.reseed(seed);
        this.seed = seed;
    }

    /**
     * Generate a double in the half-open range [{@code min}, {@code max}).
     *
     * <p>If precision is set via {@link #withPrecision(int)}, the result is rounded
     * to the specified number of decimal places and still lies in [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min >= max}, or if precision is set and the range
     *                                  contains no value with that many decimal places
     */
    @Override
    public Double generate(Double min, Double max) {
        validate(min, max);
        double value = random.nextDouble(min, max);
        if (precision == null) {
            return value;
        }
        double rounded = round(value);
        while (rounded < min || rounded >= max) {
            requireRepresentableValue(min, max);
            rounded = round(random.nextDouble(min, max));
        }
        return rounded;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).doubleValue();
    }

    private void requireRepresentableValue(double min, double max) {
        double smallest = BigDecimal.valueOf(min).setScale(precision, RoundingMode.CEILING).doubleValue();
        if (smallest >= max) {
            throw new IllegalArgumentException("Range [" + min + ", " + max + ") contains no value with "
                                               + precision + " decimal places");
        }
    }
}
