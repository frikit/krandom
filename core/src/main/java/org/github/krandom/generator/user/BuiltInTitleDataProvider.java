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

    private final Locale   locale;
    private final String[] titles;

    BuiltInTitleDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.titles = titlesFor(supportedLocale);
    }

    private static String[] titlesFor(SupportedLocale supportedLocale) {
        if (supportedLocale == SupportedLocale.EN_GB) {
            return new String[] { "Mr", "Mrs", "Ms", "Miss", "Dr", "Prof", "Rev", "Sir", "Dame", "Lord", "Lady", "Mx" };
        }
        if (supportedLocale == SupportedLocale.EN_AU) {
            return new String[] { "Mr", "Mrs", "Ms", "Miss", "Dr", "Prof", "Rev", "Mx" };
        }
        return switch (supportedLocale.locale().getLanguage()) {
            case "fr" -> new String[] { "M.", "Mme", "Mlle", "Dr", "Pr", "Me", "Mgr" };
            case "de" -> new String[] { "Herr", "Frau", "Dr.", "Prof.", "Dr. med.", "Dr. jur.", "Dipl.-Ing." };
            case "ja" -> new String[] { "さん", "様", "殿", "君", "ちゃん", "先生", "博士" };
            case "es" -> new String[] { "Sr.", "Sra.", "Srta.", "Dr.", "Dra.", "Prof.", "Don", "Doña" };
            case "it" -> new String[] { "Sig.", "Sig.ra", "Sig.na", "Dott.", "Dott.ssa", "Prof.", "Avv." };
            case "pt" -> new String[] { "Sr.", "Sra.", "Srta.", "Dr.", "Dra.", "Prof.", "Profa." };
            case "zh" -> new String[] { "先生", "女士", "小姐", "博士", "教授", "老师" };
            case "nl" -> new String[] { "Dhr.", "Mevr.", "Dr.", "Prof.", "Ir." };
            case "pl" -> new String[] { "Pan", "Pani", "Dr", "Prof." };
            case "ru" -> new String[] { "Г-н", "Г-жа", "Д-р", "Проф." };
            case "ko" -> new String[] { "씨", "님", "박사", "교수" };
            case "tr" -> new String[] { "Bay", "Bayan", "Dr.", "Prof." };
            case "sv" -> new String[] { "Herr", "Fru", "Fröken", "Dr.", "Prof." };
            case "nb" -> new String[] { "Hr.", "Fru", "Frøken", "Dr.", "Prof." };
            case "cs" -> new String[] { "Pan", "Paní", "Dr.", "Prof." };
            case "ar" -> new String[] { "السيد", "السيدة", "د.", "أ." };
            case "hi" -> new String[] { "श्री", "श्रीमती", "डॉ.", "प्रो." };
            default -> new String[] { "Mr.", "Mrs.", "Ms.", "Miss", "Dr.", "Prof.", "Rev.", "Hon.", "Mx." };
        };
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
