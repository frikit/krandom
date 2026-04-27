/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link OffsetDateTime} values.
 *
 * <p>Pairs a random {@link java.time.LocalDateTime} drawn from
 * {@link LocalDateTimeGenerator} with a randomly selected {@link ZoneOffset} sampled at
 * 15-minute increments across the full IANA-allowed range of {@code -18:00} to {@code +18:00}.
 */
public final class OffsetDateTimeGenerator implements Generator<OffsetDateTime> {

    private static final int OFFSET_STEP_MINUTES = 15;
    private static final int MIN_OFFSET_MINUTES  = -18 * 60;
    private static final int MAX_OFFSET_MINUTES  =  18 * 60;
    private static final int OFFSET_STEPS        = (MAX_OFFSET_MINUTES - MIN_OFFSET_MINUTES) / OFFSET_STEP_MINUTES + 1;

    private final Random                 random;
    private final LocalDateTimeGenerator dateTimeGenerator;

    /**
     * Creates an offset-date-time generator with default configuration.
     */
    public OffsetDateTimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an offset-date-time generator with the specified configuration.
     */
    public OffsetDateTimeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.dateTimeGenerator = new LocalDateTimeGenerator(config);
    }

    @Override
    public OffsetDateTime generate() {
        return OffsetDateTime.of(dateTimeGenerator.generate(), randomOffset());
    }

    /**
     * Generates an offset-date-time within the inclusive range, with a randomly assigned offset.
     *
     * <p>The result is at the same {@link java.time.Instant} as a uniformly sampled point
     * between the bounds, then re-expressed at a randomly chosen {@link ZoneOffset}.
     */
    public OffsetDateTime between(OffsetDateTime startInclusive, OffsetDateTime endInclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endInclusive, "endInclusive must not be null");
        if (startInclusive.isAfter(endInclusive)) {
            throw new IllegalArgumentException("startInclusive must be <= endInclusive, got: "
                                               + startInclusive + " > " + endInclusive);
        }
        long seconds = ChronoUnit.SECONDS.between(startInclusive, endInclusive);
        long offset = seconds == 0 ? 0 : random.nextLong(seconds + 1);
        return startInclusive.plusSeconds(offset).withOffsetSameInstant(randomOffset());
    }

    private ZoneOffset randomOffset() {
        int totalMinutes = MIN_OFFSET_MINUTES + random.nextInt(OFFSET_STEPS) * OFFSET_STEP_MINUTES;
        return ZoneOffset.ofTotalSeconds(totalMinutes * 60);
    }
}
