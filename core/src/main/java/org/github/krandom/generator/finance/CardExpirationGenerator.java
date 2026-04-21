/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates credit card expiration dates with locale-aware formatting.
 *
 * <p>This generator produces realistic expiration dates for testing purposes, with options
 * to generate dates in the past, future, or any time period. Supports multiple date formats
 * based on locale conventions.
 *
 * <p><strong>Date Format by Locale:</strong>
 * <ul>
 *   <li><strong>en_US, en_AU</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>en_GB</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>de_DE</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>fr_FR</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>es_ES</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>it_IT</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>pt_BR</strong> → MM/YY (e.g., "03/26")</li>
 *   <li><strong>ja_JP</strong> → YY/MM (e.g., "26/03")</li>
 *   <li><strong>zh_CN</strong> → YY/MM (e.g., "26/03")</li>
 * </ul>
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * // Generate future expiration date (default: MM/YY format)
 * CardExpirationGenerator gen = new CardExpirationGenerator();
 * String expiry = gen.generate();              // "08/27"
 *
 * // Generate with specific date range
 * CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
 * CardExpirationGenerator pastGen = new CardExpirationGenerator(DateRange.PAST);
 * CardExpirationGenerator anyGen = new CardExpirationGenerator(DateRange.ANY);
 *
 * String futureExpiry = futureGen.generate();  // "11/29" (future)
 * String pastExpiry = pastGen.generate();      // "02/23" (past)
 * String anyExpiry = anyGen.generate();        // Could be past or future
 *
 * // Get just the month or year
 * String month = gen.getMonth();               // "07"
 * String year = gen.getYear();                 // "28"
 * String fullYear = gen.getYear(true);         // "2028"
 * }</pre>
 *
 * <p><strong>Locale-Aware Formatting:</strong>
 * <pre>{@code
 * // US format (MM/YY)
 * Locale usLocale = Locale.of("en", "US");
 * String usExpiry = gen.generate(usLocale);    // "03/26"
 *
 * // Japanese format (YY/MM)
 * Locale jpLocale = Locale.of("ja", "JP");
 * String jpExpiry = gen.generate(jpLocale);    // "26/03"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * CardExpirationGenerator gen1 = new CardExpirationGenerator(
 *     GeneratorConfig.builder().seed(12345L).build());
 * CardExpirationGenerator gen2 = new CardExpirationGenerator(
 *     GeneratorConfig.builder().seed(12345L).build());
 * gen1.generate().equals(gen2.generate());  // true (same sequence)
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 *
 * @see CreditCardGenerator
 * @see DateRange
 */
public final class CardExpirationGenerator implements Generator<String> {

    private static final DateTimeFormatter MM_YY_FORMATTER      = DateTimeFormatter.ofPattern("MM/yy");
    private static final DateTimeFormatter YY_MM_FORMATTER      = DateTimeFormatter.ofPattern("yy/MM");
    private static final DateTimeFormatter MONTH_FORMATTER      = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter YEAR_SHORT_FORMATTER = DateTimeFormatter.ofPattern("yy");
    private static final DateTimeFormatter YEAR_FULL_FORMATTER  = DateTimeFormatter.ofPattern("yyyy");

    private final GeneratorConfig config;
    private final Random          random;
    private final DateRange       dateRange;

    /**
     * Creates a generator that produces future-only expiration dates with default configuration.
     */
    public CardExpirationGenerator() {
        this(GeneratorConfig.defaults(), DateRange.FUTURE);
    }

    /**
     * Creates a generator with the specified date range using default configuration.
     *
     * @param dateRange the date range to use (PAST, FUTURE, or ANY); must not be {@code null}
     * @throws NullPointerException if {@code dateRange} is {@code null}
     */
    public CardExpirationGenerator(DateRange dateRange) {
        this(GeneratorConfig.defaults(), dateRange);
    }

    /**
     * Creates a generator using the given configuration with future-only dates.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CardExpirationGenerator(GeneratorConfig config) {
        this(config, DateRange.FUTURE);
    }

    /**
     * Creates a generator using the given configuration and date range.
     *
     * @param config    the generator configuration; must not be {@code null}
     * @param dateRange the date range to use (PAST, FUTURE, or ANY); must not be {@code null}
     * @throws NullPointerException if {@code config} or {@code dateRange} is {@code null}
     */
    public CardExpirationGenerator(GeneratorConfig config, DateRange dateRange) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.dateRange = Objects.requireNonNull(dateRange, "dateRange must not be null");
        this.random = config.createRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates an expiration date in MM/YY format (or YY/MM for Asian locales).
     * The date range depends on the configured DateRange option.
     *
     * @return an expiration date string in locale-appropriate format; never {@code null}
     */
    @Override
    public String generate() {
        return generate(config.getLocale(), dateRange);
    }

