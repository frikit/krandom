/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

/**
 * GoFakeit-style bank/ACH payload.
 *
 * @param accountNumber account number
 * @param routingNumber routing (ABA) number
 * @param bankName bank name
 * @param bankType bank type
 * @param accountName account label
 * @param transactionType transaction type
 */
public record BankInfo(
        String accountNumber,
        String routingNumber,
        String bankName,
        String bankType,
        String accountName,
        String transactionType
) {
}
