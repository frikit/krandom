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
 * Generates locale-aware marital status values, e.g. {@code "Married"}.
 *
 * <p>Values are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link MaritalStatusDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Output is reproducible for a fixed seed and locale.
 */
public final class MaritalStatusGenerator implements Generator<String> {

    private static final MaritalStatusDataProvider DEFAULT_PROVIDER =
        new BuiltInMaritalStatusDataProvider(Locale.ROOT, "default");

    private final List<String> statuses;
    private final Random random;

    /**
     * Creates a marital-status generator with default configuration.
     */
    public MaritalStatusGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a marital-status generator for the given locale.
     *
     * @param locale locale whose marital statuses to use; must not be {@code null}
     */
    public MaritalStatusGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a marital-status generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public MaritalStatusGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        MaritalStatusDataProvider provider = config.getRegistryContext().maritalStatusProvider(config.getLocale());
        this.statuses = (provider != null ? provider : DEFAULT_PROVIDER).getMaritalStatuses();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return statuses.get(random.nextInt(statuses.size()));
    }
}
