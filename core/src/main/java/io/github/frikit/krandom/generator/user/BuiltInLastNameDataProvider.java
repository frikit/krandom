/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in last-name provider backed by classpath name resources.
 */
final class BuiltInLastNameDataProvider implements LastNameDataProvider {

    private final Locale   locale;
    private final String[] lastNames;

    BuiltInLastNameDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        this.lastNames = NameResourceLoader.load("krandom/names/last/" + resourcePrefix + ".txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getLastNames() {
        return lastNames.clone();
    }
}
