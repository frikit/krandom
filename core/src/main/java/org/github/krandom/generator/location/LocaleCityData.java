/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Built-in city name data for all supported locales.
 *
 * <p>Each constant loads its city list from a classpath resource file at enum-initialization
 * time. City names are from the country/region associated with the locale, so {@link #EN_US}
 * returns US cities, {@link #DE_DE} returns German cities, {@link #JA_JP} returns Japanese
 * cities, etc.
 *
 * <p>Data files live at {@code krandom/cities/{locale}_cities.txt} on the classpath.
 */
public enum LocaleCityData implements CityDataProvider {

    /** US cities (e.g., {@code "New York"}, {@code "Los Angeles"}, {@code "Chicago"}). */
    EN_US(Locale.of("en", "US"), "en_US"),
    /** UK cities (e.g., {@code "London"}, {@code "Manchester"}, {@code "Edinburgh"}). */
    EN_GB(Locale.of("en", "GB"), "en_GB"),
    /** Australian cities (e.g., {@code "Sydney"}, {@code "Melbourne"}, {@code "Brisbane"}). */
    EN_AU(Locale.of("en", "AU"), "en_AU"),
    /** French cities (e.g., {@code "Paris"}, {@code "Lyon"}, {@code "Marseille"}). */
    FR_FR(Locale.of("fr", "FR"), "fr_FR"),
    /** German cities (e.g., {@code "Berlin"}, {@code "München"}, {@code "Hamburg"}). */
    DE_DE(Locale.of("de", "DE"), "de_DE"),
    /** Japanese cities (e.g., {@code "東京"}, {@code "大阪"}, {@code "京都"}). */
    JA_JP(Locale.of("ja", "JP"), "ja_JP"),
    /** Spanish cities (e.g., {@code "Madrid"}, {@code "Barcelona"}, {@code "Valencia"}). */
    ES_ES(Locale.of("es", "ES"), "es_ES"),
    /** Italian cities (e.g., {@code "Roma"}, {@code "Milano"}, {@code "Napoli"}). */
    IT_IT(Locale.of("it", "IT"), "it_IT"),
    /** Brazilian cities (e.g., {@code "São Paulo"}, {@code "Rio de Janeiro"}, {@code "Brasília"}). */
    PT_BR(Locale.of("pt", "BR"), "pt_BR"),
    /** Chinese cities (e.g., {@code "北京"}, {@code "上海"}, {@code "广州"}). */
    ZH_CN(Locale.of("zh", "CN"), "zh_CN");

    private final Locale locale;
    private final String[] cities;

    LocaleCityData(Locale locale, String resourcePrefix) {
        this.locale = locale;
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

    /**
     * Returns all enum constants as a list of providers (for registry initialization).
     *
     * @return list of all built-in providers
     */
    static List<CityDataProvider> allProviders() {
        return Arrays.asList(values());
    }
}
