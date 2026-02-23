/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Built-in locale-specific last-name data loaded from classpath resource files.
 *
 * <p>Each constant implements {@link LastNameDataProvider} and is automatically seeded into
 * {@link LastNameDataRegistry} at class-load time. Name lists are loaded from
 * {@code krandom/names/<locale>_last.txt}.
 *
 * <p>Custom locales or overrides can be registered via
 * {@link LastNameDataRegistry#register(LastNameDataProvider)}.
 */
public enum LocaleLastNameData implements LastNameDataProvider {

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
    private final String[] lastNames;

    LocaleLastNameData(Locale locale, String resourcePrefix) {
        this.locale    = locale;
        this.lastNames = NameResourceLoader.load("krandom/names/" + resourcePrefix + "_last.txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getLastNames() {
        return lastNames.clone();
    }
}
