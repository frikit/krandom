/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates structured product payloads.
 */
public final class ProductInfoGenerator implements Generator<ProductInfo> {

    private final GeneratorConfig   config;
    private final CommerceGenerator commerceGenerator;

    /**
     * Creates a product-info generator using default configuration ({@link Locale#US}).
     */
    public ProductInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a product-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public ProductInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a product-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public ProductInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.commerceGenerator = new CommerceGenerator(config);
    }

    @Override
    public ProductInfo generate() {
        return commerceGenerator.generateProductInfo();
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }
}
