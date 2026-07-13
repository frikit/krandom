/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.weather;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.DataRegistryContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware weather conditions, e.g. {@code "Sunny"} for English or {@code "Солнечно"}
 * for Russian.
 *
 * <p>Condition names are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link WeatherDataRegistry}. Locales without a built-in file fall back to bundled English
 * conditions.
 *
 * <pre>{@code
 *   String en = new WeatherGenerator().generate();                    // e.g. "Rainy"
 *   String ru = new WeatherGenerator(Locale.of("ru","RU")).generate(); // e.g. "Дождливо"
 * }</pre>
 */
public final class WeatherGenerator implements Generator<String> {

    private static final WeatherDataProvider DEFAULT_PROVIDER =
        new BuiltInWeatherDataProvider(Locale.ROOT, "krandom/weather/default.txt");

    private final List<String> conditions;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public WeatherGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose condition names to use; falls back to English if no built-in
     *               file exists; must not be {@code null}
     */
    public WeatherGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public WeatherGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        WeatherDataProvider provider = registryContext.weatherProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.conditions = provider.getConditions();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random weather condition in the configured locale.
     *
     * @return a localized condition such as {@code "Sunny"}; never {@code null}
     */
    @Override
    public String generate() {
        return conditions.get(random.nextInt(conditions.size()));
    }
}
