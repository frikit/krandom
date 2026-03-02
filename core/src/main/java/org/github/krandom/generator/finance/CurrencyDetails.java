/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

/**
 * GoFakeit-style currency payload.
 *
 * @param shortCode currency short code (ISO 4217)
 * @param longName currency display name
 * @param symbol currency symbol
 * @param numericCode currency numeric code
 */
public record CurrencyDetails(
        String shortCode,
        String longName,
        String symbol,
        String numericCode
) {
}
