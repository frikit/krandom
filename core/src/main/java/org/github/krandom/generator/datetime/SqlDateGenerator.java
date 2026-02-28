/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link java.sql.Date} values.
 */
public final class SqlDateGenerator implements Generator<java.sql.Date> {

    private static final LocalDate DEFAULT_MIN = LocalDate.of(1970, 1, 1);
    private static final LocalDate DEFAULT_MAX = LocalDate.of(2100, 12, 31);

    private final Random random;
    private final LocalDate min;
    private final LocalDate max;

    public SqlDateGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SqlDateGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
        this.min = DEFAULT_MIN;
        this.max = DEFAULT_MAX;
    }

    public SqlDateGenerator(LocalDate min, LocalDate max) {
        this.random = new SecureRandom();
        this.min = Objects.requireNonNull(min, "min must not be null");
        this.max = Objects.requireNonNull(max, "max must not be null");
        if (this.min.isAfter(this.max)) {
            throw new IllegalArgumentException("min must be <= max");
        }
    }

    @Override
    public java.sql.Date generate() {
        long lo = min.toEpochDay();
        long hi = max.toEpochDay();
        return java.sql.Date.valueOf(LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1)));
    }
}
