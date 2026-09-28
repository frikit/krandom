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
 * Built-in company catch-phrase vocabulary backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/company_catch_phrases/<list>/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInCompanyCatchPhraseDataProvider implements CompanyCatchPhraseDataProvider {

    private final Locale locale;
    private final List<String> adjectives;
    private final List<String> nouns;
    private final List<String> taglines;
    private final String format;

    BuiltInCompanyCatchPhraseDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInCompanyCatchPhraseDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.adjectives = List.of(LocaleTextResourceLoader.load("krandom/company_catch_phrases/adjectives/" + resourcePrefix + ".txt"));
        this.nouns = List.of(LocaleTextResourceLoader.load("krandom/company_catch_phrases/nouns/" + resourcePrefix + ".txt"));
        this.taglines = List.of(LocaleTextResourceLoader.load("krandom/company_catch_phrases/taglines/" + resourcePrefix + ".txt"));
        String[] formats = LocaleTextResourceLoader.load("krandom/company_catch_phrases/formats/" + resourcePrefix + ".txt");
        this.format = formats[0];
    }

    @Override
    public Locale getLocale() {
        return locale;
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
    public List<String> getTaglines() {
        return taglines;
    }

    @Override
    public String getFormat() {
        return format;
    }
}
