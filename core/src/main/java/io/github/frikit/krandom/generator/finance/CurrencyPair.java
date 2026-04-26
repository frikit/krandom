/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.Objects;

/**
 * Represents a foreign exchange (FX) currency pair.
 *
 * <p>A currency pair consists of a <em>base</em> currency and a <em>quote</em> currency.
 * The notation {@code "EUR/USD"} means: 1 EUR is worth X USD.
 *
 * <p>Base and quote are always different currencies.
 *
 * <pre>{@code
 * CurrencyPair pair = new CurrencyPairGenerator().generateWithInfo();
 * System.out.println(pair.toPairString());      // "EUR/USD"
 * System.out.println(pair.base().code());       // "EUR"
 * System.out.println(pair.quote().symbol());    // "$"
 * }</pre>
 *
 * @param base  the base currency (left side of the pair)
 * @param quote the quote currency (right side of the pair); always different from {@code base}
 * @see CurrencyPairGenerator
 */
public record CurrencyPair(CurrencyInfo base, CurrencyInfo quote) {

    /**
     * Compact constructor — validates that base and quote are not null and not the same currency.
     *
     * @throws NullPointerException     if {@code base} or {@code quote} is {@code null}
     * @throws IllegalArgumentException if {@code base} and {@code quote} have the same ISO code
     */
    public CurrencyPair {
        Objects.requireNonNull(base, "base must not be null");
        Objects.requireNonNull(quote, "quote must not be null");
        if (base.code().equals(quote.code())) {
            throw new IllegalArgumentException(
                "base and quote must be different currencies, got: " + base.code());
        }
    }

    /**
     * Returns this pair in standard FX notation (e.g., {@code "EUR/USD"}).
     *
     * @return the pair string in {@code BASE/QUOTE} format
     */
    public String toPairString() {
        return base.code() + "/" + quote.code();
    }
}
