/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.GeneratorConfig;

/**
 * Generates random {@link Byte} values.
 *
 * <p>Default range: [{@code Byte.MIN_VALUE}, {@code Byte.MAX_VALUE}) = [-128, 127).
 *
 * <pre>{@code
 *   byte value  = new ByteGenerator().generate();
 *   byte inRange = new ByteGenerator((byte) 0, (byte) 100).generate();
 * }</pre>
 */
public final class ByteGenerator extends AbstractBoundedGenerator<Byte> {

    public ByteGenerator() {
        super(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public ByteGenerator(byte min, byte max) {
        super(min, max);
    }

    /**
     * Creates a generator over the default range using the configuration's random source.
     *
     * @param config generator configuration; must not be {@code null}
     */
    public ByteGenerator(GeneratorConfig config) {
        super(Byte.MIN_VALUE, Byte.MAX_VALUE, config);
    }

    /**
     * Creates a generator over {@code [min, max)} using the configuration's random source.
     *
     * @param min    lower bound (inclusive)
     * @param max    upper bound (exclusive)
     * @param config generator configuration; must not be {@code null}
     */
    public ByteGenerator(byte min, byte max, GeneratorConfig config) {
        super(min, max, config);
    }

    /**
     * Generate a byte in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min >= max}
     */
    @Override
    public Byte generate(Byte min, Byte max) {
        validate(min, max);
        int lo = min.intValue();
        int hi = max.intValue();
        return (byte) random.nextInt(lo, hi);
    }
}
