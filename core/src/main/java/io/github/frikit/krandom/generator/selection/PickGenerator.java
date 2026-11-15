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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.jspecify.annotations.Nullable;

/**
 * Picks a single random element from a non-empty source list, which may hold {@code null}.
 *
 * @param <T> element type, which may be nullable
 */
public final class PickGenerator<T extends @Nullable Object> implements Generator<T> {

    private final List<T> source;
    private final Random random;

    /**
     * Creates a pick generator backed by the default fast PRNG.
     *
     * @param source source list; must not be null or empty, and may hold {@code null}
     */
    public PickGenerator(List<T> source) {
        this(source, new Random());
    }

    /**
     * Creates a pick generator whose draws come from the configuration's random source.
     *
     * @param source source list; must not be null or empty, and may hold {@code null}
     * @param config generator configuration; must not be {@code null}
     */
    public PickGenerator(List<T> source, GeneratorConfig config) {
        this(source, Objects.requireNonNull(config, "config must not be null").createRandom());
    }

    private PickGenerator(List<T> source, Random random) {
        Objects.requireNonNull(source, "source must not be null");
        if (source.isEmpty()) {
            throw new IllegalArgumentException("source must not be empty");
        }
        // A copy that keeps null elements, which List.copyOf rejects.
        this.source = Collections.unmodifiableList(new ArrayList<>(source));
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public T generate() {
        return source.get(random.nextInt(source.size()));
    }
}
