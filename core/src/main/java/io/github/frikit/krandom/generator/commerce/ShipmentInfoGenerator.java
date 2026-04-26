/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates structured shipment payloads derived from coherent order data.
 */
public final class ShipmentInfoGenerator implements Generator<ShipmentInfo> {

    private static final String[] CARRIERS = {
        "UPS", "FedEx", "USPS", "DHL", "Royal Mail"
    };
    private static final String[] SERVICE_LEVELS = {
        "STANDARD", "EXPRESS", "TWO_DAY", "OVERNIGHT", "ECONOMY"
    };
    private static final String[] SHIPMENT_STATUSES = {
        "SHIPPED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"
    };

    private final GeneratorConfig    config;
    private final Random             random;
    private final UUIDGenerator      uuidGenerator;
    private final OrderInfoGenerator orderInfoGenerator;

    /**
     * Creates a shipment-info generator using default configuration ({@link Locale#US}).
     */
    public ShipmentInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a shipment-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public ShipmentInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a shipment-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public ShipmentInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.uuidGenerator = new UUIDGenerator(config);
        this.orderInfoGenerator = new OrderInfoGenerator(config);
    }

    @Override
    public ShipmentInfo generate() {
        OrderInfo order = orderInfoGenerator.generate();
        String carrier = CARRIERS[random.nextInt(CARRIERS.length)];
        String status = SHIPMENT_STATUSES[random.nextInt(SHIPMENT_STATUSES.length)];
        LocalDate shippedOn = order.orderedOn().plusDays(random.nextInt(0, 3));
        LocalDate estimatedDeliveryOn = shippedOn.plusDays(random.nextInt(1, 8));
        LocalDate deliveredOn = "DELIVERED".equals(status)
            ? estimatedDeliveryOn.plusDays(random.nextInt(-1, 2))
            : null;
        BigDecimal weightKg = BigDecimal.valueOf((0.2 + (random.nextDouble() * 4.8)) * Math.max(1, order.quantity()))
                                        .setScale(2, RoundingMode.HALF_UP);

        return new ShipmentInfo(
            prefixedId("SHP"),
            order.orderNumber(),
            carrier,
            SERVICE_LEVELS[random.nextInt(SERVICE_LEVELS.length)],
            trackingNumber(carrier),
            status,
            shippedOn,
            estimatedDeliveryOn,
            deliveredOn,
            weightKg,
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

    private String trackingNumber(String carrier) {
        String compact = uuidGenerator.generateV4String().replace("-", "").toUpperCase(Locale.ROOT);
        String normalizedCarrier = carrier.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        return normalizedCarrier.substring(0, Math.min(3, normalizedCarrier.length())) + compact.substring(0, 15);
    }
}
