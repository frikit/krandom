/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.commerce.*;

import java.util.Locale;

/**
 * Fluent namespace for commerce-related generators.
 *
 * <p>Usage: {@code Generators.commerce().product().generate()}
 */
public final class CommerceGenerators {

    private final GeneratorConfig config;

    public CommerceGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public CommerceGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public CommerceGenerator commerce() { return new CommerceGenerator(config); }

    public ProductInfoGenerator product() { return new ProductInfoGenerator(config); }
    public ProductInfoGenerator product(Locale locale) { return new ProductInfoGenerator(withLocale(locale)); }

    public OrderInfoGenerator order() { return new OrderInfoGenerator(config); }
    public OrderInfoGenerator order(Locale locale) { return new OrderInfoGenerator(withLocale(locale)); }

    public ShipmentInfoGenerator shipment() { return new ShipmentInfoGenerator(config); }
    public ShipmentInfoGenerator shipment(Locale locale) { return new ShipmentInfoGenerator(withLocale(locale)); }

    private GeneratorConfig withLocale(Locale locale) {
        return config.toBuilder().locale(locale).build();
    }
}
