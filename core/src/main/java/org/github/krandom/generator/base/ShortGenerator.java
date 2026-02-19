/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

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

    public ShortGenerator(short min, short max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate a short in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min == max}
     */
    @Override
    public Short generate(Short min, Short max) {
        validate(min, max);
        int lo = lo(min, max).intValue();
        int hi = hi(min, max).intValue();
        return (short) random.nextInt(lo, hi);
    }
}
