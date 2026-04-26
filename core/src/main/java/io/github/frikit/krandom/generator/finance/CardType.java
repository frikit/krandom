/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.List;

/**
 * Enumeration of supported credit card types with their IIN (Issuer Identification Number) patterns
 * and card number lengths.
 *
 * <p>Each card type defines:
 * <ul>
 *   <li>Prefix patterns (IIN ranges) that identify the card issuer</li>
 *   <li>Valid card number lengths</li>
 *   <li>CVV length (typically 3 digits, except American Express with 4)</li>
 * </ul>
 */
public enum CardType {

    /**
     * Visa cards starting with 4, typically 16 digits (also supports 13-digit legacy format).
     */
    VISA("Visa", List.of("4"), List.of(16, 13), 3),

    /**
     * Mastercard with prefixes 51-55 or 2221-2720, 16 digits.
     */
    MASTERCARD("Mastercard",
               List.of("51", "52", "53", "54", "55", "2221-2720"),
               List.of(16),
               3),

    /**
     * American Express (Amex) with prefixes 34 or 37, 15 digits, 4-digit CVV.
     */
    AMEX("American Express", List.of("34", "37"), List.of(15), 4),

    /**
     * Discover cards with prefixes 6011, 644-649, or 65, 16 digits.
     */
    DISCOVER("Discover", List.of("6011", "644-649", "65"), List.of(16), 3),

    /**
     * JCB cards with prefixes 3528-3589, 16 digits.
     */
    JCB("JCB", List.of("3528-3589"), List.of(16), 3),

    /**
     * Diners Club with prefixes 300-305, 36, or 38, 14 digits.
     */
    DINERS_CLUB("Diners Club", List.of("300-305", "36", "38"), List.of(14), 3),

    /**
     * Random card type - when used, a random card type will be selected from the above options.
     */
    RANDOM("Random", List.of(), List.of(), 3);

    private final String        displayName;
    private final List<String>  prefixPatterns;
    private final List<Integer> cardLengths;
    private final int           cvvLength;

    CardType(String displayName, List<String> prefixPatterns, List<Integer> cardLengths, int cvvLength) {
        this.displayName = displayName;
        this.prefixPatterns = prefixPatterns;
        this.cardLengths = cardLengths;
        this.cvvLength = cvvLength;
    }

    /**
     * Returns the display name of this card type.
     *
     * @return the display name (e.g., "Visa", "Mastercard")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the list of prefix patterns for this card type.
     * Patterns can be single values (e.g., "4") or ranges (e.g., "51-55").
     *
     * @return list of prefix patterns
     */
    public List<String> getPrefixPatterns() {
        return prefixPatterns;
    }

    /**
     * Returns the valid card number lengths for this card type.
     *
     * @return list of valid lengths
     */
    public List<Integer> getCardLengths() {
        return cardLengths;
    }

    /**
     * Returns the CVV length for this card type (3 for most cards, 4 for Amex).
     *
     * @return CVV length in digits
     */
    public int getCvvLength() {
        return cvvLength;
    }
}
