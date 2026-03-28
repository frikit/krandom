/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.locale.SupportedLocale;

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
        this.maleNames = NameResourceLoader.load("krandom/names/" + resourcePrefix + "_first_male.txt");
        this.femaleNames = NameResourceLoader.load("krandom/names/" + resourcePrefix + "_first_female.txt");
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
