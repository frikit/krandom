/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Picks {@code count} distinct elements (without replacement) from a source list.
 *
 * @param <T> element type
 */
public final class PickSetGenerator<T> implements Generator<List<T>> {

    private final List<T> source;
    private final int     count;
    private final Random  random;

    /**
     * Creates a pick-set generator backed by {@link SecureRandom}.
     *
     * @param source source list; must not be null
     * @param count  number of unique elements to pick
     */
    public PickSetGenerator(List<T> source, int count) {
        this(source, count, new SecureRandom());
    }

    /**
     * Creates a pick-set generator with deterministic seed support.
     *
     * @param source source list; must not be null
     * @param count  number of unique elements to pick
     * @param seed   deterministic seed
     */
    public PickSetGenerator(List<T> source, int count, long seed) {
        this(source, count, new Random(seed));
    }

    private PickSetGenerator(List<T> source, int count, Random random) {
        Objects.requireNonNull(source, "source must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
        if (count > source.size()) {
            throw new IllegalArgumentException("count must be <= source size (" + source.size() + "), got: " + count);
        }
        this.source = List.copyOf(source);
        this.count = count;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public List<T> generate() {
        if (count == 0) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, random);
        return List.copyOf(copy.subList(0, count));
    }
}

