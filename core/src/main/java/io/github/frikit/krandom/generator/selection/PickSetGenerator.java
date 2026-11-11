/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Picks {@code count} distinct elements (without replacement) from a source list.
 *
 * <p>Elements are distinct by {@link Object#equals(Object)}: a value that the source holds more than
 * once is picked at most once, and is as much more likely to be picked as it has copies. So
 * {@code count} may be at most the number of distinct values in the source.
 *
 * @param <T> element type
 */
public final class PickSetGenerator<T> implements Generator<List<T>> {

    private final List<T> source;
    private final int count;
    private final Random random;

    /**
     * Creates a pick-set generator backed by the default fast PRNG.
     *
     * @param source source list; must not be null
     * @param count  number of unique elements to pick; at most the number of distinct values in {@code source}
     */
    public PickSetGenerator(List<T> source, int count) {
        this(source, count, new Random());
    }

    /**
     * Creates a pick-set generator whose draws come from the configuration's random source.
     *
     * @param source source list; must not be null
     * @param count  number of distinct elements to pick; must be from zero to the number of distinct
     *               values in {@code source}
     * @param config generator configuration; must not be {@code null}
     */
    public PickSetGenerator(List<T> source, int count, GeneratorConfig config) {
        this(
                source,
                count,
                Objects.requireNonNull(config, "config must not be null").createRandom());
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
        int distinct = new HashSet<>(this.source).size();
        if (count > distinct) {
            throw new IllegalArgumentException(
                    "count must be <= the number of distinct values in source (" + distinct + "), got: " + count);
        }
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
        // The first count distinct values of the shuffle: its first count elements where they repeat no value.
        Set<T> picked = new LinkedHashSet<>();
        for (int index = 0; picked.size() < count; index++) {
            picked.add(copy.get(index));
        }
        return List.copyOf(picked);
    }
}
