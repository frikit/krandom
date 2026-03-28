/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link Duration} values.
 */
public final class DurationGenerator implements Generator<Duration> {

    private static final long DEFAULT_MIN_SECONDS = 1;
    private static final long DEFAULT_MAX_SECONDS = 31_536_000L; // 365 days

    private final Random random;

    public DurationGenerator() {
        this(GeneratorConfig.defaults());
    }

    public DurationGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
    }

    @Override
    public Duration generate() {
        return betweenSeconds(DEFAULT_MIN_SECONDS, DEFAULT_MAX_SECONDS);
    }

    /**
     * Generates a duration with seconds sampled from an inclusive range.
     */
    public Duration betweenSeconds(long minSeconds, long maxSeconds) {
        if (minSeconds < 0) {
            throw new IllegalArgumentException("minSeconds must be >= 0, got: " + minSeconds);
        }
        if (maxSeconds < minSeconds) {
            throw new IllegalArgumentException("maxSeconds must be >= minSeconds, got: "
                                               + maxSeconds + " < " + minSeconds);
        }
        if (maxSeconds == minSeconds) {
            return Duration.ofSeconds(minSeconds);
        }
        long sample = minSeconds + random.nextLong(maxSeconds - minSeconds + 1);
        return Duration.ofSeconds(sample);
    }
}
