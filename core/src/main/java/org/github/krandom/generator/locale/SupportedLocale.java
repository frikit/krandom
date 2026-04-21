/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.locale;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    ZH_CN("zh", "CN"),
    NL_NL("nl", "NL"),
    PL_PL("pl", "PL"),
    RU_RU("ru", "RU", "de_DE", "de_DE"),
    KO_KR("ko", "KR", "ja_JP", "ja_JP"),
    TR_TR("tr", "TR"),
    SV_SE("sv", "SE", "en_GB", "en_GB"),
    NB_NO("nb", "NO", "en_GB", "en_GB"),
    CS_CZ("cs", "CZ", "de_DE", "de_DE"),
    AR_SA("ar", "SA", "en_US", "en_US"),
    HI_IN("hi", "IN", "en_GB", "en_US");

    private final Locale locale;
    private final String canonicalResourcePrefix;
    private final String resourcePrefix;
    private final LocaleDataQualityTier resourceDataTier;
    private final String professionDatasetPrefix;
    private final LocaleDataQualityTier professionDataTier;

    SupportedLocale(String language, String country) {
        this(language,
             country,
             language + "_" + country,
             LocaleDataQualityTier.NATIVE_DATASET,
             language + "_" + country,
             LocaleDataQualityTier.NATIVE_DATASET);
    }

    SupportedLocale(String language, String country, String resourcePrefix, String professionDatasetPrefix) {
        this(language,
             country,
             resourcePrefix,
             LocaleDataQualityTier.ALIAS_FALLBACK_DATASET,
             professionDatasetPrefix,
             LocaleDataQualityTier.CURATED_FALLBACK_DATASET);
    }

    SupportedLocale(String language,
                    String country,
                    String resourcePrefix,
                    LocaleDataQualityTier resourceDataTier,
                    String professionDatasetPrefix,
                    LocaleDataQualityTier professionDataTier) {
        this.locale = Locale.of(language, country);
        this.canonicalResourcePrefix = language + "_" + country;
        this.resourcePrefix = resourcePrefix;
        this.resourceDataTier = resourceDataTier;
        this.professionDatasetPrefix = professionDatasetPrefix;
        this.professionDataTier = professionDataTier;
    }

    public static List<Locale> locales() {
        return Arrays.stream(values()).map(SupportedLocale::locale).toList();
    }

    public static Optional<SupportedLocale> fromLocale(Locale locale) {
        return Arrays.stream(values()).filter(value -> value.locale.equals(locale)).findFirst();
    }

    public Locale locale() {
        return locale;
    }

    public String canonicalResourcePrefix() {
        return canonicalResourcePrefix;
    }

    public String resourcePrefix() {
        return resourcePrefix;
    }

    /**
     * Quality tier for resource-backed identity and address datasets such as names, cities,
     * states, countries, and street names.
     */
    public LocaleDataQualityTier resourceDataTier() {
        return resourceDataTier;
    }

    /**
     * Quality tier for built-in profession data.
     */
    public LocaleDataQualityTier professionDataTier() {
        return professionDataTier;
    }

    /**
     * Overall built-in locale tier for kRandom's main data providers.
     */
    public LocaleDataQualityTier qualityTier() {
        return LocaleDataQualityTier.max(resourceDataTier, professionDataTier);
    }

    public Optional<SupportedLocale> resourceFallbackLocale() {
        if (!resourceDataTier.isFallback()) {
            return Optional.empty();
        }
        return fromCanonicalResourcePrefix(resourcePrefix);
    }

    public Optional<SupportedLocale> professionFallbackLocale() {
        if (!professionDataTier.isFallback()) {
            return Optional.empty();
        }
        return fromCanonicalResourcePrefix(professionDatasetPrefix);
    }

    private static Optional<SupportedLocale> fromCanonicalResourcePrefix(String resourcePrefix) {
        return Arrays.stream(values())
                     .filter(value -> value.canonicalResourcePrefix.equals(resourcePrefix))
                     .findFirst();
    }
}
