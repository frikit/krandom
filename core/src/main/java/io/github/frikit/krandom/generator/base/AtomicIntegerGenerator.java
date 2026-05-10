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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates random {@link AtomicInteger} values.
 */
public final class AtomicIntegerGenerator implements Generator<AtomicInteger>, Seedable {

    private final Random random;
    private final int    min;
    private final int    max;

    public AtomicIntegerGenerator() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, GeneratorConfig.defaults());
    }

    public AtomicIntegerGenerator(GeneratorConfig config) {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, config);
    }

    public AtomicIntegerGenerator(int min, int max) {
        this(min, max, GeneratorConfig.defaults());
    }

    public AtomicIntegerGenerator(int min, int max, long seed) {
        this(min, max, GeneratorConfig.builder().seed(seed).build());
    }

    private AtomicIntegerGenerator(int min, int max, GeneratorConfig config) {
        if (min == max) {
            throw new IllegalArgumentException("min and max must differ, both were: " + min);
        }
        this.min = min;
        this.max = max;
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
    }

    @Override
    public AtomicInteger generate() {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        return new AtomicInteger(random.nextInt(lo, hi));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
