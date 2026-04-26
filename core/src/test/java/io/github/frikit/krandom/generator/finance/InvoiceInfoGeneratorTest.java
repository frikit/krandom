/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InvoiceInfoGenerator")
class InvoiceInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured invoice payload")
    void generateInvoiceInfo() {
        InvoiceInfo info = new InvoiceInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(info.invoiceNumber().startsWith("INV-"));
        assertTrue(info.orderNumber().startsWith("ORD-"));
        assertFalse(info.status().isBlank());
        assertNotNull(info.issuedOn());
        assertNotNull(info.dueOn());
        assertFalse(info.dueOn().isBefore(info.issuedOn()));
        assertFalse(info.currencyCode().isBlank());
        assertEquals(0, info.total().compareTo(info.subtotal().add(info.tax()).setScale(2)));
        assertNotNull(info.seller());
        assertNotNull(info.customer());
        assertEquals(info.customer().address(), info.billingAddress());
        assertTrue(info.customer().contact().email().contains("@"));
        assertTrue(info.seller().email().contains("@"));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        InvoiceInfoGenerator one = new InvoiceInfoGenerator(config);
        InvoiceInfoGenerator two = new InvoiceInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new InvoiceInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new InvoiceInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new InvoiceInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofInvoiceInfo().generate());
        assertNotNull(Generators.ofInvoiceInfo(Locale.US).generate());
        assertNotNull(Generators.ofInvoiceInfo(GeneratorConfig.defaults()).generate());
    }
}
