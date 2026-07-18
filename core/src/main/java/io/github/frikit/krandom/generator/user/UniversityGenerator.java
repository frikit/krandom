/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates coherent university fixtures supplied by a local data pack.
 *
 * <p>University data is deliberately not global: load a verified
 * {@link io.github.frikit.krandom.generator.datapack.LocalDataPack} and attach it to the
 * {@link DataRegistryContext} in the generator configuration. This keeps data provenance and
 * test fixtures explicit.
 */
public final class UniversityGenerator implements Generator<UniversityData> {

    private final GeneratorConfig  config;
    private final Random           random;
    private final UniversityData[] universities;

    /**
     * Creates a university generator from explicit configuration.
     *
     * @param config generator configuration containing a local university data pack
     * @throws NullPointerException          if {@code config} is null
     * @throws UnsupportedOperationException if no university data is configured for the locale
     */
    public UniversityGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext context = config.getRegistryContext();
        Locale locale = config.getLocale();
        UniversityDataProvider provider = context.universityProvider(locale);
        if (provider == null) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " has no local university data pack. Registered locales: "
                + context.universityRegisteredKeys());
        }
        this.random = config.createRandom();
        this.universities = provider.getUniversities();
    }

    /**
     * Generates a coherent university fixture.
     *
     * @return university data
     */
    @Override
    public UniversityData generate() {
        return universities[random.nextInt(universities.length)];
    }

    /**
     * Generates an institution name.
     *
     * @return institution name
     */
    public String name() {
        return generate().name();
    }

    /**
     * Generates a degree name or abbreviation.
     *
     * @return degree
     */
    public String degree() {
        return generate().degree();
    }

    /**
     * Generates an institution prefix.
     *
     * @return prefix
     */
    public String prefix() {
        return generate().prefix();
    }

    /**
     * Generates an institution suffix.
     *
     * @return suffix
     */
    public String suffix() {
        return generate().suffix();
    }

    /**
     * Generates an institution location.
     *
     * @return place
     */
    public String place() {
        return generate().place();
    }

    /**
     * Returns the configured locale.
     *
     * @return generator locale
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns the number of available coherent fixtures.
     *
     * @return fixture count
     */
    public int getUniversityCount() {
        return universities.length;
    }

    /**
     * Returns whether the configured context explicitly supplies university data for this locale.
     *
     * @return true when a local pack is registered
     */
    public boolean isLocaleExplicitlySupported() {
        return config.getRegistryContext().isUniversityRegistered(config.getLocale());
    }
}
