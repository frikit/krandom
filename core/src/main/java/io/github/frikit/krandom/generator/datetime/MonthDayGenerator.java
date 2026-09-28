/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.time.Month;
import java.time.MonthDay;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link MonthDay} values.
 */
public final class MonthDayGenerator implements Generator<MonthDay>, Seedable {

    private final Random random;

    public MonthDayGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a seeded month-day generator.
     *
     * @deprecated raw seeds bypass replayable recipes; use
     *             {@link #MonthDayGenerator(GeneratorConfig)} with
     *             {@code GeneratorConfig.builder().seed(seed).build()}, which produces the same
     *             values.
     */
    @Deprecated(since = "2.6.0")
    public MonthDayGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public MonthDayGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public MonthDay generate() {
        Month month = Month.of(random.nextInt(1, 13));
        return MonthDay.of(month, random.nextInt(1, month.maxLength() + 1));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
