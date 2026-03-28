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
        return switch (supportedLocale.locale().getLanguage()) {
            case "fr" -> new String[] { "Homme", "Femme" };
            case "de" -> new String[] { "Männlich", "Weiblich" };
            case "ja" -> new String[] { "男性", "女性" };
            case "es" -> new String[] { "Hombre", "Mujer" };
            case "it" -> new String[] { "Maschio", "Femmina" };
            case "pt" -> new String[] { "Masculino", "Feminino" };
            case "zh" -> new String[] { "男", "女" };
            case "nl" -> new String[] { "Man", "Vrouw" };
            case "pl" -> new String[] { "Mężczyzna", "Kobieta" };
            case "ru" -> new String[] { "Мужчина", "Женщина" };
            case "ko" -> new String[] { "남성", "여성" };
            case "tr" -> new String[] { "Erkek", "Kadın" };
            case "sv" -> new String[] { "Man", "Kvinna" };
            case "nb" -> new String[] { "Mann", "Kvinne" };
            case "cs" -> new String[] { "Muž", "Žena" };
            case "ar" -> new String[] { "ذكر", "أنثى" };
            case "hi" -> new String[] { "पुरुष", "महिला" };
            default -> new String[] { "Male", "Female" };
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
