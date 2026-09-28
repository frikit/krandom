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
 * Built-in company buzzword vocabulary backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/company_buzzwords/<list>/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInCompanyBuzzwordDataProvider implements CompanyBuzzwordDataProvider {

    private final Locale locale;
    private final List<String> verbs;
    private final List<String> adjectives;
    private final List<String> nouns;
    private final String format;

    BuiltInCompanyBuzzwordDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInCompanyBuzzwordDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.verbs = List.of(LocaleTextResourceLoader.load("krandom/company_buzzwords/verbs/" + resourcePrefix + ".txt"));
        this.adjectives = List.of(LocaleTextResourceLoader.load("krandom/company_buzzwords/adjectives/" + resourcePrefix + ".txt"));
        this.nouns = List.of(LocaleTextResourceLoader.load("krandom/company_buzzwords/nouns/" + resourcePrefix + ".txt"));
        String[] formats = LocaleTextResourceLoader.load("krandom/company_buzzwords/formats/" + resourcePrefix + ".txt");
        this.format = formats[0];
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getVerbs() {
        return verbs;
    }

    @Override
    public List<String> getAdjectives() {
        return adjectives;
    }

    @Override
    public List<String> getNouns() {
        return nouns;
    }

    @Override
    public String getFormat() {
        return format;
    }
}
