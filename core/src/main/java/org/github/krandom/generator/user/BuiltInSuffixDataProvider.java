/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in suffix provider for supported locales.
 */
final class BuiltInSuffixDataProvider implements SuffixDataProvider {

    private final Locale   locale;
    private final String[] suffixes;

    BuiltInSuffixDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.suffixes = suffixesFor(supportedLocale);
    }

    private static String[] suffixesFor(SupportedLocale supportedLocale) {
        return switch (supportedLocale.locale().getLanguage()) {
            case "fr" -> new String[] { "fils", "père", "PhD", "MD" };
            case "de" -> new String[] { "jun.", "sen.", "PhD", "Dr. h.c." };
            case "ja" -> new String[] { "博士", "学士", "修士" };
            case "es" -> new String[] { "Jr.", "Sr.", "II", "III", "PhD", "MD" };
            case "it" -> new String[] { "Jr.", "Sr.", "PhD", "MD" };
            case "pt" -> new String[] { "Filho", "Júnior", "Neto", "PhD", "MD" };
            case "zh" -> new String[] { "博士", "硕士", "学士" };
            case "nl" -> new String[] { "Jr.", "Sr.", "drs.", "PhD" };
            case "pl" -> new String[] { "junior", "senior", "dr", "mgr", "PhD" };
            case "ru" -> new String[] { "мл.", "ст.", "канд. наук", "д-р" };
            case "ko" -> new String[] { "주니어", "시니어", "박사", "석사" };
            case "tr" -> new String[] { "Jr.", "Sr.", "Dr.", "Prof." };
            case "sv" -> new String[] { "d.y.", "d.ä.", "Fil.dr", "MD" };
            case "nb" -> new String[] { "jr.", "sr.", "PhD", "Dr." };
            case "cs" -> new String[] { "ml.", "st.", "Ing.", "PhDr." };
            case "ar" -> new String[] { "الابن", "الأب", "د.", "م." };
            case "hi" -> new String[] { "जूनियर", "सीनियर", "पीएचडी", "एमडी" };
            default -> new String[] { "Jr.", "Sr.", "II", "III", "IV", "Esq.", "PhD", "MD", "JD", "DO", "DDS", "RN", "CPA" };
        };
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getSuffixes() {
        return suffixes.clone();
    }
}
