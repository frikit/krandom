/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Built-in locale-specific name-suffix data (e.g. "Jr.", "Sr.", "PhD").
 *
 * <p>Each constant implements {@link SuffixDataProvider} and is automatically seeded into
 * {@link SuffixDataRegistry} at class-load time. Custom locales or overrides can be registered
 * via {@link SuffixDataRegistry#register(SuffixDataProvider)}.
 */
public enum LocaleSuffixData implements SuffixDataProvider {

    EN_US(
        Locale.of("en", "US"),
        new String[] { "Jr.", "Sr.", "II", "III", "IV", "Esq.", "PhD", "MD", "JD", "DO", "DDS", "RN", "CPA" }
    ),

    EN_GB(
        Locale.of("en", "GB"),
        new String[] { "Jr", "Sr", "Jnr", "Snr", "Esq", "PhD", "MD", "OBE", "MBE", "CBE" }
    ),

    EN_AU(
        Locale.of("en", "AU"),
        new String[] { "Jr", "Sr", "Esq", "PhD", "MD", "OAM", "AM" }
    ),

    FR_FR(
        Locale.of("fr", "FR"),
        new String[] { "fils", "père", "PhD", "MD" }
    ),

    DE_DE(
        Locale.of("de", "DE"),
        new String[] { "jun.", "sen.", "PhD", "Dr. h.c." }
    ),

    JA_JP(
        Locale.of("ja", "JP"),
        new String[] { "博士", "学士", "修士" }
    ),

    ES_ES(
        Locale.of("es", "ES"),
        new String[] { "Jr.", "Sr.", "II", "III", "PhD", "MD" }
    ),

    IT_IT(
        Locale.of("it", "IT"),
        new String[] { "Jr.", "Sr.", "PhD", "MD" }
    ),

    PT_BR(
        Locale.of("pt", "BR"),
        new String[] { "Filho", "Júnior", "Neto", "PhD", "MD" }
    ),

    ZH_CN(
        Locale.of("zh", "CN"),
        new String[] { "博士", "硕士", "学士" }
    );

    private final Locale locale;
    private final String[] suffixes;

    LocaleSuffixData(Locale locale, String[] suffixes) {
        this.locale   = locale;
        this.suffixes = suffixes;
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
