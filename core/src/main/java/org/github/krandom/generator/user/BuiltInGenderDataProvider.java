/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in gender-label provider for supported locales.
 */
final class BuiltInGenderDataProvider implements GenderDataProvider {

    private final Locale locale;
    private final String maleLabel;
    private final String femaleLabel;

    BuiltInGenderDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String[] labels = labelsFor(supportedLocale);
        this.maleLabel = labels[0];
        this.femaleLabel = labels[1];
    }

    private static String[] labelsFor(SupportedLocale supportedLocale) {
        return switch (supportedLocale) {
            case EN_US, EN_GB, EN_AU -> new String[] { "Male", "Female" };
            case FR_FR -> new String[] { "Homme", "Femme" };
            case DE_DE -> new String[] { "Männlich", "Weiblich" };
            case JA_JP -> new String[] { "男性", "女性" };
            case ES_ES -> new String[] { "Hombre", "Mujer" };
            case IT_IT -> new String[] { "Maschio", "Femmina" };
            case PT_BR -> new String[] { "Masculino", "Feminino" };
            case ZH_CN -> new String[] { "男", "女" };
        };
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
