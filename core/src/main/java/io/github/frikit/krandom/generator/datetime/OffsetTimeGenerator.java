/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link OffsetTime} values, whose offset follows the rule on {@link ZoneOffsetGenerator}.
 */
public final class OffsetTimeGenerator implements Generator<OffsetTime>, Seedable {

    private final Random random;

    public OffsetTimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public OffsetTimeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public OffsetTime generate() {
        LocalTime time =
                LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60), random.nextInt(1_000_000_000));
        return OffsetTime.of(time, ZoneOffsetGenerator.randomOffset(random));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
