/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in gender-label provider backed by classpath gender resources.
 *
 * <p>Labels are loaded from {@code krandom/genders/<locale>.txt}, which holds exactly two lines:
 * the male label on the first line and the female label on the second.
 */
final class BuiltInGenderDataProvider implements GenderDataProvider {

    private final Locale locale;
    private final String maleLabel;
    private final String femaleLabel;

    BuiltInGenderDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String[] labels = LocaleTextResourceLoader.load("krandom/genders/" + supportedLocale.resourcePrefix() + ".txt");
        this.maleLabel = labels[0];
        this.femaleLabel = labels[1];
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getMaleLabel() {
        return maleLabel;
    }

    @Override
    public String getFemaleLabel() {
        return femaleLabel;
    }
}
