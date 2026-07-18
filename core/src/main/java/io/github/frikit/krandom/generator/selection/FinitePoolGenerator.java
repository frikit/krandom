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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

/**
 * Emits every value in a finite pool at most once before reporting exhaustion.
 *
 * @param <T> value type
 */
public final class FinitePoolGenerator<T> implements Generator<T> {

    private final List<T> values;
    private final Random  random;
    private List<T>       remaining;

    /**
     * Creates a pool using the default random source.
     *
     * @param values non-empty values to emit
     */
    public FinitePoolGenerator(List<T> values) {
        this(values, new Random());
    }

    /**
     * Creates a deterministically seeded pool.
     *
     * @param values non-empty values to emit
     * @param seed deterministic shuffle seed
     */
    public FinitePoolGenerator(List<T> values, long seed) {
        this(values, new Random(seed));
    }

    private FinitePoolGenerator(List<T> values, Random random) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        values.forEach(value -> Objects.requireNonNull(value, "values must not contain null"));
        this.values = List.copyOf(values);
        this.random = random;
        reset();
    }

    @Override
    public T generate() {
        if (remaining.isEmpty()) {
            throw new NoSuchElementException("Finite pool is exhausted; call reset() to start a new cycle");
        }
        return remaining.removeLast();
    }

    /**
     * Starts a new independently shuffled pool cycle.
     */
    public void reset() {
        List<T> shuffled = new ArrayList<>(values);
        Collections.shuffle(shuffled, random);
        remaining = shuffled;
    }

    /**
     * Returns the number of values remaining in the current cycle.
     *
     * @return remaining value count
     */
    public int remaining() {
        return remaining.size();
    }
}
