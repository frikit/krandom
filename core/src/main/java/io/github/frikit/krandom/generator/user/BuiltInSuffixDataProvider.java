/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in suffix provider backed by classpath suffix resources.
 *
 * <p>Suffixes are loaded from {@code krandom/suffixes/<locale>.txt} (one suffix per line).
 */
final class BuiltInSuffixDataProvider implements SuffixDataProvider {

    private final Locale   locale;
    private final String[] suffixes;

    BuiltInSuffixDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.suffixes = LocaleTextResourceLoader.load("krandom/suffixes/" + supportedLocale.resourcePrefix() + ".txt");
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
