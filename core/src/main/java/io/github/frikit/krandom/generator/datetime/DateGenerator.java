/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random dates and date components.
 *
 * <p>This generator produces dates in various formats and provides methods
 * to generate individual date components like year, month, day, etc.
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * DateGenerator gen = new DateGenerator();
 * LocalDate date = gen.generate();  // Random date
 * String dateStr = gen.generateString();  // "2078-05-27"
 * }</pre>
 *
 * <p><strong>Date Formatting:</strong>
 * <pre>{@code
 * DateGenerator gen = new DateGenerator();
 * String american = gen.generateAmerican();  // "05/27/2078" (MM/DD/YYYY)
 * String european = gen.generateEuropean();  // "27/05/2078" (DD/MM/YYYY)
 * }</pre>
 *
 * <p><strong>Date Components:</strong>
 * <pre>{@code
 * DateGenerator gen = new DateGenerator();
 * int year = gen.generateYear();        // Random year
 * int month = gen.generateMonth();      // 1-12
 * String monthName = gen.generateMonthName();  // "October"
 * }</pre>
 *
 * <p><strong>Timestamps:</strong>
 * <pre>{@code
 * DateGenerator gen = new DateGenerator();
 * long timestamp = gen.generateTimestamp();  // Unix timestamp in seconds
 * }</pre>
 *
 * <p><strong>Constrained Dates:</strong>
 * <pre>{@code
 * DateGenerator gen = new DateGenerator();
 * LocalDate date2020 = gen.generateWithYear(2020);  // Date in year 2020
 * LocalDate dateInMay = gen.generateWithMonth(5);   // Date in May
 * LocalDate date15th = gen.generateWithDay(15);     // Date on the 15th
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * DateGenerator gen = new DateGenerator(config);
 * LocalDate date = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 *
 * @see TimeGenerator for time component generation
 */
public final class DateGenerator implements Generator<LocalDate> {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2100;
    private final GeneratorConfig config;
    private final Random          random;
    private final Clock           clock;
    /**
     * Non-null only when constructed via the bounded constructor.
     */
    private final LocalDate rangeMin;
    private final LocalDate rangeMax;

    /**
     * Creates a date generator with default configuration.
     */
    public DateGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a date generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public DateGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.clock = config.getClock();
        this.rangeMin = null;
        this.rangeMax = null;
    }

    /**
     * Creates a date generator restricted to the given date range.
     * Intended for use by {@code FieldGeneratorResolver} when a date range is configured.
     *
     * @param min earliest date (inclusive)
     * @param max latest date (inclusive)
     */
    public DateGenerator(LocalDate min, LocalDate max) {
        this.config = GeneratorConfig.defaults();
        this.random = new SecureRandom();
        this.clock = this.config.getClock();
        this.rangeMin = min;
        this.rangeMax = max;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random date between 1970 and 2100.
     *
     * @return a random date; never {@code null}
     */
    @Override
    public LocalDate generate() {
        if (rangeMin != null) {
            long lo = rangeMin.toEpochDay();
            long hi = rangeMax.toEpochDay();
            return LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1));
        }
        int year = generateYear();
        int month = generateMonth();
        int day = generateValidDay(year, month);
        return LocalDate.of(year, month, day);
    }

    /**
     * Generates a random date with the specified year.
     *
     * @param year the year (1-9999)
     * @return a random date in the specified year; never {@code null}
     */
    public LocalDate generateWithYear(int year) {
        int month = generateMonth();
        int day = generateValidDay(year, month);
        return LocalDate.of(year, month, day);
    }

    /**
     * Generates a random date with the specified month.
     *
     * @param month the month (1-12)
     * @return a random date in the specified month; never {@code null}
     */
    public LocalDate generateWithMonth(int month) {
        int year = generateYear();
        int day = generateValidDay(year, month);
        return LocalDate.of(year, month, day);
    }

    /**
     * Generates a random date with the specified day of month.
     *
     * @param day the day (1-28, safe for all months)
     * @return a random date on the specified day; never {@code null}
     */
    public LocalDate generateWithDay(int day) {
        int year = generateYear();
        int month = generateMonth();
        // Ensure day is valid for the month
        int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
        int actualDay = Math.min(day, maxDay);
        return LocalDate.of(year, month, actualDay);
    }

    /**
     * Generates a date as a string in ISO format (YYYY-MM-DD).
     *
     * @return a date string; never {@code null}
     */
    public String generateString() {
        return generate().toString();
    }

    /**
     * Generates a date in American format (MM/DD/YYYY).
     *
     * @return a date string in American format; never {@code null}
     */
    public String generateAmerican() {
        LocalDate date = generate();
        return String.format("%02d/%02d/%04d",
                             date.getMonthValue(), date.getDayOfMonth(), date.getYear());
    }

    /**
     * Generates a date in European format (DD/MM/YYYY).
     *
     * @return a date string in European format; never {@code null}
     */
    public String generateEuropean() {
        LocalDate date = generate();
        return String.format("%02d/%02d/%04d",
                             date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    /**
     * Generates a random year between 1970 and 2100.
     *
     * @return a year
     */
    public int generateYear() {
        return MIN_YEAR + random.nextInt(MAX_YEAR - MIN_YEAR + 1);
    }

    /**
     * Generates a random year within the specified range.
     *
     * @param min minimum year (inclusive)
     * @param max maximum year (inclusive)
     * @return a year
     */
    public int generateYear(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Generates a random month (1-12).
     *
     * @return a month
     */
    public int generateMonth() {
        return 1 + random.nextInt(12);
    }

    /**
     * Generates a random month name.
     *
     * @return a month name; never {@code null}
     */
    public String generateMonthName() {
        return generateMonthName(config.getLocale());
    }

    /**
     * Generates a random month name in the provided locale.
     *
     * @param locale locale for month display names; must not be {@code null}
     * @return a localized month name; never {@code null}
     */
    public String generateMonthName(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");
        return Month.of(generateMonth()).getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * Generates a random Unix timestamp in seconds.
     * <p>Range: January 1, 1970 to December 31, 2100
     *
     * @return a Unix timestamp
     */
    public long generateTimestamp() {
        LocalDate date = generate();
        // Use midnight for the timestamp
        LocalDateTime dateTime = date.atStartOfDay();
        return dateTime.atZone(clock.getZone()).toEpochSecond();
    }

    /**
     * Alias for {@link #generateTimestamp()} to match Faker-style unix-time naming.
     *
     * @return Unix timestamp in seconds
     */
    public long generateUnixTime() {
        return generateTimestamp();
    }

    /**
     * Generates a date-time value at start-of-day for a generated date.
     *
     * @return local date-time
     */
    public LocalDateTime generateDateTime() {
        return generate().atStartOfDay();
    }

    /**
     * Generates a date-time string in ISO local-date-time format.
     *
     * @return date-time string
     */
    public String generateDateTimeString() {
        return generateDateTime().toString();
    }

    /**
     * Generates a future date in the range [tomorrow, tomorrow + 10 years].
     *
     * @return a future date; never {@code null}
     */
    public LocalDate future() {
        return future(3650);
    }

    /**
     * Generates a future date in the range [tomorrow, tomorrow + maxDaysAhead].
     *
     * @param maxDaysAhead maximum days ahead; must be {@code > 0}
     * @return a future date; never {@code null}
     * @throws IllegalArgumentException if {@code maxDaysAhead <= 0}
     */
    public LocalDate future(int maxDaysAhead) {
        if (maxDaysAhead <= 0) {
            throw new IllegalArgumentException("maxDaysAhead must be > 0, got: " + maxDaysAhead);
        }
        LocalDate start = LocalDate.now(clock).plusDays(1);
        LocalDate end = start.plusDays(maxDaysAhead);
        return between(start, end);
    }

    /**
     * Generates a past date in the range [today - 10 years, yesterday].
     *
     * @return a past date; never {@code null}
     */
    public LocalDate past() {
        return past(3650);
    }

    /**
     * Generates a past date in the range [today - maxDaysBack, yesterday].
     *
     * @param maxDaysBack maximum days back; must be {@code > 0}
     * @return a past date; never {@code null}
     * @throws IllegalArgumentException if {@code maxDaysBack <= 0}
     */
    public LocalDate past(int maxDaysBack) {
        if (maxDaysBack <= 0) {
            throw new IllegalArgumentException("maxDaysBack must be > 0, got: " + maxDaysBack);
        }
        LocalDate end = LocalDate.now(clock).minusDays(1);
        LocalDate start = end.minusDays(maxDaysBack);
        return between(start, end);
    }

    /**
     * Generates a date between the given bounds (inclusive).
     *
     * @param fromInclusive lower bound (inclusive)
     * @param toInclusive   upper bound (inclusive)
     * @return a date in the provided range; never {@code null}
     * @throws NullPointerException     if either bound is {@code null}
     * @throws IllegalArgumentException if {@code fromInclusive} is after {@code toInclusive}
     */
    public LocalDate between(LocalDate fromInclusive, LocalDate toInclusive) {
        Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        Objects.requireNonNull(toInclusive, "toInclusive must not be null");
        if (fromInclusive.isAfter(toInclusive)) {
            throw new IllegalArgumentException(
                "fromInclusive must be <= toInclusive, got: " + fromInclusive + " > " + toInclusive);
        }

        long days = ChronoUnit.DAYS.between(fromInclusive, toInclusive);
        if (days == 0) {
            return fromInclusive;
        }
        long offset = random.nextLong(days + 1);
        return fromInclusive.plusDays(offset);
    }

    /**
     * Generates a valid day for the given year and month.
     *
     * @param year  the year
     * @param month the month
     * @return a valid day
     */
    private int generateValidDay(int year, int month) {
        int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
        return 1 + random.nextInt(maxDay);
    }
}
