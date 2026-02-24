/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Built-in locale-specific gender labels.
 *
 * <p>Each constant implements {@link GenderDataProvider} and is automatically seeded into
 * {@link GenderDataRegistry} at class-load time.
 *
 * <p>Custom locales or overrides can be registered via
 * {@link GenderDataRegistry#register(GenderDataProvider)}.
 */
public enum LocaleGenderData implements GenderDataProvider {

    EN_US(Locale.of("en", "US"), "Male",      "Female"),
    EN_GB(Locale.of("en", "GB"), "Male",      "Female"),
    EN_AU(Locale.of("en", "AU"), "Male",      "Female"),
    FR_FR(Locale.of("fr", "FR"), "Homme",     "Femme"),
    DE_DE(Locale.of("de", "DE"), "Männlich",  "Weiblich"),
    JA_JP(Locale.of("ja", "JP"), "男性",       "女性"),
    ES_ES(Locale.of("es", "ES"), "Hombre",    "Mujer"),
    IT_IT(Locale.of("it", "IT"), "Maschio",   "Femmina"),
    PT_BR(Locale.of("pt", "BR"), "Masculino", "Feminino"),
    ZH_CN(Locale.of("zh", "CN"), "男",         "女");

    private final Locale locale;
    private final String maleLabel;
    private final String femaleLabel;

    LocaleGenderData(Locale locale, String maleLabel, String femaleLabel) {
        this.locale      = locale;
        this.maleLabel   = maleLabel;
        this.femaleLabel = femaleLabel;
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
