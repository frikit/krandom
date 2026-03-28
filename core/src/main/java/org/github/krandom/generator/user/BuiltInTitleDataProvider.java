/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in title provider for supported locales.
 */
final class BuiltInTitleDataProvider implements TitleDataProvider {

    private final Locale locale;
    private final String[] titles;

    BuiltInTitleDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.titles = titlesFor(supportedLocale);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getTitles() {
        return titles.clone();
    }

    private static String[] titlesFor(SupportedLocale supportedLocale) {
        return switch (supportedLocale) {
            case EN_US -> new String[]{"Mr.", "Mrs.", "Ms.", "Miss", "Dr.", "Prof.", "Rev.", "Hon.", "Mx."};
            case EN_GB -> new String[]{"Mr", "Mrs", "Ms", "Miss", "Dr", "Prof", "Rev", "Sir", "Dame", "Lord", "Lady", "Mx"};
            case EN_AU -> new String[]{"Mr", "Mrs", "Ms", "Miss", "Dr", "Prof", "Rev", "Mx"};
            case FR_FR -> new String[]{"M.", "Mme", "Mlle", "Dr", "Pr", "Me", "Mgr"};
            case DE_DE -> new String[]{"Herr", "Frau", "Dr.", "Prof.", "Dr. med.", "Dr. jur.", "Dipl.-Ing."};
            case JA_JP -> new String[]{"さん", "様", "殿", "君", "ちゃん", "先生", "博士"};
            case ES_ES -> new String[]{"Sr.", "Sra.", "Srta.", "Dr.", "Dra.", "Prof.", "Don", "Doña"};
            case IT_IT -> new String[]{"Sig.", "Sig.ra", "Sig.na", "Dott.", "Dott.ssa", "Prof.", "Avv."};
            case PT_BR -> new String[]{"Sr.", "Sra.", "Srta.", "Dr.", "Dra.", "Prof.", "Profa."};
            case ZH_CN -> new String[]{"先生", "女士", "小姐", "博士", "教授", "老师"};
        };
    }
}
