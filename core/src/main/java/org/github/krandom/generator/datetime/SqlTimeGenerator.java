/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.sql.Time;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link Time} values.
 */
public final class SqlTimeGenerator implements Generator<Time> {

    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    private final Random random;

    public SqlTimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SqlTimeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public Time generate() {
        return new Time(random.nextLong(MILLIS_PER_DAY));
    }
}
