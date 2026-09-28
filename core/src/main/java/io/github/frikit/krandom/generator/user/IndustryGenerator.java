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
 * Generates locale-aware company industry values, e.g. {@code "Healthcare"} for English or
 * {@code "Gesundheitswesen"} for German.
 *
 * <p>Names are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link IndustryDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Output is reproducible for a fixed seed and locale.
 */
public final class IndustryGenerator implements Generator<String> {

    private static final IndustryDataProvider DEFAULT_PROVIDER =
        new BuiltInIndustryDataProvider(Locale.ROOT, "default");

    private final List<String> industries;
    private final Random random;

    /**
     * Creates an industry generator with default configuration.
     */
    public IndustryGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an industry generator for the given locale.
     *
     * @param locale locale whose industry names to use; must not be {@code null}
     */
    public IndustryGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates an industry generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public IndustryGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        IndustryDataProvider provider = config.getRegistryContext().industryProvider(config.getLocale());
        this.industries = (provider != null ? provider : DEFAULT_PROVIDER).getIndustries();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return industries.get(random.nextInt(industries.size()));
    }
}
