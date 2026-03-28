/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.locale;

import java.util.List;
import java.util.Locale;
import java.util.Arrays;

/**
 * Central catalog of built-in locales supported across locale-aware data providers.
 *
 * <p>Use this enum as the single source of truth for supported locale expansion.
 * When adding a new constant here, locale-aware providers and registries are expected
 * to add matching data; coverage tests enforce that contract.
 */
public enum SupportedLocale {

    EN_US("en", "US"),
    EN_GB("en", "GB"),
    EN_AU("en", "AU"),
    FR_FR("fr", "FR"),
    DE_DE("de", "DE"),
    JA_JP("ja", "JP"),
    ES_ES("es", "ES"),
    IT_IT("it", "IT"),
    PT_BR("pt", "BR"),
    ZH_CN("zh", "CN");

    private final Locale locale;
    private final String resourcePrefix;

    SupportedLocale(String language, String country) {
        this.locale = Locale.of(language, country);
        this.resourcePrefix = language + "_" + country;
    }

    public Locale locale() {
        return locale;
    }

    public String resourcePrefix() {
        return resourcePrefix;
    }

    public static List<Locale> locales() {
        return Arrays.stream(values()).map(SupportedLocale::locale).toList();
    }
}
