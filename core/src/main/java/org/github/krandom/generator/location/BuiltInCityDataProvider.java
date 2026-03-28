/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in city provider backed by classpath city resources.
 */
final class BuiltInCityDataProvider implements CityDataProvider {

    private final Locale   locale;
    private final String[] cities;

    BuiltInCityDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        this.cities = CityResourceLoader.load("krandom/cities/" + resourcePrefix + "_cities.txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getCities() {
        return cities.clone();
    }
}
