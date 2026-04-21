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
        if (supportedLocale == SupportedLocale.NL_NL
            || supportedLocale == SupportedLocale.PL_PL
            || supportedLocale == SupportedLocale.CS_CZ
            || supportedLocale == SupportedLocale.KO_KR
            || supportedLocale == SupportedLocale.RU_RU
            || supportedLocale == SupportedLocale.TR_TR
            || supportedLocale == SupportedLocale.SV_SE
            || supportedLocale == SupportedLocale.NB_NO
            || supportedLocale == SupportedLocale.AR_SA
            || supportedLocale == SupportedLocale.HI_IN) {
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
            countries.add(Locale.of("", countryCode).getDisplayCountry(locale));
        }
        return countries.toArray(String[]::new);
    }
}
