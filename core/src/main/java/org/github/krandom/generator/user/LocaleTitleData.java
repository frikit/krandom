/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Built-in locale-specific honorific title data.
 *
 * <p>Each constant implements {@link TitleDataProvider} and is automatically seeded into
 * {@link TitleDataRegistry} at class-load time. Custom locales or overrides can be registered
 * via {@link TitleDataRegistry#register(TitleDataProvider)}.
 */
public enum LocaleTitleData implements TitleDataProvider {

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

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getTitles() {
        return titles.clone();
    }
}
