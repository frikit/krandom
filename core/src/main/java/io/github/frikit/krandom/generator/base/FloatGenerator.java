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
 * Generates random {@link Float} values.
 *
 * <p>Default range: [{@code 0.0f}, {@code 1.0f}) — matching Java's {@code Random.nextFloat()}.
 * Specify a custom range via the two-/three-arg constructors; a {@link GeneratorConfig} supplies the
 * random source (seeded, caller-owned, or secure).
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

    private final Integer         precision;
    /**
     * Most recent seed from the constructor, the configuration, or {@link #reseed(long)};
     * {@code null} when unseeded.
     */
    private Long                  seed;
    /** Configuration whose random source an unseeded precision generator keeps; may be {@code null}. */
    private final GeneratorConfig config;

    public FloatGenerator() {
        super(0f, 1f, null);
        this.precision = null;
        this.config = null;
    }

    public FloatGenerator(float min, float max) {
        super(min, max, null);
        this.precision = null;
        this.config = null;
    }

    /**
     * Creates a generator over the default range using the configuration's random source.
     *
     * @param config generator configuration; must not be {@code null}
     */
    public FloatGenerator(GeneratorConfig config) {
        this(0f, 1f, config);
    }

    /**
     * Creates a generator over {@code [min, max)} using the configuration's random source, which
     * {@link #withPrecision(int)} keeps.
     *
     * @param min    lower bound (inclusive)
     * @param max    upper bound (exclusive)
     * @param config generator configuration; must not be {@code null}
     */
    public FloatGenerator(float min, float max, GeneratorConfig config) {
        this(min, max, config, null);
    }

    /**
     * Creates a seeded generator over {@code [min, max)}.
     *
     * @param min  lower bound (inclusive)
     * @param max  upper bound (exclusive)
     * @param seed raw seed
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #FloatGenerator(float, float, GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
    public FloatGenerator(float min, float max, long seed) {
        super(min, max, seed);
        this.precision = null;
        this.seed = seed;
        this.config = null;
    }

    private FloatGenerator(float min, float max, Long seed, Integer precision) {
        super(min, max, seed);
        this.precision = precision;
        this.seed = seed;
        this.config = null;
    }

    private FloatGenerator(float min, float max, GeneratorConfig config, Integer precision) {
        super(config, min, max);
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
     * <p>The new generator keeps this generator's seed: it is seeded with the constructor seed or,
     * after {@link #reseed(long)}, with the most recent reseed value, and starts from that seed's
     * initial state. A generator created from an unseeded configuration keeps that configuration's
     * random source; any other unseeded generator produces an unseeded precision generator.
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
        if (seed == null && config != null) {
            return new FloatGenerator(getMin(), getMax(), config, decimals);
        }
        return new FloatGenerator(getMin(), getMax(), seed, decimals);
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
     * Generate a float in the half-open range [{@code min}, {@code max}).
     *
     * <p>If precision is set via {@link #withPrecision(int)}, the result is rounded
     * to the specified number of decimal places and still lies in [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min >= max}, or if precision is set and the range
     *                                  contains no value with that many decimal places
     */
    @Override
    public Float generate(Float min, Float max) {
        validate(min, max);
        float value = random.nextFloat(min, max);
        if (precision == null) {
            return value;
        }
        float rounded = round(value);
        while (rounded < min || rounded >= max) {
            requireRepresentableValue(min, max);
            rounded = round(random.nextFloat(min, max));
        }
        return rounded;
    }

    private float round(float value) {
        return BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).floatValue();
    }

    private void requireRepresentableValue(float min, float max) {
        // Float.toString gives the shortest decimal that identifies the float, e.g. "0.1" for 0.1f.
        float smallest = new BigDecimal(Float.toString(min)).setScale(precision, RoundingMode.CEILING).floatValue();
        if (smallest >= max) {
            throw new IllegalArgumentException("Range [" + min + ", " + max + ") contains no value with "
                                               + precision + " decimal places");
        }
    }
}
