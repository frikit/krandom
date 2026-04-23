/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ShipmentInfoGenerator")
class ShipmentInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured shipment payload")
    void generateShipmentInfo() {
        ShipmentInfo info = new ShipmentInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(info.shipmentNumber().startsWith("SHP-"));
        assertTrue(info.orderNumber().startsWith("ORD-"));
        assertFalse(info.carrier().isBlank());
        assertFalse(info.serviceLevel().isBlank());
        assertFalse(info.trackingNumber().isBlank());
        assertFalse(info.status().isBlank());
        assertNotNull(info.shippedOn());
        assertNotNull(info.estimatedDeliveryOn());
        assertFalse(info.estimatedDeliveryOn().isBefore(info.shippedOn()));
        assertTrue(info.weightKg().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(info.recipient());
        assertEquals(info.recipient().address(), info.destination());
        if ("DELIVERED".equals(info.status())) {
            assertNotNull(info.deliveredOn());
            assertFalse(info.deliveredOn().isBefore(info.shippedOn()));
        } else {
            assertNull(info.deliveredOn());
        }
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.CANADA)
                                                .seed(42L)
                                                .build();

        ShipmentInfoGenerator one = new ShipmentInfoGenerator(config);
        ShipmentInfoGenerator two = new ShipmentInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("status coverage reaches delivered and undelivered branches")
    void statusCoverage() {
        ShipmentInfoGenerator generator = new ShipmentInfoGenerator(
            GeneratorConfig.builder().locale(Locale.US).seed(7L).build());
        boolean sawDelivered = false;
        boolean sawUndelivered = false;

        for (int i = 0; i < 128; i++) {
            ShipmentInfo info = generator.generate();
            sawDelivered |= "DELIVERED".equals(info.status());
            sawUndelivered |= !"DELIVERED".equals(info.status());
        }

        assertTrue(sawDelivered);
        assertTrue(sawUndelivered);
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new ShipmentInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new ShipmentInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new ShipmentInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofShipmentInfo().generate());
        assertNotNull(Generators.ofShipmentInfo(Locale.US).generate());
        assertNotNull(Generators.ofShipmentInfo(GeneratorConfig.defaults()).generate());
    }
}
