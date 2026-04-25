/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.spring;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.provider.ProviderHub;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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

    @Bean
    @ConditionalOnMissingBean
    public GeneratorConfig generatorConfig(KrandomProperties properties) {
        GeneratorConfig.Builder builder = GeneratorConfig.builder();

        if (properties.getSeed() != null) {
            builder.seed(properties.getSeed());
        }

        if (properties.getLocale() != null && !properties.getLocale().isBlank()) {
            builder.locale(parseLocale(properties.getLocale()));
        }

        if (properties.getObjectMaxDepth() != null) {
            builder.objectMaxDepth(properties.getObjectMaxDepth());
        }

        if (properties.getObjectNullProbability() != null) {
            builder.objectNullProbability(properties.getObjectNullProbability());
        }

        if (properties.getMinStringLength() != null && properties.getMaxStringLength() != null) {
            builder.stringLength(properties.getMinStringLength(), properties.getMaxStringLength());
        }

        if (properties.getMinCollectionSize() != null && properties.getMaxCollectionSize() != null) {
            builder.collectionSize(properties.getMinCollectionSize(), properties.getMaxCollectionSize());
        }

        return builder.build();
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

    @Bean
    @ConditionalOnMissingBean
    public ProviderHub providerHub(GeneratorConfig config) {
        return new ProviderHub(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public KrandomObjectFakerFactory krandomObjectFakerFactory(GeneratorConfig config) {
        return new KrandomObjectFakerFactory(config);
    }
}