    /**
     * Generates an expiration date with the specified date range.
     *
     * @param dateRange the date range to use (PAST, FUTURE, or ANY)
     * @return an expiration date string in MM/YY format; never {@code null}
     */
    public String generate(DateRange dateRange) {
        return generate(config.getLocale(), dateRange);
    }

    /**
     * Generates an expiration date using the specified locale's format.
     *
     * @param locale the locale for formatting (null uses default MM/YY)
     * @return an expiration date string in locale-specific format; never {@code null}
     */
    public String generate(Locale locale) {
        return generate(locale, dateRange);
    }

    /**
     * Generates an expiration date using the specified locale's format and date range.
     *
     * @param locale    the locale for formatting (null uses default MM/YY)
     * @param dateRange the date range to use (PAST, FUTURE, or ANY)
     * @return an expiration date string in locale-specific format; never {@code null}
     */
    public String generate(Locale locale, DateRange dateRange) {
        YearMonth expiryDate = generateYearMonth(dateRange);

        // Determine format based on locale
        if (locale != null && isAsianLocale(locale)) {
            return expiryDate.format(YY_MM_FORMATTER); // YY/MM for Asian locales
        }
        return expiryDate.format(MM_YY_FORMATTER); // MM/YY for Western locales
    }

    /**
     * Generates an expiration month (01-12) as a zero-padded string.
     *
     * <p>The month is from a date that respects the configured date range.
     *
     * @return a month string (e.g., "01", "07", "12"); never {@code null}
     */
    public String getMonth() {
        return getMonth(dateRange);
    }

    /**
     * Generates an expiration month with the specified date range.
     *
     * @param dateRange the date range to use (PAST, FUTURE, or ANY)
     * @return a month string (e.g., "01", "07", "12"); never {@code null}
     */
    public String getMonth(DateRange dateRange) {
        YearMonth expiryDate = generateYearMonth(dateRange);
        return expiryDate.format(MONTH_FORMATTER);
    }

    /**
     * Generates an expiration year in 2-digit format (e.g., "26" for 2026).
     *
     * <p>The year is from a date that respects the configured date range.
     *
     * @return a 2-digit year string; never {@code null}
     */
    public String getYear() {
        return getYear(false, dateRange);
    }

    /**
     * Generates an expiration year with control over format.
     *
     * @param fullYear if true, returns 4-digit year (e.g., "2026"); if false, returns 2-digit (e.g., "26")
     * @return a year string; never {@code null}
     */
    public String getYear(boolean fullYear) {
        return getYear(fullYear, dateRange);
    }

    /**
     * Generates an expiration year with control over format and date range.
     *
     * @param fullYear  if true, returns 4-digit year; if false, returns 2-digit
     * @param dateRange the date range to use (PAST, FUTURE, or ANY)
     * @return a year string; never {@code null}
     */
    public String getYear(boolean fullYear, DateRange dateRange) {
        YearMonth expiryDate = generateYearMonth(dateRange);
        return expiryDate.format(fullYear ? YEAR_FULL_FORMATTER : YEAR_SHORT_FORMATTER);
    }

    /**
     * Returns the configured date range for this generator.
     *
     * @return the date range (PAST, FUTURE, or ANY)
     */
    public DateRange getDateRange() {
        return dateRange;
    }

    /**
     * Generates a YearMonth based on the specified date range.
     *
     * @param dateRange the date range to use (PAST, FUTURE, or ANY)
     * @return a YearMonth instance
     */
    private YearMonth generateYearMonth(DateRange dateRange) {
        YearMonth now = YearMonth.now();

        return switch (dateRange) {
            case PAST -> {
                // 1-60 months in the past
                int monthsToSubtract = 1 + random.nextInt(60);
                yield now.minusMonths(monthsToSubtract);
            }
            case FUTURE -> {
                // 1-60 months in the future
                int monthsToAdd = 1 + random.nextInt(60);
                yield now.plusMonths(monthsToAdd);
            }
            case ANY -> {
                // Up to 60 months in the past or future
                int monthsOffset = random.nextInt(121) - 60; // -60 to +60
                yield now.plusMonths(monthsOffset);
            }
        };
    }

    /**
     * Checks if the locale uses Asian date formatting (YY/MM instead of MM/YY).
     *
     * @param locale the locale to check
     * @return true if the locale is Japanese or Chinese
     */
    private boolean isAsianLocale(Locale locale) {
        String country = locale.getCountry();
        return "JP".equals(country) || "CN".equals(country);
    }
}
