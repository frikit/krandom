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
 * Built-in job position titles backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/positions/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInPositionDataProvider implements PositionDataProvider {

    private final Locale locale;
    private final List<String> positions;

    BuiltInPositionDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInPositionDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.positions = List.of(LocaleTextResourceLoader.load("krandom/positions/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getPositions() {
        return positions;
    }
}
