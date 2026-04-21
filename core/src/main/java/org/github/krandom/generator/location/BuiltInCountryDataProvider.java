/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Built-in country provider backed by classpath country resources.
 */
final class BuiltInCountryDataProvider implements CountryDataProvider {

    private final Locale   locale;
    private final String[] countries;

    BuiltInCountryDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        if (supportedLocale == SupportedLocale.NL_NL || supportedLocale == SupportedLocale.PL_PL) {
            this.countries = localizedCountryNames(locale);
        } else {
            String resourcePrefix = supportedLocale.resourcePrefix();
            this.countries = CountryResourceLoader.load("krandom/countries/" + resourcePrefix + "_countries.txt");
        }
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
            String displayCountry = Locale.of("", countryCode).getDisplayCountry(locale);
            if (displayCountry != null && !displayCountry.isBlank()) {
                countries.add(displayCountry);
            }
        }
        return countries.toArray(String[]::new);
    }
}
