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
 * Built-in marital status values backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/marital_statuses/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInMaritalStatusDataProvider implements MaritalStatusDataProvider {

    private final Locale locale;
    private final List<String> maritalStatuses;

    BuiltInMaritalStatusDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInMaritalStatusDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.maritalStatuses = List.of(LocaleTextResourceLoader.load("krandom/marital_statuses/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getMaritalStatuses() {
        return maritalStatuses;
    }
}
