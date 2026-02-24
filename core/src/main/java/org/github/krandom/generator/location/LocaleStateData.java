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
 * Built-in state/province name data for all supported locales.
 *
 * <p>Each constant loads its state list from a classpath resource file at enum-initialization
 * time. State names are from the country/region associated with the locale, so {@link #EN_US}
 * returns US states, {@link #EN_GB} returns UK countries, {@link #EN_AU} returns Australian
 * states, etc.
 *
 * <p>Data files live at {@code krandom/states/{locale}_states.txt} on the classpath.
 */
public enum LocaleStateData implements StateDataProvider {

    /** US states (e.g., {@code "California"}, {@code "Texas"}, {@code "New York"}). */
    EN_US(Locale.of("en", "US"), "en_US"),
    /** UK countries (e.g., {@code "England"}, {@code "Scotland"}, {@code "Wales"}, {@code "Northern Ireland"}). */
    EN_GB(Locale.of("en", "GB"), "en_GB"),
    /** Australian states and territories (e.g., {@code "New South Wales"}, {@code "Victoria"}, {@code "Queensland"}). */
    EN_AU(Locale.of("en", "AU"), "en_AU"),
    /** German federal states (e.g., {@code "Bayern"}, {@code "Nordrhein-Westfalen"}, {@code "Baden-Württemberg"}). */
    DE_DE(Locale.of("de", "DE"), "de_DE"),
    /** French regions (e.g., {@code "Île-de-France"}, {@code "Auvergne-Rhône-Alpes"}, {@code "Nouvelle-Aquitaine"}). */
    FR_FR(Locale.of("fr", "FR"), "fr_FR"),
    /** Spanish autonomous communities (e.g., {@code "Andalucía"}, {@code "Cataluña"}, {@code "Madrid"}). */
    ES_ES(Locale.of("es", "ES"), "es_ES"),
    /** Italian regions (e.g., {@code "Toscana"}, {@code "Lombardia"}, {@code "Lazio"}). */
    IT_IT(Locale.of("it", "IT"), "it_IT"),
    /** Brazilian states (e.g., {@code "São Paulo"}, {@code "Rio de Janeiro"}, {@code "Minas Gerais"}). */
    PT_BR(Locale.of("pt", "BR"), "pt_BR"),
    /** Japanese prefectures (e.g., {@code "東京都"}, {@code "大阪府"}, {@code "京都府"}). */
    JA_JP(Locale.of("ja", "JP"), "ja_JP"),
    /** Chinese provinces (e.g., {@code "北京市"}, {@code "上海市"}, {@code "广东省"}). */
    ZH_CN(Locale.of("zh", "CN"), "zh_CN");

    private final Locale locale;
    private final String[] states;
    private final String[] abbreviations;

    LocaleStateData(Locale locale, String resourcePrefix) {
        this.locale = locale;
        StateResourceLoader.StateData data = 
                StateResourceLoader.load("krandom/states/" + resourcePrefix + "_states.txt");
        this.states = data.states;
        this.abbreviations = data.abbreviations;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getStates() {
        return states.clone();
    }

    @Override
    public String[] getAbbreviations() {
        return abbreviations.clone();
    }

    /**
     * Returns all enum constants as a list of providers (for registry initialization).
     *
     * @return list of all built-in providers
     */
    static List<StateDataProvider> allProviders() {
        return Arrays.asList(values());
    }
}
