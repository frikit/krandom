/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link ZoneId} values.
 */
public final class ZoneIdGenerator implements Generator<ZoneId>, Seedable {

    private static final List<String> ZONE_IDS =
        List.copyOf(ZoneId.getAvailableZoneIds().stream().sorted().toList());

    private final Random random;

    public ZoneIdGenerator() {
        this(GeneratorConfig.defaults());
    }

    public ZoneIdGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    public ZoneIdGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public ZoneId generate() {
        return ZoneId.of(ZONE_IDS.get(random.nextInt(ZONE_IDS.size())));
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
