/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.user.PersonInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Structured order payload for end-to-end commerce fixtures.
 *
 * @param orderNumber     order identifier
 * @param status          order lifecycle status
 * @param orderedOn       order creation date
 * @param currencyCode    ISO currency code
 * @param quantity        purchased quantity
 * @param unitPrice       single-item price
 * @param subtotal        quantity * unitPrice
 * @param shipping        shipping charge
 * @param tax             tax amount
 * @param total           subtotal + shipping + tax
 * @param customer        customer payload
 * @param product         purchased product payload
 * @param shippingAddress shipping address payload
 */
public record OrderInfo(
    String orderNumber,
    String status,
    LocalDate orderedOn,
    String currencyCode,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal tax,
    BigDecimal total,
    PersonInfo customer,
    ProductInfo product,
    AddressInfo shippingAddress
) {

}
