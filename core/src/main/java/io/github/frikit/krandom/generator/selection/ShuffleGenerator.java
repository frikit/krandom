/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Returns a shuffled copy of a source list.
 *
 * @param <T> element type
 */
public final class ShuffleGenerator<T> implements Generator<List<T>> {

    private final List<T> source;
    private final Random  random;

    /**
     * Creates a shuffle generator backed by the default fast PRNG.
     *
     * @param source source list; must not be null
     */
    public ShuffleGenerator(List<T> source) {
        this(source, new Random());
    }

    /**
     * Creates a shuffle generator with deterministic seed support.
     *
     * @param source source list; must not be null
     * @param seed   deterministic seed
     */
    public ShuffleGenerator(List<T> source, long seed) {
        this(source, new Random(seed));
    }

    private ShuffleGenerator(List<T> source, Random random) {
        Objects.requireNonNull(source, "source must not be null");
        this.source = List.copyOf(source);
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public List<T> generate() {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, random);
        return List.copyOf(copy);
    }
}
