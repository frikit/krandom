/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link YearMonth} values.
 */
public final class YearMonthGenerator implements Generator<YearMonth>, Seedable {

    private static final int DEFAULT_MIN_YEAR = 1970;
    private static final int DEFAULT_MAX_YEAR = 2100;

    private final Random random;
    private final int    minYear;
    private final int    maxYear;

    public YearMonthGenerator() {
        this(DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR, GeneratorConfig.defaults());
    }

    public YearMonthGenerator(GeneratorConfig config) {
        this(DEFAULT_MIN_YEAR, DEFAULT_MAX_YEAR, config);
    }

    public YearMonthGenerator(int minYear, int maxYear) {
        this(minYear, maxYear, GeneratorConfig.defaults());
    }

    /**
     * Creates a seeded year-month generator.
     *
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #YearMonthGenerator(int, int, GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
    public YearMonthGenerator(int minYear, int maxYear, long seed) {
        this(minYear, maxYear, GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a year-month generator over years {@code [minYear, maxYear]} using the
     * configuration's random source.
     *
     * @param minYear lowest year (inclusive)
     * @param maxYear highest year (inclusive)
     * @param config  generator configuration; must not be {@code null}
     * @throws IllegalArgumentException if {@code minYear > maxYear}
     */
    public YearMonthGenerator(int minYear, int maxYear, GeneratorConfig config) {
        if (minYear > maxYear) {
            throw new IllegalArgumentException("minYear must be <= maxYear");
        }
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
    }

    @Override
    public YearMonth generate() {
        return YearMonth.of(random.nextInt(minYear, maxYear + 1), random.nextInt(1, 13));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
