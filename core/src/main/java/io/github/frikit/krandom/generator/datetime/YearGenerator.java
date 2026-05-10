/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.time.Year;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link Year} values.
 */
public final class YearGenerator implements Generator<Year>, Seedable {

    private static final int DEFAULT_MIN_YEAR = 1970;
    private static final int DEFAULT_MAX_YEAR = 2100;

    private final Random random;
    private final int    minYear;
    private final int    maxYear;

    public YearGenerator() {
        this(DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR, GeneratorConfig.defaults());
    }

    public YearGenerator(GeneratorConfig config) {
        this(DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR, config);
    }

    public YearGenerator(int minYear, int maxYear) {
        this(minYear, maxYear, GeneratorConfig.defaults());
    }

    public YearGenerator(int minYear, int maxYear, long seed) {
        this(minYear, maxYear, GeneratorConfig.builder().seed(seed).build());
    }

    private YearGenerator(int minYear, int maxYear, GeneratorConfig config) {
        if (minYear > maxYear) {
            throw new IllegalArgumentException("minYear must be <= maxYear");
        }
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
    }

    @Override
    public Year generate() {
        return Year.of(random.nextInt(minYear, maxYear + 1));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
