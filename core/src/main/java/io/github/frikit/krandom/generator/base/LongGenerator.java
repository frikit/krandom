/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

/**
 * Generates random {@link Long} values.
 *
 * <p>Default range: [{@code Long.MIN_VALUE}, {@code Long.MAX_VALUE}) — the full 64-bit range
 * minus the maximum value itself (exclusive upper bound).
 *
 * <pre>{@code
 *   long any       = new LongGenerator().generate();
 *   long timestamp = new LongGenerator(0L, System.currentTimeMillis()).generate();
 * }</pre>
 */
public final class LongGenerator extends AbstractBoundedGenerator<Long> {

    public LongGenerator() {
        super(Long.MIN_VALUE, Long.MAX_VALUE, null);
    }

    public LongGenerator(long min, long max) {
        super(min, max, null);
    }

    public LongGenerator(long min, long max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate a long in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min >= max}
     */
    @Override
    public Long generate(Long min, Long max) {
        validate(min, max);
                return random.nextLong(min, max);
    }
}
