/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.GeneratorConfig;

/**
 * Generates random {@link Short} values.
 *
 * <p>Default range: [{@code Short.MIN_VALUE}, {@code Short.MAX_VALUE}) = [-32768, 32767).
 *
 * <pre>{@code
 *   short value   = new ShortGenerator().generate();
 *   short inRange = new ShortGenerator((short) 100, (short) 200).generate();
 * }</pre>
 */
public final class ShortGenerator extends AbstractBoundedGenerator<Short> {

    public ShortGenerator() {
        super(Short.MIN_VALUE, Short.MAX_VALUE, null);
    }

    public ShortGenerator(short min, short max) {
        super(min, max, null);
    }

    /**
     * Creates a generator over the default range using the configuration's random source.
     *
     * @param config generator configuration; must not be {@code null}
     */
    public ShortGenerator(GeneratorConfig config) {
        super(config, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    /**
     * Creates a generator over {@code [min, max)} using the configuration's random source.
     *
     * @param min    lower bound (inclusive)
     * @param max    upper bound (exclusive)
     * @param config generator configuration; must not be {@code null}
     */
    public ShortGenerator(short min, short max, GeneratorConfig config) {
        super(config, min, max);
    }

    /**
     * Creates a seeded generator over {@code [min, max)}.
     *
     * @param min  lower bound (inclusive)
     * @param max  upper bound (exclusive)
     * @param seed raw seed
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #ShortGenerator(short, short, GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
    public ShortGenerator(short min, short max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate a short in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min >= max}
     */
    @Override
    public Short generate(Short min, Short max) {
        validate(min, max);
        int lo = min.intValue();
        int hi = max.intValue();
        return (short) random.nextInt(lo, hi);
    }
}
