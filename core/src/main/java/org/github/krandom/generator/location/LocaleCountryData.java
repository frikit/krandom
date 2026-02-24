/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import java.util.Locale;

/**
 * Built-in country name data for all supported locales.
 *
 * <p>Each constant loads its country list from a classpath resource file at enum-initialization
 * time. Country names are translated into the target locale's language, so the same geographic
 * entity has a different name depending on the configured locale — for example, Germany is
 * {@code "Germany"} in {@link #EN_US} but {@code "Deutschland"} in {@link #DE_DE} and
 * {@code "ドイツ"} in {@link #JA_JP}.
 *
 * <p>Data files live at {@code krandom/countries/{locale}_countries.txt} on the classpath.
 */
public enum LocaleCountryData implements CountryDataProvider {

    /** US English country names (e.g., {@code "Germany"}, {@code "France"}). */
    EN_US(Locale.of("en", "US"), "en_US"),
    /** British English country names — identical to US English. */
    EN_GB(Locale.of("en", "GB"), "en_GB"),
    /** Australian English country names — identical to US English. */
    EN_AU(Locale.of("en", "AU"), "en_AU"),
    /** French country names (e.g., {@code "Allemagne"}, {@code "France"}). */
    FR_FR(Locale.of("fr", "FR"), "fr_FR"),
    /** German country names (e.g., {@code "Deutschland"}, {@code "Frankreich"}). */
    DE_DE(Locale.of("de", "DE"), "de_DE"),
    /** Japanese country names (e.g., {@code "ドイツ"}, {@code "フランス"}). */
    JA_JP(Locale.of("ja", "JP"), "ja_JP"),
    /** Spanish country names (e.g., {@code "Alemania"}, {@code "Francia"}). */
    ES_ES(Locale.of("es", "ES"), "es_ES"),
    /** Italian country names (e.g., {@code "Germania"}, {@code "Francia"}). */
    IT_IT(Locale.of("it", "IT"), "it_IT"),
    /** Brazilian Portuguese country names (e.g., {@code "Alemanha"}, {@code "França"}). */
    PT_BR(Locale.of("pt", "BR"), "pt_BR"),
    /** Simplified Chinese country names (e.g., {@code "德国"}, {@code "法国"}). */
    ZH_CN(Locale.of("zh", "CN"), "zh_CN");

    private final Locale locale;
    private final String[] countries;

    LocaleCountryData(Locale locale, String resourcePrefix) {
        this.locale    = locale;
        this.countries = CountryResourceLoader.load("krandom/countries/" + resourcePrefix + "_countries.txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getCountries() {
        return countries.clone();
    }
}
