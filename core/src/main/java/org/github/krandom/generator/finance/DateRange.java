/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

/**
 * Enumeration of date range options for card expiration generation.
 *
 * <p>This enum controls whether generated expiration dates should be in the past,
 * future, or any time period (past or future).
 *
 * <p><strong>Usage Examples:</strong>
 * <pre>{@code
 * // Generate only future dates (valid cards)
 * CardExpirationGenerator futureGen = new CardExpirationGenerator(DateRange.FUTURE);
 * 
 * // Generate only past dates (expired cards)
 * CardExpirationGenerator pastGen = new CardExpirationGenerator(DateRange.PAST);
 * 
 * // Generate any dates (past or future)
 * CardExpirationGenerator anyGen = new CardExpirationGenerator(DateRange.ANY);
 * }</pre>
 */
public enum DateRange {
    
    /**
     * Generate dates only in the past (1-60 months ago).
     * Useful for testing expired card scenarios.
     */
    PAST,
    
    /**
     * Generate dates only in the future (1-60 months ahead).
     * Default option, ensures all generated dates are valid for testing.
     */
    FUTURE,
    
    /**
     * Generate dates in any time period (up to 60 months in past or future).
     * Useful for comprehensive testing of date handling logic.
     */
    ANY;
    
    /**
     * Converts a boolean futureOnly flag to a DateRange.
     *
     * @param futureOnly if true, returns FUTURE; if false, returns ANY
     * @return the corresponding DateRange
     */
    public static DateRange fromBoolean(boolean futureOnly) {
        return futureOnly ? FUTURE : ANY;
    }
}
