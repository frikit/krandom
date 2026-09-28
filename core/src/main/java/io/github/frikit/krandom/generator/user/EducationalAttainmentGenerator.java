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
 * Generates locale-aware educational attainment values, e.g. {@code "Master's Degree"}.
 *
 * <p>Values are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link EducationalAttainmentDataRegistry}; locales without built-in data fall back to the bundled
 * English list. Output is reproducible for a fixed seed and locale.
 */
public final class EducationalAttainmentGenerator implements Generator<String> {

    private static final EducationalAttainmentDataProvider DEFAULT_PROVIDER =
        new BuiltInEducationalAttainmentDataProvider(Locale.ROOT, "default");

    private final List<String> levels;
    private final Random random;

    /**
     * Creates an educational-attainment generator with default configuration.
     */
    public EducationalAttainmentGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an educational-attainment generator for the given locale.
     *
     * @param locale locale whose attainment levels to use; must not be {@code null}
     */
    public EducationalAttainmentGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates an educational-attainment generator with the specified configuration.
     *
     * @param config generator configuration; must not be {@code null}
     */
    public EducationalAttainmentGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        EducationalAttainmentDataProvider provider = config.getRegistryContext().educationalAttainmentProvider(config.getLocale());
        this.levels = (provider != null ? provider : DEFAULT_PROVIDER).getEducationalAttainments();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return levels.get(random.nextInt(levels.size()));
    }
}
