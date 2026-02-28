/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link LocalDateTime} values.
 *
 * <p>The generated values fall within the range [1970-01-01T00:00:00, 2100-12-31T23:59:59].
 * Days are calendar-correct (leap years are handled automatically).
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * LocalDateTimeGenerator gen = new LocalDateTimeGenerator();
 * LocalDateTime dt = gen.generate();  // e.g. 2047-08-19T14:32:07
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * LocalDateTimeGenerator gen = new LocalDateTimeGenerator(config);
 * LocalDateTime dt = gen.generate();  // reproducible
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 *
 * @see DateGenerator
 * @see TimeGenerator
 */
public final class LocalDateTimeGenerator implements Generator<LocalDateTime> {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2100;
    private static final LocalDateTime MIN_DATE_TIME = LocalDateTime.of(MIN_YEAR, 1, 1, 0, 0, 0);
    private static final LocalDateTime MAX_DATE_TIME = LocalDateTime.of(MAX_YEAR, 12, 31, 23, 59, 59);

    private final Random    random;
    private final LocalDate rangeMin;
    private final LocalDate rangeMax;

    /**
     * Creates a local-date-time generator with default configuration.
     */
    public LocalDateTimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a local-date-time generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public LocalDateTimeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random   = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
        this.rangeMin = null;
        this.rangeMax = null;
    }

    /**
     * Creates a local-date-time generator restricted to the given date range.
     * Intended for use by {@code FieldGeneratorResolver} when a date range is configured.
     *
     * @param min earliest date (inclusive)
     * @param max latest date (inclusive)
     */
    public LocalDateTimeGenerator(LocalDate min, LocalDate max) {
        this.random   = new SecureRandom();
        this.rangeMin = min;
        this.rangeMax = max;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random date-time between 1970-01-01T00:00:00 and 2100-12-31T23:59:59.
     *
     * @return a random local date-time; never {@code null}
     */
    @Override
    public LocalDateTime generate() {
        LocalDate date;
        if (rangeMin != null) {
            long lo = rangeMin.toEpochDay();
            long hi = rangeMax.toEpochDay();
            date = LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1));
        } else {
            int year   = MIN_YEAR + random.nextInt(MAX_YEAR - MIN_YEAR + 1);
            int month  = 1 + random.nextInt(12);
            int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
            int day    = 1 + random.nextInt(maxDay);
            date = LocalDate.of(year, month, day);
        }
        int hour   = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        return LocalDateTime.of(date, LocalTime.of(hour, minute, second));
    }

    /**
     * Generates a date-time strictly before the provided reference date-time.
     *
     * @param reference upper bound (exclusive); must not be {@code null}
     * @return generated date-time strictly before {@code reference}
     */
    public LocalDateTime before(LocalDateTime reference) {
        Objects.requireNonNull(reference, "reference must not be null");
        LocalDateTime upper = reference.minusSeconds(1);
        if (upper.isBefore(MIN_DATE_TIME)) {
            throw new IllegalArgumentException("reference must be after " + MIN_DATE_TIME + " for before()");
        }
        return between(MIN_DATE_TIME, upper);
    }

    /**
     * Generates a date-time strictly after the provided reference date-time.
     *
     * @param reference lower bound (exclusive); must not be {@code null}
     * @return generated date-time strictly after {@code reference}
     */
    public LocalDateTime after(LocalDateTime reference) {
        Objects.requireNonNull(reference, "reference must not be null");
        LocalDateTime lower = reference.plusSeconds(1);
        if (lower.isAfter(MAX_DATE_TIME)) {
            throw new IllegalArgumentException("reference must be before " + MAX_DATE_TIME + " for after()");
        }
        return between(lower, MAX_DATE_TIME);
    }

    /**
     * Generates a date-time between the provided bounds (inclusive).
     *
     * @param startInclusive lower bound (inclusive); must not be {@code null}
     * @param endInclusive upper bound (inclusive); must not be {@code null}
     * @return generated date-time in range
     */
    public LocalDateTime between(LocalDateTime startInclusive, LocalDateTime endInclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endInclusive, "endInclusive must not be null");
        if (startInclusive.isAfter(endInclusive)) {
            throw new IllegalArgumentException("startInclusive must be <= endInclusive, got: "
                    + startInclusive + " > " + endInclusive);
        }
        long seconds = ChronoUnit.SECONDS.between(startInclusive, endInclusive);
        if (seconds == 0) {
            return startInclusive;
        }
        long offset = random.nextLong(seconds + 1);
        return startInclusive.plusSeconds(offset);
    }
}
