/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

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

@DisplayName("PaymentInfoGenerator")
class PaymentInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured payment payload")
    void generatePaymentInfo() {
        PaymentInfo info = new PaymentInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(info.paymentNumber().startsWith("PAY-"));
        assertTrue(info.orderNumber().startsWith("ORD-"));
        assertTrue(info.invoiceNumber().startsWith("INV-"));
        assertFalse(info.status().isBlank());
        assertFalse(info.method().isBlank());
        assertFalse(info.processor().isBlank());
        assertNotNull(info.authorizedOn());
        assertFalse(info.currencyCode().isBlank());
        assertTrue(info.amount().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(info.payer());
        assertEquals(info.payer().address(), info.billingAddress());
        assertFalse(info.instrumentReference().isBlank());
        if ("AUTHORIZED".equals(info.status()) || "FAILED".equals(info.status())) {
            assertNull(info.settledOn());
        } else {
            assertNotNull(info.settledOn());
            assertFalse(info.settledOn().isBefore(info.authorizedOn()));
        }
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.CANADA)
                                                .seed(42L)
                                                .build();

        PaymentInfoGenerator one = new PaymentInfoGenerator(config);
        PaymentInfoGenerator two = new PaymentInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("method and settlement branches are exercised")
    void branchCoverage() {
        PaymentInfoGenerator generator = new PaymentInfoGenerator(
            GeneratorConfig.builder().locale(Locale.US).seed(9L).build());
        boolean sawCard = false;
        boolean sawBank = false;
        boolean sawSettled = false;
        boolean sawUnsettled = false;

        for (int i = 0; i < 192; i++) {
            PaymentInfo info = generator.generate();
            sawCard |= "CARD".equals(info.method());
            sawBank |= !"CARD".equals(info.method());
            sawSettled |= info.settledOn() != null;
            sawUnsettled |= info.settledOn() == null;
        }

        assertTrue(sawCard);
        assertTrue(sawBank);
        assertTrue(sawSettled);
        assertTrue(sawUnsettled);
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new PaymentInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new PaymentInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new PaymentInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofPaymentInfo().generate());
        assertNotNull(Generators.ofPaymentInfo(Locale.US).generate());
        assertNotNull(Generators.ofPaymentInfo(GeneratorConfig.defaults()).generate());
    }
}
