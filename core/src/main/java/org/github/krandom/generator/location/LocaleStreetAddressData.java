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
 * Built-in locale street-address data loaded from classpath resources.
 */
public enum LocaleStreetAddressData implements StreetAddressDataProvider {

    EN_US(Locale.of("en", "US"), "en_US"),
    EN_GB(Locale.of("en", "GB"), "en_GB"),
    EN_AU(Locale.of("en", "AU"), "en_AU"),
    FR_FR(Locale.of("fr", "FR"), "fr_FR"),
    DE_DE(Locale.of("de", "DE"), "de_DE"),
    JA_JP(Locale.of("ja", "JP"), "ja_JP"),
    ES_ES(Locale.of("es", "ES"), "es_ES"),
    IT_IT(Locale.of("it", "IT"), "it_IT"),
    PT_BR(Locale.of("pt", "BR"), "pt_BR"),
    ZH_CN(Locale.of("zh", "CN"), "zh_CN");

    private final Locale locale;
    private final String[] streetNames;
    private final String[] streetTypesShort;
    private final String[] streetTypesLong;

    LocaleStreetAddressData(Locale locale, String resourcePrefix) {
        this.locale = locale;
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

    static List<StreetAddressDataProvider> allProviders() {
        return Arrays.asList(values());
    }
}
