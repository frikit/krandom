/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link Time} values, to the second. A {@code Time} is a time of day, so a seeded
 * value reads the same whichever of the JDK's time zones is the JVM's default.
 */
public final class SqlTimeGenerator implements Generator<Time> {

    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

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
        return Time.valueOf(LocalTime.ofSecondOfDay(random.nextInt(SECONDS_PER_DAY)));
    }
}
