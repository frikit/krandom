/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware formatted monetary amounts.
 *
 * <p>The default amount range is {@code [0, 10 000)}. All amounts use
 * {@link java.text.NumberFormat#getCurrencyInstance(Locale)} so that thousand
 * separators, decimal separators, symbol placement, and decimal-place count follow the
 * conventions of the configured locale.
 *
 * <p><strong>Locale formatting examples:</strong>
 * <ul>
 *   <li>{@code en_US} → {@code "$1,234.56"}</li>
 *   <li>{@code de_DE} → {@code "1.234,56 €"}</li>
 *   <li>{@code fr_FR} → {@code "1 234,56 €"}</li>
 *   <li>{@code ja_JP} → {@code "¥1,235"} (JPY has no decimal places)</li>
 *   <li>{@code pt_BR} → {@code "R$1.234,56"}</li>
 *   <li>{@code zh_CN} → {@code "¥1,234.56"}</li>
 * </ul>
 *
 * <p><strong>Basic usage:</strong>
 * <pre>{@code
 * MoneyGenerator gen = new MoneyGenerator();
 * String amount   = gen.generate();              // "$4,231.87" (en_US default)
 * String capped   = gen.generate(100.0);         // "$37.42"
 *
 * String dollar   = gen.generateDollar();        // "$4,231.87"
 * String euro     = gen.generateEuro();          // "4 231,87 €"
 *
 * // Locale-aware
 * MoneyGenerator de = new MoneyGenerator(Locale.GERMANY);
 * String german   = de.generate();              // "4.231,87 €"
 * String explicit = gen.generate(Locale.JAPAN); // "¥4,232"
 * }</pre>
 *
 * <p>Unrecognized locales fall back to US Dollar formatting.
 *
 * @see CurrencyGenerator
 * @see CurrencyPairGenerator
 */
public final class MoneyGenerator implements Generator<String> {

    /** Default maximum amount (exclusive). Matches Chance.js {@code dollar()} default. */
    public static final double DEFAULT_MAX = 10_000.00;

    private final Locale locale;
    private final Random random;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Creates a generator for {@link Locale#US} (USD) using a secure random source. */
    public MoneyGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given configuration.
     *
     * @param config generator configuration (locale + optional seed); must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public MoneyGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * Creates a generator for the given locale using a secure random source.
     *
     * @param locale the locale for currency formatting; must not be {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    public MoneyGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                .locale(Objects.requireNonNull(locale, "locale must not be null"))
                .build());
    }

    // ── Locale-aware generate() ───────────────────────────────────────────────

    /**
     * Generates a formatted monetary amount in {@code [0, 10 000)} using the configured locale.
     *
     * @return locale-formatted amount string (e.g., {@code "$4,231.87"} for en_US)
     */
    @Override
    public String generate() {
        return format(locale, nextAmount(DEFAULT_MAX));
    }

    /**
     * Generates a formatted monetary amount in {@code [0, max)} using the configured locale.
     *
     * @param max upper bound (exclusive); must be ≥ 0
     * @return locale-formatted amount string
     * @throws IllegalArgumentException if {@code max < 0}
     */
    public String generate(double max) {
        validateMax(max);
        return format(locale, nextAmount(max));
    }

    /**
     * Generates a formatted monetary amount for the given locale in {@code [0, 10 000)}.
     *
     * <p>Overrides the instance locale for this single call. Unrecognized locales fall back to USD.
     *
     * @param overrideLocale locale for this call; must not be {@code null}
     * @return locale-formatted amount string
     * @throws NullPointerException if {@code overrideLocale} is {@code null}
     */
    public String generate(Locale overrideLocale) {
        Objects.requireNonNull(overrideLocale, "locale must not be null");
        return format(overrideLocale, nextAmount(DEFAULT_MAX));
    }

    /**
     * Generates a formatted monetary amount for the given locale in {@code [0, max)}.
     *
     * @param overrideLocale locale for this call; must not be {@code null}
     * @param max            upper bound (exclusive); must be ≥ 0
     * @return locale-formatted amount string
     * @throws NullPointerException     if {@code overrideLocale} is {@code null}
     * @throws IllegalArgumentException if {@code max < 0}
     */
    public String generate(Locale overrideLocale, double max) {
        Objects.requireNonNull(overrideLocale, "locale must not be null");
        validateMax(max);
        return format(overrideLocale, nextAmount(max));
    }

    // ── Dollar ────────────────────────────────────────────────────────────────

    /**
     * Generates a US-dollar amount in {@code [0, 10 000)}, formatted as {@code "$1,234.56"}.
     *
     * <p>Always uses US formatting regardless of the instance locale.
     *
     * @return dollar-formatted amount string
     */
    public String generateDollar() {
        return formatDollar(nextAmount(DEFAULT_MAX));
    }

    /**
     * Generates a US-dollar amount in {@code [0, max)}, formatted as {@code "$1,234.56"}.
     *
     * @param max upper bound (exclusive); must be ≥ 0
     * @return dollar-formatted amount string
     * @throws IllegalArgumentException if {@code max < 0}
     */
    public String generateDollar(double max) {
        validateMax(max);
        return formatDollar(nextAmount(max));
    }

    // ── Euro ──────────────────────────────────────────────────────────────────

    /**
     * Generates a Euro amount in {@code [0, 10 000)}, formatted per French locale conventions
     * (e.g., {@code "1 234,56 €"}).
     *
     * <p>Always produces Euro formatting regardless of the instance locale.
     *
     * @return euro-formatted amount string
     */
    public String generateEuro() {
        return formatEuro(nextAmount(DEFAULT_MAX));
    }

    /**
     * Generates a Euro amount in {@code [0, max)}.
     *
     * @param max upper bound (exclusive); must be ≥ 0
     * @return euro-formatted amount string
     * @throws IllegalArgumentException if {@code max < 0}
     */
    public String generateEuro(double max) {
        validateMax(max);
        return formatEuro(nextAmount(max));
    }

    // ── Accessor ──────────────────────────────────────────────────────────────

    /**
     * Returns the locale this generator was configured with.
     *
     * @return the configured locale
     */
    public Locale getLocale() {
        return locale;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private double nextAmount(double max) {
        return random.nextDouble() * max;
    }

    private static void validateMax(double max) {
        if (max < 0) {
            throw new IllegalArgumentException("max must be >= 0, got: " + max);
        }
    }

    /**
     * Formats {@code amount} using the currency of {@code loc}.
     * Falls back to USD / US formatting for unrecognised locales.
     */
    private static String format(Locale loc, double amount) {
        Currency currency = Currency.forLocale(loc);
        if (currency == null) {
            return formatDollar(amount);
        }
        NumberFormat fmt = NumberFormat.getCurrencyInstance(loc);
        fmt.setCurrency(java.util.Currency.getInstance(currency.getCode()));
        return fmt.format(amount);
    }

    private static String formatDollar(double amount) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
        fmt.setCurrency(java.util.Currency.getInstance("USD"));
        return fmt.format(amount);
    }

    private static String formatEuro(double amount) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        fmt.setCurrency(java.util.Currency.getInstance("EUR"));
        return fmt.format(amount);
    }
}
