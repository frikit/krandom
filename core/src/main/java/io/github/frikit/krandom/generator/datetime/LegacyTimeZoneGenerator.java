/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

/**
 * Generates random legacy {@link TimeZone} values.
 */
public final class LegacyTimeZoneGenerator implements Generator<TimeZone>, Seedable {

    private static final List<String> ZONE_IDS =
        List.copyOf(Arrays.stream(TimeZone.getAvailableIDs()).sorted().toList());

    private final Random random;

    public LegacyTimeZoneGenerator() {
        this(GeneratorConfig.defaults());
    }

    public LegacyTimeZoneGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public LegacyTimeZoneGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public TimeZone generate() {
        return TimeZone.getTimeZone(ZONE_IDS.get(random.nextInt(ZONE_IDS.size())));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
