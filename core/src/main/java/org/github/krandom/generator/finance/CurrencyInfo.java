/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

/**
 * Represents complete information about a currency conforming to ISO 4217 standards.
 *
 * <p>This record encapsulates the four key identifiers for a currency:
 * <ul>
 *   <li><strong>Code</strong>: The 3-letter ISO 4217 alphabetic code (e.g., "USD", "EUR")</li>
 *   <li><strong>Name</strong>: The full official name (e.g., "United States Dollar", "Euro")</li>
 *   <li><strong>Symbol</strong>: The currency symbol (e.g., "$", "€", "£")</li>
 *   <li><strong>Numeric Code</strong>: The 3-digit ISO 4217 numeric code (e.g., "840", "978")</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * CurrencyInfo usd = new CurrencyInfo("USD", "United States Dollar", "$", "840");
 * System.out.println(usd.code());     // "USD"
 * System.out.println(usd.name());     // "United States Dollar"
 * System.out.println(usd.symbol());   // "$"
 * System.out.println(usd.numericCode()); // "840"
 * }</pre>
 *
 * @param code the ISO 4217 3-letter alphabetic code
 * @param name the full official currency name
 * @param symbol the currency symbol
 * @param numericCode the ISO 4217 3-digit numeric code
 */
public record CurrencyInfo(String code, String name, String symbol, String numericCode) {
    
    /**
     * Creates a new CurrencyInfo with the specified properties.
     *
     * @param code the ISO 4217 3-letter alphabetic code
     * @param name the full official currency name
     * @param symbol the currency symbol
     * @param numericCode the ISO 4217 3-digit numeric code
     * @throws NullPointerException if any parameter is null
     */
    public CurrencyInfo {
        if (code == null || name == null || symbol == null || numericCode == null) {
            throw new NullPointerException("All CurrencyInfo fields must be non-null");
        }
    }
}
