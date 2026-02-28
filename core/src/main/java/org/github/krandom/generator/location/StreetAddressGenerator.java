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
 * Generates locale-aware street addresses (for example {@code "742 Main Street"}).
 *
 * <p>The house number is drawn from [1, 9999]. Street names and short/long suffix lists come
 * from {@link StreetAddressDataRegistry} for the configured locale.
 */
public final class StreetAddressGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;
    private final String[] streetNames;
    private final String[] streetTypesShort;
    private final String[] streetTypesLong;

    /** Creates a street-address generator with default configuration (Locale.US). */
    public StreetAddressGenerator() {
        this(GeneratorConfig.defaults());
    }

    /** Creates an unseeded generator for the given locale. */
    public StreetAddressGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a street-address generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     * @throws UnsupportedOperationException if the configured locale has no registered data
     */
    public StreetAddressGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        Locale locale = config.getLocale();
        if (!StreetAddressDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                    "Locale " + locale + " is not supported. Registered locales: "
                            + StreetAddressDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();

        StreetAddressDataProvider provider = StreetAddressDataRegistry.forLocale(locale);
        this.streetNames = provider.getStreetNames();
        this.streetTypesShort = provider.getStreetTypesShort();
        this.streetTypesLong = provider.getStreetTypesLong();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates address with short suffix form by default (Chance-style short_suffix=true).
     */
    @Override
    public String generate() {
        return generate(true);
    }

    /**
     * Generates a random street address of the form {@code "<number> <streetName> <streetType>"}.
     *
     * @param shortSuffix {@code true} for abbreviated suffixes ({@code "St"}, {@code "Ave"}),
     *                    {@code false} for full suffixes ({@code "Street"}, {@code "Avenue"})
     * @return a street-address string; never {@code null}
     */
    public String generate(boolean shortSuffix) {
        int number = random.nextInt(1, 10000);
        String name = streetNames[random.nextInt(streetNames.length)];
        String[] types = shortSuffix ? streetTypesShort : streetTypesLong;
        String type = types[random.nextInt(types.length)];
        return number + " " + name + " " + type;
    }

    /** Returns configured locale. */
    public Locale getLocale() {
        return config.getLocale();
    }

    /** Returns number of locale street names available to this generator instance. */
    public int getStreetNameCount() {
        return streetNames.length;
    }

    /** Returns true when configured locale is registered. */
    public boolean isLocaleExplicitlySupported() {
        return StreetAddressDataRegistry.isRegistered(config.getLocale());
    }
}
