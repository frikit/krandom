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
 * Generates locale-aware job position titles, e.g. {@code "Software Engineer"}.
 *
 * <p>Titles are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link PositionDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Output is reproducible for a fixed seed and locale.
 */
public final class PositionGenerator implements Generator<String> {

    private static final PositionDataProvider DEFAULT_PROVIDER =
        new BuiltInPositionDataProvider(Locale.ROOT, "default");

    private final List<String> positions;
    private final Random random;

    /**
     * Creates a position generator with default configuration.
     */
    public PositionGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a position generator for the given locale.
     *
     * @param locale locale whose position titles to use; must not be {@code null}
     */
    public PositionGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a position generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public PositionGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        PositionDataProvider provider = config.getRegistryContext().positionProvider(config.getLocale());
        this.positions = (provider != null ? provider : DEFAULT_PROVIDER).getPositions();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return positions.get(random.nextInt(positions.size()));
    }
}
