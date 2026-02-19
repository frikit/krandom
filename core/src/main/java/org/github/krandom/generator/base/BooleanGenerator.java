/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random {@link Boolean} values with equal probability for {@code true} and {@code false}.
 *
 * <pre>{@code
 *   boolean coinFlip = new BooleanGenerator().generate();
 *   List<Boolean> flips = new BooleanGenerator().generateList(10);
 * }</pre>
 */
public final class BooleanGenerator implements Generator<Boolean> {

    private final RandomGenerator random;

    /** Uses {@link SecureRandom} — non-deterministic. */
    public BooleanGenerator() {
        this.random = new SecureRandom();
    }

    /** Uses a seeded {@link Random} for deterministic, reproducible output. */
    public BooleanGenerator(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Boolean generate() {
        return random.nextBoolean();
    }
}
