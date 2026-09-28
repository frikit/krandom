/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in text vocabulary words backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/text/words/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInTextWordDataProvider implements TextWordDataProvider {

    private final Locale locale;
    private final List<String> words;

    BuiltInTextWordDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInTextWordDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.words = List.of(LocaleTextResourceLoader.load("krandom/text/words/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getWords() {
        return words;
    }
}
