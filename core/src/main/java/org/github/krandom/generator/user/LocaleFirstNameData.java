/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Built-in locale-specific first-name data loaded from classpath resource files.
 *
 * <p>Each constant implements {@link FirstNameDataProvider} and is automatically seeded into
 * {@link FirstNameDataRegistry} at class-load time. Name lists are loaded from
 * {@code krandom/names/<locale>_first_male.txt} and {@code krandom/names/<locale>_first_female.txt}.
 *
 * <p>Custom locales or overrides can be registered via
 * {@link FirstNameDataRegistry#register(FirstNameDataProvider)}.
 */
public enum LocaleFirstNameData implements FirstNameDataProvider {

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
    private final String[] maleNames;
    private final String[] femaleNames;

    LocaleFirstNameData(Locale locale, String resourcePrefix) {
        this.locale      = locale;
        this.maleNames   = NameResourceLoader.load("krandom/names/" + resourcePrefix + "_first_male.txt");
        this.femaleNames = NameResourceLoader.load("krandom/names/" + resourcePrefix + "_first_female.txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getMaleFirstNames() {
        return maleNames.clone();
    }

    @Override
    public String[] getFemaleFirstNames() {
        return femaleNames.clone();
    }
}
