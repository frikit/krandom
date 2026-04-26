/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

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
 * }</pre>
 */
public final class IntGenerator extends AbstractBoundedGenerator<Integer> {

    public IntGenerator() {
        super(Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }

    public IntGenerator(int min, int max) {
        super(min, max, null);
    }

    public IntGenerator(int min, int max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate an int in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min == max}
     */
    @Override
    public Integer generate(Integer min, Integer max) {
        validate(min, max);
        int lo = lo(min, max);
        int hi = hi(min, max);
        return random.nextInt(lo, hi);
    }
}
