/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in first-name provider backed by classpath name resources.
 */
final class BuiltInFirstNameDataProvider implements FirstNameDataProvider {

    private final Locale   locale;
    private final String[] maleNames;
    private final String[] femaleNames;

    BuiltInFirstNameDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        this.maleNames = LocaleTextResourceLoader.load("krandom/names/first_male/" + resourcePrefix + ".txt");
        this.femaleNames = LocaleTextResourceLoader.load("krandom/names/first_female/" + resourcePrefix + ".txt");
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getMaleFirstNames() {
        return maleNames.clone();
    }

    @Override
    public String[] getFemaleFirstNames() {
        return femaleNames.clone();
    }
}
