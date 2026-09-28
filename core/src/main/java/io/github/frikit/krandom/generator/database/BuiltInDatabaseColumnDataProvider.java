/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.database;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in database column names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/database_columns/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInDatabaseColumnDataProvider implements DatabaseColumnDataProvider {

    private final Locale locale;
    private final List<String> columns;

    BuiltInDatabaseColumnDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInDatabaseColumnDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.columns = List.of(LocaleTextResourceLoader.load("krandom/database_columns/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getColumns() {
        return columns;
    }
}
