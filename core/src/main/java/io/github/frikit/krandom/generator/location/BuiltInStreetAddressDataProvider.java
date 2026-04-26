/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in street-address provider backed by classpath street resources.
 */
final class BuiltInStreetAddressDataProvider implements StreetAddressDataProvider {

    private final Locale   locale;
    private final String[] streetNames;
    private final String[] streetTypesShort;
    private final String[] streetTypesLong;

    BuiltInStreetAddressDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        this.streetNames = StreetAddressResourceLoader.load("krandom/streets/" + resourcePrefix + "_street_names.txt");
        this.streetTypesShort = StreetAddressResourceLoader.load("krandom/streets/" + resourcePrefix + "_street_types_short.txt");
        this.streetTypesLong = StreetAddressResourceLoader.load("krandom/streets/" + resourcePrefix + "_street_types_long.txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getStreetNames() {
        return streetNames.clone();
    }

    @Override
    public String[] getStreetTypesShort() {
        return streetTypesShort.clone();
    }

    @Override
    public String[] getStreetTypesLong() {
        return streetTypesLong.clone();
    }
}
