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
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link OffsetTime} values.
 */
public final class OffsetTimeGenerator implements Generator<OffsetTime>, Seedable {

    private static final int MIN_OFFSET_SECONDS = -18 * 60 * 60;
    private static final int MAX_OFFSET_SECONDS =  18 * 60 * 60;

    private final Random random;

    public OffsetTimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public OffsetTimeGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public OffsetTimeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public OffsetTime generate() {
        LocalTime time = LocalTime.of(
            random.nextInt(24),
            random.nextInt(60),
            random.nextInt(60),
            random.nextInt(1_000_000_000));
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(random.nextInt(MIN_OFFSET_SECONDS, MAX_OFFSET_SECONDS + 1));
        return OffsetTime.of(time, offset);
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
