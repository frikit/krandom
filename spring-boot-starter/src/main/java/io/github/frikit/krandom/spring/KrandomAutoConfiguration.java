/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.github.frikit.krandom.generator.GenerationRecipe;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Base64;
import java.util.IllformedLocaleException;
import java.util.Locale;

/**
 * Auto-configuration for krandom.
 *
 * <p>Registers a {@link GeneratorConfig}, {@link ProviderHub}, and
 * {@link KrandomObjectFakerFactory} backed by {@code krandom.*} application properties.
 * All beans are {@code @ConditionalOnMissingBean} so users can override any of them.
 *
 * <p>Usage in {@code application.properties}:
 * <pre>
 *   krandom.seed=42
 *   krandom.locale=de-DE
 *   krandom.object-max-depth=3
 *   krandom.object-null-probability=0.1
 * </pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(KrandomProperties.class)
public class KrandomAutoConfiguration {

    /** Creates the auto-configuration. */
    public KrandomAutoConfiguration() {
    }

    /**
     * Creates the generator configuration from the bound application properties.
     *
     * @param properties bound krandom properties
     * @return the configured generator settings
     */
    @Bean
    @ConditionalOnMissingBean
    public GeneratorConfig generatorConfig(KrandomProperties properties) {
        GeneratorConfig.Builder builder;
        GeneratorConfig defaults = GeneratorConfig.defaults();

        if (properties.getRecipe() != null && !properties.getRecipe().isBlank()) {
            if (properties.getSeed() != null
                || (properties.getLocale() != null && !properties.getLocale().isBlank())) {
                throw new IllegalArgumentException(
                    "Configure krandom.recipe or the individual krandom.seed/krandom.locale "
                        + "properties, not both: the recipe already carries seed and locale");
            }
            builder = parseRecipe(properties.getRecipe()).toGeneratorConfig().toBuilder();
        } else {
            builder = GeneratorConfig.builder();
            if (properties.getSeed() != null) {
                builder.seed(properties.getSeed());
            }
            if (properties.getLocale() != null && !properties.getLocale().isBlank()) {
                builder.locale(parseLocale(properties.getLocale()));
            }
        }

        if (properties.getClock() != null && !properties.getClock().isBlank()) {
            builder.clock(parseClock(properties.getClock(), properties.getClockZone()));
        } else if (properties.getClockZone() != null && !properties.getClockZone().isBlank()) {
            throw new IllegalArgumentException(
                "krandom.clock-zone requires krandom.clock to be set");
        }

        if (properties.getPaymentCardSafetyPolicy() != null) {
            builder.paymentCardSafetyPolicy(properties.getPaymentCardSafetyPolicy());
        }
        if (properties.getPhoneNumberSafetyPolicy() != null) {
            builder.phoneNumberSafetyPolicy(properties.getPhoneNumberSafetyPolicy());
        }
        if (properties.getNationalIdSafetyPolicy() != null) {
            builder.nationalIdSafetyPolicy(properties.getNationalIdSafetyPolicy());
        }
        if (properties.getBankingSafetyPolicy() != null) {
            builder.bankingSafetyPolicy(properties.getBankingSafetyPolicy());
        }
        if (properties.getSecuritiesIdentifierSafetyPolicy() != null) {
            builder.securitiesIdentifierSafetyPolicy(properties.getSecuritiesIdentifierSafetyPolicy());
        }
        if (properties.getCryptoAddressSafetyPolicy() != null) {
            builder.cryptoAddressSafetyPolicy(properties.getCryptoAddressSafetyPolicy());
        }
        if (properties.getBusinessTaxIdentifierSafetyPolicy() != null) {
            builder.businessTaxIdentifierSafetyPolicy(properties.getBusinessTaxIdentifierSafetyPolicy());
        }
        if (properties.getIdentityDocumentSafetyPolicy() != null) {
            builder.identityDocumentSafetyPolicy(properties.getIdentityDocumentSafetyPolicy());
        }
        if (properties.getObjectConstructionPolicy() != null) {
            builder.objectConstructionPolicy(properties.getObjectConstructionPolicy());
        }

        if (properties.getObjectMaxDepth() != null) {
            builder.objectMaxDepth(properties.getObjectMaxDepth());
        }

        if (properties.getObjectNullProbability() != null) {
            builder.objectNullProbability(properties.getObjectNullProbability());
        }

        if (properties.getMinStringLength() != null || properties.getMaxStringLength() != null) {
            int min = properties.getMinStringLength() != null
                      ? properties.getMinStringLength()
                      : defaults.getMinStringLength();
            int max = properties.getMaxStringLength() != null
                      ? properties.getMaxStringLength()
                      : defaults.getMaxStringLength();
            builder.stringLength(min, max);
        }

        if (properties.getMinCollectionSize() != null || properties.getMaxCollectionSize() != null) {
            int min = properties.getMinCollectionSize() != null
                      ? properties.getMinCollectionSize()
                      : defaults.getMinCollectionSize();
            int max = properties.getMaxCollectionSize() != null
                      ? properties.getMaxCollectionSize()
                      : defaults.getMaxCollectionSize();
            builder.collectionSize(min, max);
        }

        return builder.build();
    }

    private static GenerationRecipe parseRecipe(String value) {
        try {
            String serialized = value.startsWith("base64:")
                ? new String(Base64.getUrlDecoder().decode(value.substring("base64:".length())),
                             StandardCharsets.UTF_8)
                : value.replace("\\n", "\n");
            return GenerationRecipe.parse(serialized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid krandom.recipe: " + e.getMessage(), e);
        }
    }

    private static Clock parseClock(String instant, String zone) {
        try {
            ZoneId zoneId = zone != null && !zone.isBlank() ? ZoneId.of(zone) : ZoneId.of("UTC");
            return Clock.fixed(java.time.Instant.parse(instant.trim()), zoneId);
        } catch (java.time.DateTimeException e) {
            throw new IllegalArgumentException(
                "Invalid krandom.clock/krandom.clock-zone: " + e.getMessage(), e);
        }
    }

    private static Locale parseLocale(String tag) {
        String normalized = tag.trim().replace('_', '-');
        try {
            Locale locale = new Locale.Builder().setLanguageTag(normalized).build();
            if (locale.getLanguage().isBlank()) {
                throw new IllformedLocaleException("Locale language is required");
            }
            return locale;
        } catch (IllformedLocaleException e) {
            throw new IllegalArgumentException("Invalid krandom.locale: " + tag, e);
        }
    }

    /**
     * Creates the provider hub used by application code.
     *
     * @param config generator configuration
     * @return the configured provider hub
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderHub providerHub(GeneratorConfig config) {
        return new ProviderHub(config);
    }

    /**
     * Creates the typed object-faker factory used by application code.
     *
     * @param config generator configuration
     * @return the configured object-faker factory
     */
    @Bean
    @ConditionalOnMissingBean
    public KrandomObjectFakerFactory krandomObjectFakerFactory(GeneratorConfig config) {
        return new KrandomObjectFakerFactory(config);
    }
}
