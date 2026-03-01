/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates nullable booleans (true/false/null).
 */
public final class NullableBooleanGenerator implements Generator<Boolean> {

    private final Random random;

    public NullableBooleanGenerator() {
        this(GeneratorConfig.defaults());
    }

    public NullableBooleanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public Boolean generate() {
        int value = random.nextInt(3);
        if (value == 0) {
            return null;
        }
        return value == 1;
    }
}
