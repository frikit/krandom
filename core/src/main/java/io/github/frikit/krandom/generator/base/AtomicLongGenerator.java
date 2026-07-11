/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates random {@link AtomicLong} values.
 */
public final class AtomicLongGenerator implements Generator<AtomicLong>, Seedable {

    private final Random random;
    private final long   min;
    private final long   max;

    public AtomicLongGenerator() {
        this(Long.MIN_VALUE, Long.MAX_VALUE, GeneratorConfig.defaults());
    }

    public AtomicLongGenerator(GeneratorConfig config) {
        this(Long.MIN_VALUE, Long.MAX_VALUE, config);
    }

    public AtomicLongGenerator(long min, long max) {
        this(min, max, GeneratorConfig.defaults());
    }

    public AtomicLongGenerator(long min, long max, long seed) {
        this(min, max, GeneratorConfig.builder().seed(seed).build());
    }

    private AtomicLongGenerator(long min, long max, GeneratorConfig config) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max, got: min=" + min + ", max=" + max);
        }
        this.min = min;
        this.max = max;
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
    }

    @Override
    public AtomicLong generate() {
        long lo = Math.min(min, max);
        long hi = Math.max(min, max);
        return new AtomicLong(random.nextLong(lo, hi));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
