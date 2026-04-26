/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random foreign exchange (FX) currency pairs.
 *
 * <p>A currency pair (e.g., {@code "EUR/USD"}) consists of a base currency and a quote currency.
 * Both currencies are always different. When constructed with a {@link Locale}, the locale's
 * primary currency is used as the base.
 *
 * <pre>{@code
 * CurrencyPairGenerator gen = new CurrencyPairGenerator();
 *
 * // Random pair — both currencies random
 * String pair = gen.generate();                    // "EUR/USD"
 * CurrencyPair info = gen.generateWithInfo();      // base=EUR, quote=USD
 *
 * // Locale-aware — base is the locale's primary currency
 * String usdPair = gen.generate(Locale.US);        // "USD/..."
 * String jpyPair = gen.generate(Locale.JAPAN);     // "JPY/..."
 * }</pre>
 */
public final class CurrencyPairGenerator implements Generator<String> {

    private static final Currency[] ALL_CURRENCIES = Currency.values();

    private final Random random;

    /**
     * Creates a generator backed by a cryptographically strong random source.
     */
    public CurrencyPairGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given configuration.
     *
     * @param config generator configuration (seed, locale); must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CurrencyPairGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a random currency pair string (e.g., {@code "EUR/USD"}).
     *
     * @return a {@code BASE/QUOTE} pair string with two randomly chosen different currencies
     */
    @Override
    public String generate() {
        return generateWithInfo().toPairString();
    }

    /**
     * Generates a currency pair string where the base is the locale's primary currency.
     *
     * <p>If the locale is not recognized, both currencies are chosen at random.
     *
     * @param locale the locale whose primary currency becomes the base; may be {@code null}
     * @return a {@code BASE/QUOTE} pair string
     */
    public String generate(Locale locale) {
        return generateWithInfo(locale).toPairString();
    }

    /**
     * Generates a random {@link CurrencyPair} with two different currencies.
     *
     * @return a randomly generated currency pair
     */
    public CurrencyPair generateWithInfo() {
        int baseIdx = random.nextInt(ALL_CURRENCIES.length);
        int quoteIdx;
        do {
            quoteIdx = random.nextInt(ALL_CURRENCIES.length);
        } while (quoteIdx == baseIdx);
        return new CurrencyPair(
            ALL_CURRENCIES[baseIdx].toInfo(),
            ALL_CURRENCIES[quoteIdx].toInfo());
    }

    /**
     * Generates a {@link CurrencyPair} where the base is the locale's primary currency.
     *
     * <p>If the locale is {@code null} or not recognized, both currencies are random.
     *
     * @param locale the locale whose primary currency becomes the base; may be {@code null}
     * @return a currency pair with the locale's currency as the base
     */
    public CurrencyPair generateWithInfo(Locale locale) {
        Currency base = Currency.forLocale(locale);
        if (base == null) {
            return generateWithInfo();
        }
        Currency quote;
        do {
            quote = ALL_CURRENCIES[random.nextInt(ALL_CURRENCIES.length)];
        } while (quote == base);
        return new CurrencyPair(base.toInfo(), quote.toInfo());
    }
}
