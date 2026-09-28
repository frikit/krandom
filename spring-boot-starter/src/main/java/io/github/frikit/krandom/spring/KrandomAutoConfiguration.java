/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.github.frikit.krandom.generator.GenerationRecipe;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.IllformedLocaleException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
 *
 * <p>{@code krandom.recipe} is exclusive: the recipe already records the seed, locale, clock,
 * string and collection bounds, object settings, and safety and construction policies, so setting
 * any of those properties next to it fails startup instead of silently replaying a different
 * configuration. When the configuration is seed-owned, its effective recipe is logged at
 * {@code INFO} on startup, ready to be copied into {@code krandom.recipe}.
 */
@AutoConfiguration
@EnableConfigurationProperties(KrandomProperties.class)
public class KrandomAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(KrandomAutoConfiguration.class);

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
        GeneratorConfig config = hasText(properties.getRecipe())
            ? recipeConfig(properties)
            : propertiesConfig(properties);
        logEffectiveRecipe(config);
        return config;
    }

    /**
     * A recipe is a complete replay description: every other {@code krandom.*} generation
     * property maps onto a field it already records, so combining them would silently replay a
     * different configuration. Fail closed and name each conflicting property.
     */
    private static GeneratorConfig recipeConfig(KrandomProperties properties) {
        List<String> conflicts = explicitlySetRecipeProperties(properties);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "Configure krandom.recipe or the individual krandom.* properties, not both: the recipe already "
                    + "defines " + String.join(", ", conflicts));
        }
        return parseRecipe(properties.getRecipe()).toGeneratorConfig();
    }

    private static List<String> explicitlySetRecipeProperties(KrandomProperties properties) {
        Map<String, Object> candidates = new LinkedHashMap<>();
        candidates.put("krandom.seed", properties.getSeed());
        candidates.put("krandom.locale", textOrNull(properties.getLocale()));
        candidates.put("krandom.clock", textOrNull(properties.getClock()));
        candidates.put("krandom.clock-zone", textOrNull(properties.getClockZone()));
        candidates.put("krandom.object-max-depth", properties.getObjectMaxDepth());
        candidates.put("krandom.object-null-probability", properties.getObjectNullProbability());
        candidates.put("krandom.min-string-length", properties.getMinStringLength());
        candidates.put("krandom.max-string-length", properties.getMaxStringLength());
        candidates.put("krandom.min-collection-size", properties.getMinCollectionSize());
        candidates.put("krandom.max-collection-size", properties.getMaxCollectionSize());
        candidates.put("krandom.payment-card-safety-policy", properties.getPaymentCardSafetyPolicy());
        candidates.put("krandom.phone-number-safety-policy", properties.getPhoneNumberSafetyPolicy());
        candidates.put("krandom.national-id-safety-policy", properties.getNationalIdSafetyPolicy());
        candidates.put("krandom.banking-safety-policy", properties.getBankingSafetyPolicy());
        candidates.put("krandom.securities-identifier-safety-policy", properties.getSecuritiesIdentifierSafetyPolicy());
        candidates.put("krandom.crypto-address-safety-policy", properties.getCryptoAddressSafetyPolicy());
        candidates.put("krandom.business-tax-identifier-safety-policy",
                       properties.getBusinessTaxIdentifierSafetyPolicy());
        candidates.put("krandom.identity-document-safety-policy", properties.getIdentityDocumentSafetyPolicy());
        candidates.put("krandom.object-construction-policy", properties.getObjectConstructionPolicy());
        List<String> set = new ArrayList<>();
        candidates.forEach((name, value) -> {
            if (value != null) {
                set.add(name);
            }
        });
        return set;
    }

    private static void logEffectiveRecipe(GeneratorConfig config) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        config.getGenerationRecipe().ifPresent(recipe -> {
            String serialized = recipe.serializeForDiagnostics();
            String encoded = Base64.getUrlEncoder().withoutPadding()
                                   .encodeToString(serialized.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("krandom: effective generation recipe (replay with krandom.recipe=base64:" + encoded
                            + "; without krandom.clock the recorded instant is the startup time):"
                            + System.lineSeparator() + serialized);
        });
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String textOrNull(String value) {
        return hasText(value) ? value : null;
    }

    private static GeneratorConfig propertiesConfig(KrandomProperties properties) {
        GeneratorConfig defaults = GeneratorConfig.defaults();
        GeneratorConfig.Builder builder = GeneratorConfig.builder();
        if (properties.getSeed() != null) {
            builder.seed(properties.getSeed());
        }
        if (hasText(properties.getLocale())) {
            builder.locale(parseLocale(properties.getLocale()));
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
     * Creates the provider hub used by application code and applies every
     * {@link KrandomProviderCustomizer} bean once, in order, before the hub is published.
     *
     * @param config      generator configuration
     * @param customizers provider registrations to apply
     * @return the configured provider hub
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderHub providerHub(GeneratorConfig config, ObjectProvider<KrandomProviderCustomizer> customizers) {
        ProviderHub hub = new ProviderHub(config);
        customizers.orderedStream().forEach(customizer -> customizer.customize(hub));
        return hub;
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
