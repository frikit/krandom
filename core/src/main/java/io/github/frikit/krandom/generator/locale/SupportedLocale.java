/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.locale;

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

    // ── Tier 1: Native datasets (20 original locales) ───────────────────
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
    RU_RU("ru", "RU"),
    KO_KR("ko", "KR"),
    TR_TR("tr", "TR"),
    SV_SE("sv", "SE"),
    NB_NO("nb", "NO"),
    CS_CZ("cs", "CZ"),
    AR_SA("ar", "SA"),
    HI_IN("hi", "IN"),

    // ── Tier 2: Native datasets (15 new languages) ───────────────────
    DA_DK("da", "DK"),
    FI_FI("fi", "FI"),
    HU_HU("hu", "HU"),
    RO_RO("ro", "RO"),
    SK_SK("sk", "SK"),
    UK_UA("uk", "UA"),
    BG_BG("bg", "BG"),
    HR_HR("hr", "HR"),
    EL_GR("el", "GR"),
    TH_TH("th", "TH"),
    VI_VN("vi", "VN"),
    ID_ID("id", "ID"),
    MS_MY("ms", "MY"),
    HE_IL("he", "IL"),
    CA_ES("ca", "ES"),

    // ── Tier 3: Curated fallback datasets (15 regional variants) ─────
    EN_CA("en", "CA", "en_US", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "en_US", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    EN_NZ("en", "NZ", "en_AU", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "en_AU", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    EN_IE("en", "IE", "en_GB", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "en_GB", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    EN_IN("en", "IN", "en_US", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "en_US", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    EN_ZA("en", "ZA", "en_GB", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "en_GB", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    FR_CA("fr", "CA", "fr_FR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "fr_FR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    FR_BE("fr", "BE", "fr_FR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "fr_FR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    FR_CH("fr", "CH", "fr_FR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "fr_FR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    DE_AT("de", "AT", "de_DE", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "de_DE", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    DE_CH("de", "CH", "de_DE", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "de_DE", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    ES_MX("es", "MX", "es_ES", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "es_ES", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    ES_AR("es", "AR", "es_ES", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "es_ES", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    PT_PT("pt", "PT", "pt_BR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "pt_BR", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    NL_BE("nl", "BE", "nl_NL", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "nl_NL", LocaleDataQualityTier.CURATED_FALLBACK_DATASET),
    ZH_TW("zh", "TW", "zh_CN", LocaleDataQualityTier.CURATED_FALLBACK_DATASET,
          "zh_CN", LocaleDataQualityTier.CURATED_FALLBACK_DATASET);

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
        return resolveFallbackLocale(resourcePrefix, resourceDataTier);
    }

    public Optional<SupportedLocale> professionFallbackLocale() {
        return resolveFallbackLocale(professionDatasetPrefix, professionDataTier);
    }

    static Optional<SupportedLocale> resolveFallbackLocale(String resourcePrefix, LocaleDataQualityTier dataTier) {
        if (!dataTier.isFallback()) {
            return Optional.empty();
        }
        return fromCanonicalResourcePrefix(resourcePrefix);
    }

    private static Optional<SupportedLocale> fromCanonicalResourcePrefix(String resourcePrefix) {
        return Arrays.stream(values())
                     .filter(value -> value.canonicalResourcePrefix.equals(resourcePrefix))
                     .findFirst();
    }
}
