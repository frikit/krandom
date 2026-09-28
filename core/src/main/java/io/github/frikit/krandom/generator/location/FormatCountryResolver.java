/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the country whose built-in phone-number and postal-code formats apply to a locale.
 *
 * <ol>
 *   <li>A locale whose country has a built-in format resolves to that country; the language does
 *       not matter (for example {@code en_DE} resolves to {@code DE}).</li>
 *   <li>Otherwise the language resolves to the country of the first {@link SupportedLocale}
 *       constant, in catalog order, with that language (for example {@code en} and {@code en_SG}
 *       to {@code US}, {@code de_LI} to {@code DE}, {@code pt} to {@code BR}, {@code ca} to
 *       {@code ES}). The legacy Norwegian code {@code no} resolves to {@code NO}.</li>
 *   <li>A locale whose language is empty or not in the catalog, such as {@link Locale#ROOT} or
 *       {@code xx_YY}, resolves to the documented default {@code US} ({@code en_US}).</li>
 * </ol>
 */
final class FormatCountryResolver {

    static final String DEFAULT_COUNTRY = "US";

    private static final Map<String, String> PRIMARY_COUNTRY_BY_LANGUAGE = primaryCountryByLanguage();

    private FormatCountryResolver() {
    }

    /**
     * Resolves the format country of {@code locale}.
     *
     * @param locale             the configured locale
     * @param supportedCountries ISO 3166 country codes with a built-in format; must contain every
     *                           catalog country and the default
     * @return the resolved country code, always one of {@code supportedCountries}
     */
    static String resolveSupportedCountry(Locale locale, Set<String> supportedCountries) {
        String country = locale.getCountry();
        if (supportedCountries.contains(country)) {
            return country;
        }
        return PRIMARY_COUNTRY_BY_LANGUAGE.getOrDefault(locale.getLanguage(), DEFAULT_COUNTRY);
    }

    private static Map<String, String> primaryCountryByLanguage() {
        Map<String, String> countries = new HashMap<>();
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            countries.putIfAbsent(locale.getLanguage(), locale.getCountry());
        }
        countries.put("no", "NO");
        return Map.copyOf(countries);
    }
}
