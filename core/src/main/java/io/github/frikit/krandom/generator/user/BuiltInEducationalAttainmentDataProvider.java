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
 * Built-in educational attainment levels backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/educational_attainments/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInEducationalAttainmentDataProvider implements EducationalAttainmentDataProvider {

    private final Locale locale;
    private final List<String> educationalAttainments;

    BuiltInEducationalAttainmentDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInEducationalAttainmentDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.educationalAttainments = List.of(LocaleTextResourceLoader.load("krandom/educational_attainments/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getEducationalAttainments() {
        return educationalAttainments;
    }
}
