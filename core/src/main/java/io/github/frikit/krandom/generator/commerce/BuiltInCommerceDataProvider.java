/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in commerce vocabulary backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/commerce/<list>/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInCommerceDataProvider implements CommerceDataProvider {

    private final Locale locale;
    private final List<String> adjectives;
    private final List<String> materials;
    private final List<String> products;
    private final List<String> departments;
    private final List<String> colors;
    private final String productNameFormat;
    private final String productDescriptionFormat;

    BuiltInCommerceDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInCommerceDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.adjectives = List.of(LocaleTextResourceLoader.load("krandom/commerce/adjectives/" + resourcePrefix + ".txt"));
        this.materials = List.of(LocaleTextResourceLoader.load("krandom/commerce/materials/" + resourcePrefix + ".txt"));
        this.products = List.of(LocaleTextResourceLoader.load("krandom/commerce/products/" + resourcePrefix + ".txt"));
        this.departments = List.of(LocaleTextResourceLoader.load("krandom/commerce/departments/" + resourcePrefix + ".txt"));
        this.colors = List.of(LocaleTextResourceLoader.load("krandom/commerce/colors/" + resourcePrefix + ".txt"));
        String[] formats = LocaleTextResourceLoader.load("krandom/commerce/formats/" + resourcePrefix + ".txt");
        this.productNameFormat = formats[0];
        this.productDescriptionFormat = formats[1];
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAdjectives() {
        return adjectives;
    }

    @Override
    public List<String> getMaterials() {
        return materials;
    }

    @Override
    public List<String> getProducts() {
        return products;
    }

    @Override
    public List<String> getDepartments() {
        return departments;
    }

    @Override
    public List<String> getColors() {
        return colors;
    }

    @Override
    public String getProductNameFormat() {
        return productNameFormat;
    }

    @Override
    public String getProductDescriptionFormat() {
        return productDescriptionFormat;
    }
}
