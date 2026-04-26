/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.commerce.OrderInfo;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.user.CompanyInfo;
import io.github.frikit.krandom.generator.user.CompanyInfoGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates structured invoice payloads derived from coherent order data.
 */
public final class InvoiceInfoGenerator implements Generator<InvoiceInfo> {

    private static final String[] INVOICE_STATUSES = {
        "DRAFT", "SENT", "PAID", "OVERDUE"
    };

    private final GeneratorConfig      config;
    private final Random               random;
    private final UUIDGenerator        uuidGenerator;
    private final OrderInfoGenerator   orderInfoGenerator;
    private final CompanyInfoGenerator companyInfoGenerator;

    /**
     * Creates an invoice-info generator using default configuration ({@link Locale#US}).
     */
    public InvoiceInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an invoice-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public InvoiceInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates an invoice-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public InvoiceInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.uuidGenerator = new UUIDGenerator(config);
        this.orderInfoGenerator = new OrderInfoGenerator(config);
        this.companyInfoGenerator = new CompanyInfoGenerator(config);
    }

    @Override
    public InvoiceInfo generate() {
        OrderInfo order = orderInfoGenerator.generate();
        CompanyInfo seller = companyInfoGenerator.generate();
        LocalDate issuedOn = order.orderedOn().plusDays(random.nextInt(0, 5));
        LocalDate dueOn = issuedOn.plusDays(random.nextInt(7, 46));
        BigDecimal subtotal = order.subtotal().add(order.shipping()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = order.tax().setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        return new InvoiceInfo(
            prefixedId("INV"),
            order.orderNumber(),
            INVOICE_STATUSES[random.nextInt(INVOICE_STATUSES.length)],
            issuedOn,
            dueOn,
            order.currencyCode(),
            subtotal,
            tax,
            total,
            seller,
            order.customer(),
            order.shippingAddress()
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private String prefixedId(String prefix) {
        String compact = uuidGenerator.generateV4String().replace("-", "").toUpperCase(Locale.ROOT);
        return prefix + "-" + compact.substring(0, 12);
    }
}
