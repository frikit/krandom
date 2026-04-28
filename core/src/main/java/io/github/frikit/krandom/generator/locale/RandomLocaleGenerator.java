/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.locale;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random locales from kRandom's supported locale catalog.
 */
public final class RandomLocaleGenerator implements Generator<Locale> {

    private final Random random;

    /**
     * Creates a locale generator with default configuration.
     */
    public RandomLocaleGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a deterministic locale generator using the supplied seed.
     *
     * @param seed deterministic seed
     */
    public RandomLocaleGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a locale generator with explicit configuration.
     *
     * @param config generator configuration
     */
    public RandomLocaleGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a {@link Locale} from {@link SupportedLocale#values()}.
     *
     * @return supported locale
     */
    @Override
    public Locale generate() {
        return generateSupportedLocale().locale();
    }

    /**
     * Generates the enum value backing the generated locale.
     *
     * @return supported locale enum
     */
    public SupportedLocale generateSupportedLocale() {
        SupportedLocale[] values = SupportedLocale.values();
        return values[random.nextInt(values.length)];
    }
}
