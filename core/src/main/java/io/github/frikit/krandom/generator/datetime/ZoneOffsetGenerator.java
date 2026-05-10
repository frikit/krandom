/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link ZoneOffset} values across the IANA-supported range.
 */
public final class ZoneOffsetGenerator implements Generator<ZoneOffset>, Seedable {

    private static final int MIN_SECONDS = -18 * 60 * 60;
    private static final int MAX_SECONDS =  18 * 60 * 60;

    private final Random random;

    public ZoneOffsetGenerator() {
        this(GeneratorConfig.defaults());
    }

    public ZoneOffsetGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public ZoneOffsetGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public ZoneOffset generate() {
        return ZoneOffset.ofTotalSeconds(random.nextInt(MIN_SECONDS, MAX_SECONDS + 1));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
