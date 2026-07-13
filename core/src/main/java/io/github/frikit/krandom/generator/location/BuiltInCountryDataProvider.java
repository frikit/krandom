/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Built-in country provider backed by classpath country resources.
 *
 * <p>Uses a resource file when one exists for the locale's resource prefix;
 * otherwise falls back to JDK-translated country names via
 * {@link Locale#getDisplayCountry(Locale)}.
 */
final class BuiltInCountryDataProvider implements CountryDataProvider {

    private final Locale   locale;
    private final String[] countries;

    BuiltInCountryDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale, openCountryResource(supportedLocale));
    }

    BuiltInCountryDataProvider(SupportedLocale supportedLocale, InputStream resource) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        String path = "krandom/countries/" + resourcePrefix + "_countries.txt";
        if (resource != null) {
            this.countries = CountryResourceLoader.load(resource, path);
        } else {
            this.countries = localizedCountryNames(locale);
        }
    }

    private static InputStream openCountryResource(SupportedLocale supportedLocale) {
        String resourcePrefix = supportedLocale.resourcePrefix();
        String path = "krandom/countries/" + resourcePrefix + "_countries.txt";
        return BuiltInCountryDataProvider.class.getResourceAsStream("/" + path);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getCountries() {
        return countries.clone();
    }

    private static String[] localizedCountryNames(Locale locale) {
        Set<String> countries = new LinkedHashSet<>();
        for (String countryCode : Locale.getISOCountries()) {
            countries.add(Locale.of("", countryCode).getDisplayCountry(locale));
        }
        return countries.toArray(String[]::new);
    }
}
