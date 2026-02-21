/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Locale-specific honorific title data.
 * 
 * <p>Fallback: exact match → language match → EN_US default.
 */
public enum LocaleTitleData {

    EN_US(
        new Locale("en", "US"),
        new String[] { "Mr.", "Mrs.", "Ms.", "Miss", "Dr.", "Prof.", "Rev.", "Hon.", "Mx." }
    ),

    EN_GB(
        new Locale("en", "GB"),
        new String[] { "Mr", "Mrs", "Ms", "Miss", "Dr", "Prof", "Rev", "Sir", "Dame", "Lord", "Lady", "Mx" }
    ),

    EN_AU(
        new Locale("en", "AU"),
        new String[] { "Mr", "Mrs", "Ms", "Miss", "Dr", "Prof", "Rev", "Mx" }
    ),

    FR_FR(
        new Locale("fr", "FR"),
        new String[] { "M.", "Mme", "Mlle", "Dr", "Pr", "Me", "Mgr" }
    ),

    DE_DE(
        new Locale("de", "DE"),
        new String[] { "Herr", "Frau", "Dr.", "Prof.", "Dr. med.", "Dr. jur.", "Dipl.-Ing." }
    ),

    JA_JP(
        new Locale("ja", "JP"),
        new String[] { "さん", "様", "殿", "君", "ちゃん", "先生", "博士" }
    ),

    ES_ES(
        new Locale("es", "ES"),
        new String[] { "Sr.", "Sra.", "Srta.", "Dr.", "Dra.", "Prof.", "Don", "Doña" }
    ),

    IT_IT(
        new Locale("it", "IT"),
        new String[] { "Sig.", "Sig.ra", "Sig.na", "Dott.", "Dott.ssa", "Prof.", "Avv." }
    ),

    PT_BR(
        new Locale("pt", "BR"),
        new String[] { "Sr.", "Sra.", "Srta.", "Dr.", "Dra.", "Prof.", "Profa." }
    ),

    ZH_CN(
        new Locale("zh", "CN"),
        new String[] { "先生", "女士", "小姐", "博士", "教授", "老师" }
    );

    private final Locale locale;
    private final String[] titles;

    LocaleTitleData(Locale locale, String[] titles) {
        this.locale = locale;
        this.titles = titles;
    }

    public Locale getLocale() {
        return locale;
    }

    public String[] getTitles() {
        return titles.clone();
    }

    public static LocaleTitleData forLocale(Locale locale) {
        if (locale == null) {
            return EN_US;
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();

        if (!country.isEmpty()) {
            for (LocaleTitleData data : values()) {
                if (data.locale.getLanguage().equals(language)
                        && data.locale.getCountry().equals(country)) {
                    return data;
                }
            }
        }

        for (LocaleTitleData data : values()) {
            if (data.locale.getLanguage().equals(language)) {
                return data;
            }
        }

        return EN_US;
    }

    public static boolean isSupported(Locale locale) {
        if (locale == null) return false;

        String language = locale.getLanguage();
        String country = locale.getCountry();

        for (LocaleTitleData data : values()) {
            if (data.locale.getLanguage().equals(language)
                    && (country.isEmpty() || data.locale.getCountry().equals(country))) {
                return true;
            }
        }

        return false;
    }
}
