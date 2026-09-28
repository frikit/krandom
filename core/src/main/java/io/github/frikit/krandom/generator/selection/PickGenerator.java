/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

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
    private final Random  random;

    /**
     * Creates a pick generator backed by the default fast PRNG.
     *
     * @param source source list; must not be null or empty
     */
    public PickGenerator(List<T> source) {
        this(source, new Random());
    }

    /**
     * Creates a pick generator whose draws come from the configuration's random source.
     *
     * @param source source list; must not be null or empty
     * @param config generator configuration; must not be {@code null}
     */
    public PickGenerator(List<T> source, GeneratorConfig config) {
        this(source, Objects.requireNonNull(config, "config must not be null").createRandom());
    }

    /**
     * Creates a pick generator with deterministic seed support.
     *
     * @param source source list; must not be null or empty
     * @param seed   deterministic seed
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #PickGenerator(List, GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
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
