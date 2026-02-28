/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.selection;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Picks a single random element from a non-empty source list.
 *
 * @param <T> element type
 */
public final class PickGenerator<T> implements Generator<T> {

    private final List<T> source;
    private final Random random;

    /**
     * Creates a pick generator backed by {@link SecureRandom}.
     *
     * @param source source list; must not be null or empty
     */
    public PickGenerator(List<T> source) {
        this(source, new SecureRandom());
    }

    /**
     * Creates a pick generator with deterministic seed support.
     *
     * @param source source list; must not be null or empty
     * @param seed deterministic seed
     */
    public PickGenerator(List<T> source, long seed) {
        this(source, new Random(seed));
    }

    private PickGenerator(List<T> source, Random random) {
        Objects.requireNonNull(source, "source must not be null");
        if (source.isEmpty()) {
            throw new IllegalArgumentException("source must not be empty");
        }
        this.source = List.copyOf(source);
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public T generate() {
        return source.get(random.nextInt(source.size()));
    }
}

