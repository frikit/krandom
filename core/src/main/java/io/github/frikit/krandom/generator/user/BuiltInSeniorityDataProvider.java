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
 * Built-in job seniority labels backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/seniorities/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInSeniorityDataProvider implements SeniorityDataProvider {

    private final Locale locale;
    private final List<String> seniorities;

    BuiltInSeniorityDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInSeniorityDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.seniorities = List.of(LocaleTextResourceLoader.load("krandom/seniorities/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getSeniorities() {
        return seniorities;
    }
}
