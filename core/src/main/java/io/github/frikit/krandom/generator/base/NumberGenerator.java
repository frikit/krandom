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

/**
 * Generates random {@link Number} values.
 *
 * <p>The default output is an {@link Integer}, matching the reference randomizer family while
 * keeping a broad {@code Number} return type for object-generation and migration use cases.
 */
public final class NumberGenerator implements Generator<Number>, Seedable {

    private final Random random;

    public NumberGenerator() {
        this(GeneratorConfig.defaults());
    }

    public NumberGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public NumberGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public Number generate() {
        return random.nextInt();
    }

    /**
     * Generates an {@link Integer} in the half-open range [{@code min}, {@code max}).
     */
    public Number generate(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max, got: min=" + min + ", max=" + max);
        }
        return random.nextInt(min, max);
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
