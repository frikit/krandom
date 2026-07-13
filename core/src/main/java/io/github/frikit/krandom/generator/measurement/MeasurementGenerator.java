/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.measurement;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.DataRegistryContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware measurement-unit names, e.g. {@code "Kilometer"} for English or
 * {@code "Километр"} for Russian.
 *
 * <p>Unit names are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link MeasurementDataRegistry}. Locales without a built-in file fall back to bundled English
 * units.
 *
 * <pre>{@code
 *   String en = new MeasurementGenerator().generate();                     // e.g. "Liter"
 *   String ru = new MeasurementGenerator(Locale.of("ru","RU")).generate(); // e.g. "Литр"
 * }</pre>
 */
public final class MeasurementGenerator implements Generator<String> {

    private static final MeasurementDataProvider DEFAULT_PROVIDER =
        new BuiltInMeasurementDataProvider(Locale.ROOT, "krandom/measurement/default.txt");

    private final List<String> units;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public MeasurementGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose unit names to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public MeasurementGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public MeasurementGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        MeasurementDataProvider provider = registryContext.measurementProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.units = provider.getUnits();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random measurement-unit name in the configured locale.
     *
     * @return a localized unit such as {@code "Kilometer"}; never {@code null}
     */
    @Override
    public String generate() {
        return units.get(random.nextInt(units.size()));
    }
}
