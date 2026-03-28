/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware city names.
 *
 * <p>The locale controls which cities are returned — for example, US cities for {@code en_US},
 * German cities for {@code de_DE}, Japanese cities for {@code ja_JP}, etc.
 *
 * <p>Built-in support covers 10 locales: {@code en_US}, {@code en_GB}, {@code en_AU},
 * {@code fr_FR}, {@code de_DE}, {@code ja_JP}, {@code es_ES}, {@code it_IT}, {@code pt_BR}, and
 * {@code zh_CN}. Additional locales — and overrides of built-in ones — can be registered at
 * runtime via {@link CityDataRegistry#register(CityDataProvider)}.
 */
public final class CityGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        cities;

    /**
     * Creates a generator using default configuration ({@link Locale#US}).
     */
    public CityGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given config.
     *
     * @param config the generator configuration; must not be {@code null}; the locale in the
     *               config must be registered in {@link CityDataRegistry}
     * @throws NullPointerException          if {@code config} is {@code null}
     * @throws UnsupportedOperationException if the configured locale has no registered data
     */
    public CityGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!CityDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                CityDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();

        this.cities = CityDataRegistry.forLocale(locale).getCities();
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale the locale determining which cities to generate; must not be {@code null}
     * @throws NullPointerException          if {@code locale} is {@code null}
     * @throws UnsupportedOperationException if the locale has no registered data
     */
    public CityGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a city name appropriate for the configured locale.
     */
    @Override
    public String generate() {
        return cities[random.nextInt(cities.length)];
    }

    /**
     * Returns the locale this generator is configured with.
     *
     * @return the locale; never {@code null}
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns the number of distinct city names available for the configured locale.
     *
     * @return the city count; always positive
     */
    public int getCityCount() {
        return cities.length;
    }

    /**
     * Returns {@code true} if the configured locale has a registered data provider.
     *
     * @return {@code true} for all locales accepted by the constructor
     */
    public boolean isLocaleExplicitlySupported() {
        return CityDataRegistry.isRegistered(config.getLocale());
    }
}
