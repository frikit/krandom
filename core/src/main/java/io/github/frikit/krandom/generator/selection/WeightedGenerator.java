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
 * Selects values according to positive integer weights.
 *
 * @param <T> element type
 */
public final class WeightedGenerator<T> implements Generator<T> {

    private final List<T> values;
    private final int[]   cumulativeWeights;
    private final int     totalWeight;
    private final Random  random;

    /**
     * Creates a weighted generator backed by the default fast PRNG.
     *
     * @param values  values to choose from; must not be null/empty
     * @param weights positive weights; same size as values
     */
    public WeightedGenerator(List<T> values, List<Integer> weights) {
        this(values, weights, new Random());
    }

    /**
     * Creates a weighted generator whose draws come from the configuration's random source.
     *
     * @param values  candidate values; must not be null or empty
     * @param weights positive weights parallel to {@code values}
     * @param config generator configuration; must not be {@code null}
     */
    public WeightedGenerator(List<T> values, List<Integer> weights, GeneratorConfig config) {
        this(values, weights, Objects.requireNonNull(config, "config must not be null").createRandom());
    }

    /**
     * Creates a weighted generator with deterministic seed support.
     *
     * @param values  values to choose from; must not be null/empty
     * @param weights positive weights; same size as values
     * @param seed    deterministic seed
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #WeightedGenerator(List, List, GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
    public WeightedGenerator(List<T> values, List<Integer> weights, long seed) {
        this(values, weights, new Random(seed));
    }

    private WeightedGenerator(List<T> values, List<Integer> weights, Random random) {
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(weights, "weights must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        if (values.size() != weights.size()) {
            throw new IllegalArgumentException(
                "values and weights must have the same size, got: " + values.size() + " and " + weights.size());
        }
        this.values = List.copyOf(values);
        this.random = Objects.requireNonNull(random, "random must not be null");

        this.cumulativeWeights = new int[weights.size()];
        long running = 0;
        for (int i = 0; i < weights.size(); i++) {
            Integer w = weights.get(i);
            if (w == null || w <= 0) {
                throw new IllegalArgumentException("weights must be positive integers");
            }
            running += w;
            if (running > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("sum of weights exceeds Integer.MAX_VALUE");
            }
            cumulativeWeights[i] = (int) running;
        }
        this.totalWeight = (int) running;
    }

    @Override
    public T generate() {
        int roll = random.nextInt(totalWeight) + 1; // [1, totalWeight]
        int idx = 0;
        while (roll > cumulativeWeights[idx]) {
            idx++;
        }
        return values.get(idx);
    }
}
