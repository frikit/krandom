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
 * Generates random {@link ZoneOffset} values from {@code -12:00} to {@code +14:00} in 15-minute steps.
 *
 * <p>Every offset kRandom draws follows this rule, which is a fixture convention covering the offsets
 * in actual use. It is not a standard: RFC 3339 excludes seconds from an offset but imposes neither
 * this range nor this grid, and JSON imposes nothing. Nor is it a list of observed offsets: it includes
 * steps that no zone uses. A value that draws a zone keeps the offset of that zone's rules instead, as
 * {@link ZonedDateTimeGenerator} does.
 */
public final class ZoneOffsetGenerator implements Generator<ZoneOffset>, Seedable {

    private static final int MIN_SECONDS = -12 * 60 * 60;
    private static final int MAX_SECONDS = 14 * 60 * 60;
    private static final int STEP_SECONDS = 15 * 60;
    private static final int STEPS = (MAX_SECONDS - MIN_SECONDS) / STEP_SECONDS + 1;

    private final Random random;

    public ZoneOffsetGenerator() {
        this(GeneratorConfig.defaults());
    }

    public ZoneOffsetGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public ZoneOffset generate() {
        return randomOffset(random);
    }

    /** Draws an offset under the rule on this class, for the generators in this package that pair one with a time. */
    static ZoneOffset randomOffset(Random random) {
        return ZoneOffset.ofTotalSeconds(MIN_SECONDS + random.nextInt(STEPS) * STEP_SECONDS);
    }

    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
