/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates a variable-length list by choosing among one or more source generators.
 *
 * @param <T> element type
 */
public final class SequenceGenerator<T extends @Nullable Object> implements Generator<List<@Nullable T>> {

    private final List<Generator<? extends T>> sources;
    private final int                          minLength;
    private final int                          maxLength;
    private final double                       nullProbability;
    private final Random                       random;

    /**
     * Creates a generator with the default configuration and no null elements.
     *
     * @param sources generators from which each element is selected
     * @param minLength inclusive minimum generated length
     * @param maxLength inclusive maximum generated length
     */
    public SequenceGenerator(List<? extends Generator<? extends T>> sources, int minLength, int maxLength) {
        this(GeneratorConfig.defaults(), sources, minLength, maxLength, 0.0);
    }

    /**
     * Creates a generator with explicit configuration and null probability.
     *
     * @param config generator configuration
     * @param sources generators from which each element is selected
     * @param minLength inclusive minimum generated length
     * @param maxLength inclusive maximum generated length
     * @param nullProbability chance of generating a null element in {@code [0,1]}
     */
    public SequenceGenerator(GeneratorConfig config,
                             List<? extends Generator<? extends T>> sources,
                             int minLength,
                             int maxLength,
                             double nullProbability) {
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
        Objects.requireNonNull(sources, "sources must not be null");
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("sources must not be empty");
        }
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0, got: " + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("maxLength must be >= minLength, got: " + maxLength + " < " + minLength);
        }
        if (nullProbability < 0.0 || nullProbability > 1.0) {
            throw new IllegalArgumentException("nullProbability must be in [0,1], got: " + nullProbability);
        }
        List<Generator<? extends T>> copy = new ArrayList<>(sources.size());
        for (Generator<? extends T> source : sources) {
            copy.add(Objects.requireNonNull(source, "sources must not contain null"));
        }
        this.sources = List.copyOf(copy);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.nullProbability = nullProbability;
    }

    @Override
    public List<@Nullable T> generate() {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        List<@Nullable T> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(random.nextDouble() < nullProbability ? null : sources.get(random.nextInt(sources.size())).generate());
        }
        return Collections.unmodifiableList(values);
    }
}
