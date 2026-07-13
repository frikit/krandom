/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.user.PersonInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Structured payment payload for end-to-end finance fixtures.
 *
 * @param paymentNumber       payment identifier
 * @param orderNumber         related order identifier
 * @param invoiceNumber       related invoice identifier
 * @param status              payment lifecycle status
 * @param method              payment method label
 * @param processor           payment processor label
 * @param authorizedOn        authorization date
 * @param settledOn           settlement/capture date when available
 * @param currencyCode        ISO currency code
 * @param amount              payment amount
 * @param payer               payer payload
 * @param billingAddress      billing address payload
 * @param instrumentReference masked card reference or opaque banking test reference
 */
public record PaymentInfo(
    String paymentNumber,
    String orderNumber,
    String invoiceNumber,
    String status,
    String method,
    String processor,
    LocalDate authorizedOn,
    LocalDate settledOn,
    String currencyCode,
    BigDecimal amount,
    PersonInfo payer,
    AddressInfo billingAddress,
    String instrumentReference
) {

}
