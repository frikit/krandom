/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

/**
 * Generates random {@link java.sql.Date} values.
 *
 * <p>A {@code java.sql.Date} holds a day as the start of that day in the JVM's default time zone, in
 * the Julian calendar before 15 October 1582, so it cannot hold every {@link LocalDate}: no day before
 * the year 1 or after about the year 292278994, none of 5 to 14 October 1582, which the change of
 * calendar skipped, and no day that the default time zone skipped, such as 30 December 2011 in
 * {@code Pacific/Apia}. Such a day reads back, through {@link java.sql.Date#toLocalDate()}, as another
 * one. Every value reads back as a day in the range: a drawn day that would read back outside it gives
 * the last day of the range that a {@code java.sql.Date} holds.
 */
public final class SqlDateGenerator implements Generator<java.sql.Date>, Seedable {

    private static final LocalDate DEFAULT_MIN = LocalDate.of(1970, 1, 1);
    private static final LocalDate DEFAULT_MAX = LocalDate.of(2100, 12, 31);
    /** The first day that a {@code java.sql.Date} holds in any time zone: it cannot hold a year before 1. */
    private static final LocalDate FIRST_HELD = LocalDate.of(1, 1, 1);

    private final Random random;
    private final LocalDate min;
    private final LocalDate max;
    private final LocalDate lastHeld;

    public SqlDateGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SqlDateGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.min = DEFAULT_MIN;
        this.max = DEFAULT_MAX;
        this.lastHeld = lastHeldDay(DEFAULT_MIN, DEFAULT_MAX);
    }

    /**
     * Creates a generator of the days from {@code min} to {@code max}, both inclusive.
     *
     * @param min earliest day
     * @param max latest day
     * @throws IllegalArgumentException if {@code min} is after {@code max}, or no day from {@code min} to
     *                                  {@code max} is one that a {@code java.sql.Date} holds in the
     *                                  default time zone, such as a range in the year 0
     */
    public SqlDateGenerator(LocalDate min, LocalDate max) {
        this.random = new Random();
        this.min = Objects.requireNonNull(min, "min must not be null");
        this.max = Objects.requireNonNull(max, "max must not be null");
        if (this.min.isAfter(this.max)) {
            throw new IllegalArgumentException("min must be <= max");
        }
        this.lastHeld = lastHeldDay(this.min, this.max);
    }

    /** The last day from {@code min} to {@code max} that a {@code java.sql.Date} holds in the default time zone. */
    private static LocalDate lastHeldDay(LocalDate min, LocalDate max) {
        // The day of the latest instant a java.sql.Date holds bounds the search, as the year 1 does below.
        LocalDate latest = new java.sql.Date(Long.MAX_VALUE).toLocalDate();
        for (LocalDate day = max.isAfter(latest) ? latest : max;
                !day.isBefore(min) && !day.isBefore(FIRST_HELD);
                day = day.minusDays(1)) {
            if (java.sql.Date.valueOf(day).toLocalDate().equals(day)) {
                return day;
            }
        }
        throw new IllegalArgumentException("no day from " + min + " to " + max
                + " is one that a java.sql.Date holds in the default time zone "
                + TimeZone.getDefault().getID());
    }

    @Override
    public java.sql.Date generate() {
        long lo = min.toEpochDay();
        long hi = max.toEpochDay();
        java.sql.Date date = java.sql.Date.valueOf(LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1)));
        // A day that the default time zone cannot hold reads back as another one, which stays while in the range.
        return readsBackWithin(date) ? date : java.sql.Date.valueOf(lastHeld);
    }

    private boolean readsBackWithin(java.sql.Date date) {
        try {
            LocalDate readBack = date.toLocalDate();
            return !readBack.isBefore(min) && !readBack.isAfter(max);
        } catch (DateTimeException overflowed) {
            // Past the instants that a java.sql.Date holds, a day can read back as no date at all.
            return false;
        }
    }

    /**
     * Reseeds this generator's owned random source for deterministic replay.
     */
    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
