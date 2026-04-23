/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

import org.github.krandom.generator.location.AddressInfo;
import org.github.krandom.generator.user.PersonInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Structured shipment payload for end-to-end commerce fixtures.
 *
 * @param shipmentNumber       shipment identifier
 * @param orderNumber          related order identifier
 * @param carrier              carrier display name
 * @param serviceLevel         service/tier label
 * @param trackingNumber       carrier tracking reference
 * @param status               shipment lifecycle status
 * @param shippedOn            ship date
 * @param estimatedDeliveryOn  estimated delivery date
 * @param deliveredOn          actual delivery date when available
 * @param weightKg             package weight in kilograms
 * @param recipient            shipment recipient payload
 * @param destination          destination address payload
 */
public record ShipmentInfo(
    String shipmentNumber,
    String orderNumber,
    String carrier,
    String serviceLevel,
    String trackingNumber,
    String status,
    LocalDate shippedOn,
    LocalDate estimatedDeliveryOn,
    LocalDate deliveredOn,
    BigDecimal weightKg,
    PersonInfo recipient,
    AddressInfo destination
) {

}
