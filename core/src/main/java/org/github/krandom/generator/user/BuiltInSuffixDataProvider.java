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
        return switch (supportedLocale) {
            case EN_US -> new String[] { "Jr.", "Sr.", "II", "III", "IV", "Esq.", "PhD", "MD", "JD", "DO", "DDS", "RN", "CPA" };
            case EN_GB -> new String[] { "Jr", "Sr", "Jnr", "Snr", "Esq", "PhD", "MD", "OBE", "MBE", "CBE" };
            case EN_AU -> new String[] { "Jr", "Sr", "Esq", "PhD", "MD", "OAM", "AM" };
            case FR_FR -> new String[] { "fils", "père", "PhD", "MD" };
            case DE_DE -> new String[] { "jun.", "sen.", "PhD", "Dr. h.c." };
            case JA_JP -> new String[] { "博士", "学士", "修士" };
            case ES_ES -> new String[] { "Jr.", "Sr.", "II", "III", "PhD", "MD" };
            case IT_IT -> new String[] { "Jr.", "Sr.", "PhD", "MD" };
            case PT_BR -> new String[] { "Filho", "Júnior", "Neto", "PhD", "MD" };
            case ZH_CN -> new String[] { "博士", "硕士", "学士" };
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
