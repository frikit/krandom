/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.location.AddressInfo;
import org.github.krandom.generator.user.CompanyInfo;
import org.github.krandom.generator.user.PersonInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Structured invoice payload for end-to-end finance fixtures.
 *
 * @param invoiceNumber  invoice identifier
 * @param orderNumber    related order identifier
 * @param status         invoice lifecycle status
 * @param issuedOn       invoice issue date
 * @param dueOn          payment due date
 * @param currencyCode   ISO currency code
 * @param subtotal       pre-tax amount
 * @param tax            tax amount
 * @param total          subtotal + tax
 * @param seller         seller/company payload
 * @param customer       billed customer payload
 * @param billingAddress billing address payload
 */
public record InvoiceInfo(
    String invoiceNumber,
    String orderNumber,
    String status,
    LocalDate issuedOn,
    LocalDate dueOn,
    String currencyCode,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    CompanyInfo seller,
    PersonInfo customer,
    AddressInfo billingAddress
) {

}
