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
 * Built-in job field names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/job_fields/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInJobFieldDataProvider implements JobFieldDataProvider {

    private final Locale locale;
    private final List<String> jobFields;

    BuiltInJobFieldDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInJobFieldDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.jobFields = List.of(LocaleTextResourceLoader.load("krandom/job_fields/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getJobFields() {
        return jobFields;
    }
}
