/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.GeneratorConfig;

/**
 * Generates random {@link Integer} values.
 *
 * <p>Default range: [{@code Integer.MIN_VALUE}, {@code Integer.MAX_VALUE}) — the full 32-bit range
 * minus the maximum value itself (exclusive upper bound).
 *
 * <pre>{@code
 *   int any     = new IntGenerator().generate();
 *   int positive = new IntGenerator(1, Integer.MAX_VALUE).generate();
 *   int roll     = new IntGenerator(1, 7).generate();  // die roll [1..6]
 *   int seeded   = new IntGenerator(1, 7, GeneratorConfig.builder().seed(42L).build()).generate();
 * }</pre>
 */
public final class IntGenerator extends AbstractBoundedGenerator<Integer> {

    public IntGenerator() {
        super(Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }

    public IntGenerator(int min, int max) {
        super(min, max, null);
    }

    /**
     * Creates a generator over the default range using the configuration's random source.
     *
     * @param config generator configuration; must not be {@code null}
     */
    public IntGenerator(GeneratorConfig config) {
        super(config, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Creates a generator over {@code [min, max)} using the configuration's random source.
     *
     * @param min    lower bound (inclusive)
     * @param max    upper bound (exclusive)
     * @param config generator configuration; must not be {@code null}
     */
    public IntGenerator(int min, int max, GeneratorConfig config) {
        super(config, min, max);
    }

    /**
     * Creates a seeded generator over {@code [min, max)}.
     *
     * @param min  lower bound (inclusive)
     * @param max  upper bound (exclusive)
     * @param seed raw seed
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #IntGenerator(int, int, GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
    public IntGenerator(int min, int max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate an int in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min >= max}
     */
    @Override
    public Integer generate(Integer min, Integer max) {
        validate(min, max);
                return random.nextInt(min, max);
    }
}
