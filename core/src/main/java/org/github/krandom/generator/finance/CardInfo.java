/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import java.util.Objects;

/**
 * Immutable container for complete credit card information including card number, type, CVV, and expiration date.
 *
 * <p>Example usage:
 * <pre>{@code
 *   CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
 *   CardInfo info = gen.generateWithType();
 *
 *   System.out.println("Card: " + info.cardNumber());       // "4532 1488 0343 6467"
 *   System.out.println("Type: " + info.cardType());         // VISA
 *   System.out.println("CVV: " + info.cvv());               // "123"
 *   System.out.println("Expires: " + info.expirationDate()); // "12/28"
 * }</pre>
 */
public record CardInfo(
    String cardNumber,
    CardType cardType,
    String cvv,
    String expirationDate
) {

    /**
     * Creates a new CardInfo instance.
     *
     * @param cardNumber     the card number (formatted or unformatted)
     * @param cardType       the card type
     * @param cvv            the CVV/CVC code
     * @param expirationDate the expiration date in MM/YY format
     * @throws NullPointerException if any parameter is {@code null}
     */
    public CardInfo {
        Objects.requireNonNull(cardNumber, "cardNumber must not be null");
        Objects.requireNonNull(cardType, "cardType must not be null");
        Objects.requireNonNull(cvv, "cvv must not be null");
        Objects.requireNonNull(expirationDate, "expirationDate must not be null");
    }
}
