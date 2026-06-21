/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in financial-term names backed by classpath resources.
 *
 * <p>Names are loaded from {@code krandom/financial_terms/<locale>.txt}: one localized term per line.
 * Blank lines and {@code #} comments are ignored.
 */
final class BuiltInFinancialTermDataProvider implements FinancialTermDataProvider {

    private final Locale locale;
    private final List<String> terms;

    BuiltInFinancialTermDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(),
            "krandom/financial_terms/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInFinancialTermDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.terms = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getTerms() {
        return terms;
    }
}
