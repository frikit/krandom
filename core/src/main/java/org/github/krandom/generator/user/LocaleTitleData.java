/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

    private static final Set<String> SUPPORTED_LOCALE_KEYS;
    private static final Set<String> SUPPORTED_LANGUAGES;
    private static final String SUPPORTED_LOCALES_STRING;

    static {
        Set<String> localeKeys = new HashSet<>();
        Set<String> languages = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        
        LocaleTitleData[] values = values();
        for (int i = 0; i < values.length; i++) {
            Locale loc = values[i].locale;
            String lang = loc.getLanguage();
            String country = loc.getCountry();
            
            languages.add(lang);
            if (!country.isEmpty()) {
                localeKeys.add(lang + "_" + country);
            }
            
            sb.append(lang);
            if (!country.isEmpty()) {
                sb.append('_').append(country);
            }
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        
        SUPPORTED_LOCALE_KEYS = Collections.unmodifiableSet(localeKeys);
        SUPPORTED_LANGUAGES = Collections.unmodifiableSet(languages);
        SUPPORTED_LOCALES_STRING = sb.toString();
    }

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

        if (!country.isEmpty()) {
            String key = language + "_" + country;
            if (SUPPORTED_LOCALE_KEYS.contains(key)) {
                return true;
            }
        }

        return SUPPORTED_LANGUAGES.contains(language);
    }

    public static String getSupportedLocalesString() {
        return SUPPORTED_LOCALES_STRING;
    }
}
