/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.weather;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in weather-condition names backed by classpath resources.
 *
 * <p>Names are loaded from {@code krandom/weather/<locale>.txt}: one localized condition per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInWeatherDataProvider implements WeatherDataProvider {

    private final Locale locale;
    private final List<String> conditions;

    BuiltInWeatherDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "krandom/weather/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInWeatherDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.conditions = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getConditions() {
        return conditions;
    }
}
