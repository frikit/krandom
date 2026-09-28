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
 * Built-in employment type values backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/job_types/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInJobTypeDataProvider implements JobTypeDataProvider {

    private final Locale locale;
    private final List<String> jobTypes;

    BuiltInJobTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInJobTypeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.jobTypes = List.of(LocaleTextResourceLoader.load("krandom/job_types/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getJobTypes() {
        return jobTypes;
    }
}
