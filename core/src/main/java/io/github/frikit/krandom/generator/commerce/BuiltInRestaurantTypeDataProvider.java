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
 * Built-in restaurant cuisine/type names backed by classpath resources.
 *
 * <p>Names are loaded from {@code krandom/restaurant_types/<locale>.txt}: one localized type per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInRestaurantTypeDataProvider implements RestaurantTypeDataProvider {

    private final Locale locale;
    private final List<String> types;

    BuiltInRestaurantTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(),
            "krandom/restaurant_types/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInRestaurantTypeDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.types = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getTypes() {
        return types;
    }
}
