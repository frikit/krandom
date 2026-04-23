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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OrderInfoGenerator")
class OrderInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured order payload")
    void generateOrderInfo() {
        OrderInfo info = new OrderInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(info.orderNumber().startsWith("ORD-"));
        assertFalse(info.status().isBlank());
        assertNotNull(info.orderedOn());
        assertFalse(info.currencyCode().isBlank());
        assertTrue(info.quantity() > 0);
        assertTrue(info.unitPrice().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(0, info.subtotal().compareTo(
            info.unitPrice().multiply(BigDecimal.valueOf(info.quantity())).setScale(2)));
        assertEquals(0, info.total().compareTo(info.subtotal().add(info.shipping()).add(info.tax()).setScale(2)));
        assertNotNull(info.customer());
        assertNotNull(info.product());
        assertEquals(info.customer().address(), info.shippingAddress());
        assertTrue(info.customer().contact().email().contains("@"));
        assertFalse(info.product().name().isBlank());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        OrderInfoGenerator one = new OrderInfoGenerator(config);
        OrderInfoGenerator two = new OrderInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new OrderInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new OrderInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new OrderInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofOrderInfo().generate());
        assertNotNull(Generators.ofOrderInfo(Locale.US).generate());
        assertNotNull(Generators.ofOrderInfo(GeneratorConfig.defaults()).generate());
    }
}
