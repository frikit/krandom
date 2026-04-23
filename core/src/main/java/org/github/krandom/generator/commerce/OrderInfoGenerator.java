/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.finance.CurrencyGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.user.PersonInfo;
import org.github.krandom.generator.user.PersonInfoGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates structured order payloads with coherent totals and linked customer/shipping data.
 */
public final class OrderInfoGenerator implements Generator<OrderInfo> {

    private static final String[] ORDER_STATUSES = {
        "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"
    };

    private final GeneratorConfig      config;
    private final Random               random;
    private final UUIDGenerator        uuidGenerator;
    private final DateGenerator        dateGenerator;
    private final CurrencyGenerator    currencyGenerator;
    private final PersonInfoGenerator  personInfoGenerator;
    private final ProductInfoGenerator productInfoGenerator;

    /**
     * Creates an order-info generator using default configuration ({@link Locale#US}).
     */
    public OrderInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an order-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public OrderInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates an order-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public OrderInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.uuidGenerator = new UUIDGenerator(config);
        this.dateGenerator = new DateGenerator(config);
        this.currencyGenerator = new CurrencyGenerator(config);
        this.personInfoGenerator = new PersonInfoGenerator(config);
        this.productInfoGenerator = new ProductInfoGenerator(config);
    }

    @Override
    public OrderInfo generate() {
        PersonInfo customer = personInfoGenerator.generate();
        ProductInfo product = productInfoGenerator.generate();
        int quantity = random.nextInt(1, 6);
        BigDecimal unitPrice = amountBetween(5.00, 500.00);
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = amountBetween(0.00, 25.00);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05 + (random.nextDouble() * 0.15)))
                                 .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(shipping).add(tax).setScale(2, RoundingMode.HALF_UP);
        return new OrderInfo(
            prefixedId("ORD"),
            ORDER_STATUSES[random.nextInt(ORDER_STATUSES.length)],
            dateGenerator.generate(),
            currencyGenerator.generate(config.getLocale()),
            quantity,
            unitPrice,
            subtotal,
            shipping,
            tax,
            total,
            customer,
            product,
            customer.address()
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private BigDecimal amountBetween(double min, double max) {
        return BigDecimal.valueOf(min + (random.nextDouble() * (max - min)))
                         .setScale(2, RoundingMode.HALF_UP);
    }

    private String prefixedId(String prefix) {
        String compact = uuidGenerator.generateV4String().replace("-", "").toUpperCase(Locale.ROOT);
        return prefix + "-" + compact.substring(0, 12);
    }
}
