/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

/**
 * Generates random {@link java.sql.Date} values.
 *
 * <p>A {@code java.sql.Date} holds a day as the start of that day in the JVM's default time zone, in
 * the Julian calendar before 15 October 1582, so it cannot hold every {@link LocalDate}: no day before
 * the year 1 or after the year 292278994, none of 5 to 14 October 1582, which the change of calendar
 * skipped, and no day that the default time zone skipped, such as 30 December 2011 in
 * {@code Pacific/Apia}. Such a day reads back, through {@link java.sql.Date#toLocalDate()}, as a later
 * one. A range's bounds must be days that it holds, and every value reads back as a day in the range.
 */
public final class SqlDateGenerator implements Generator<java.sql.Date>, Seedable {

    private static final LocalDate DEFAULT_MIN = LocalDate.of(1970, 1, 1);
    private static final LocalDate DEFAULT_MAX = LocalDate.of(2100, 12, 31);

    private final Random random;
    private final LocalDate min;
    private final LocalDate max;

    public SqlDateGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SqlDateGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.min = DEFAULT_MIN;
        this.max = DEFAULT_MAX;
    }

    /**
     * Creates a generator of the days from {@code min} to {@code max}, both inclusive.
     *
     * @param min earliest day; a day that a {@code java.sql.Date} holds in the default time zone
     * @param max latest day; a day that a {@code java.sql.Date} holds in the default time zone
     * @throws IllegalArgumentException if {@code min} is after {@code max}, or a {@code java.sql.Date}
     *                                  cannot hold either in the default time zone
     */
    public SqlDateGenerator(LocalDate min, LocalDate max) {
        this.random = new Random();
        this.min = Objects.requireNonNull(min, "min must not be null");
        this.max = Objects.requireNonNull(max, "max must not be null");
        if (this.min.isAfter(this.max)) {
            throw new IllegalArgumentException("min must be <= max");
        }
        requireHeld("min", this.min);
        requireHeld("max", this.max);
    }

    private static void requireHeld(String name, LocalDate day) {
        LocalDate readBack = java.sql.Date.valueOf(day).toLocalDate();
        if (!readBack.equals(day)) {
            throw new IllegalArgumentException(name + " " + day
                    + " is not a day that a java.sql.Date holds in the default time zone "
                    + TimeZone.getDefault().getID() + ", which reads it back as " + readBack);
        }
    }

    @Override
    public java.sql.Date generate() {
        long lo = min.toEpochDay();
        long hi = max.toEpochDay();
        java.sql.Date date = java.sql.Date.valueOf(LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1)));
        // A day that the default time zone cannot hold reads back as a later one, which stays while in the range.
        return date.toLocalDate().isAfter(max) ? java.sql.Date.valueOf(max) : date;
    }

    /**
     * Reseeds this generator's owned random source for deterministic replay.
     */
    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
