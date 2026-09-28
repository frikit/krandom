/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware job seniority labels, e.g. {@code "Senior"}.
 *
 * <p>Labels are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link SeniorityDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Output is reproducible for a fixed seed and locale.
 */
public final class SeniorityGenerator implements Generator<String> {

    private static final SeniorityDataProvider DEFAULT_PROVIDER =
        new BuiltInSeniorityDataProvider(Locale.ROOT, "default");

    private final List<String> seniorities;
    private final Random random;

    /**
     * Creates a seniority generator with default configuration.
     */
    public SeniorityGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a seniority generator for the given locale.
     *
     * @param locale locale whose seniority labels to use; must not be {@code null}
     */
    public SeniorityGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a seniority generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public SeniorityGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        SeniorityDataProvider provider = config.getRegistryContext().seniorityProvider(config.getLocale());
        this.seniorities = (provider != null ? provider : DEFAULT_PROVIDER).getSeniorities();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return seniorities.get(random.nextInt(seniorities.size()));
    }
}
