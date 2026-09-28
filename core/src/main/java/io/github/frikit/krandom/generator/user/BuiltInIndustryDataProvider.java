/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.List;
import java.util.Locale;

/**
 * Built-in industry names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/industries/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInIndustryDataProvider implements IndustryDataProvider {

    private final Locale locale;
    private final List<String> industries;

    BuiltInIndustryDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInIndustryDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.industries = List.of(LocaleTextResourceLoader.load("krandom/industries/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getIndustries() {
        return industries;
    }
}
