/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.time.Period;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random positive {@link Period} values.
 */
public final class PeriodGenerator implements Generator<Period>, Seedable {

    private static final int DEFAULT_MAX_YEARS  = 10;
    private static final int DEFAULT_MAX_MONTHS = 11;
    private static final int DEFAULT_MAX_DAYS   = 30;

    private final Random random;

    public PeriodGenerator() {
        this(GeneratorConfig.defaults());
    }

    public PeriodGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public PeriodGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public Period generate() {
        return Period.of(
            random.nextInt(DEFAULT_MAX_YEARS + 1),
            random.nextInt(DEFAULT_MAX_MONTHS + 1),
            random.nextInt(DEFAULT_MAX_DAYS + 1));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
