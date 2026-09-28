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
 * Built-in company-name vocabulary backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/company_names/<list>/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInCompanyNameDataProvider implements CompanyNameDataProvider {

    private final Locale locale;
    private final List<String> prefixes;
    private final List<String> nouns;
    private final List<String> suffixes;
    private final String nameFormat;
    private final String legalNameFormat;

    BuiltInCompanyNameDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInCompanyNameDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.prefixes = List.of(LocaleTextResourceLoader.load("krandom/company_names/prefixes/" + resourcePrefix + ".txt"));
        this.nouns = List.of(LocaleTextResourceLoader.load("krandom/company_names/nouns/" + resourcePrefix + ".txt"));
        this.suffixes = List.of(LocaleTextResourceLoader.load("krandom/company_names/suffixes/" + resourcePrefix + ".txt"));
        String[] formats = LocaleTextResourceLoader.load("krandom/company_names/formats/" + resourcePrefix + ".txt");
        this.nameFormat = formats[0];
        this.legalNameFormat = formats[1];
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getPrefixes() {
        return prefixes;
    }

    @Override
    public List<String> getNouns() {
        return nouns;
    }

    @Override
    public List<String> getSuffixes() {
        return suffixes;
    }

    @Override
    public String getNameFormat() {
        return nameFormat;
    }

    @Override
    public String getLegalNameFormat() {
        return legalNameFormat;
    }
}
