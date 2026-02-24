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
 * Generates locale-aware state/province names.
 *
 * <p>The locale controls which states are returned — for example, US states for {@code en_US},
 * UK countries for {@code en_GB}, Australian states for {@code en_AU}, etc.
 *
 * <p>Built-in support covers 10 locales: {@code en_US}, {@code en_GB}, {@code en_AU},
 * {@code de_DE}, {@code fr_FR}, {@code es_ES}, {@code it_IT}, {@code pt_BR},
 * {@code ja_JP}, and {@code zh_CN}. Additional locales — and overrides of built-in ones — can be
 * registered at runtime via {@link StateDataRegistry#register(StateDataProvider)}.
 *
 * <p>States can be generated as full names (e.g., {@code "California"}) or abbreviations
 * (e.g., {@code "CA"}) where supported, controlled by the {@link #generate(boolean)} method.
 */
public final class StateGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;
    private final String[] states;
    private final String[] abbreviations;

    /**
     * Creates a generator using the given config.
     *
     * @param config the generator configuration; must not be {@code null}; the locale in the
     *               config must be registered in {@link StateDataRegistry}
     * @throws NullPointerException          if {@code config} is {@code null}
     * @throws UnsupportedOperationException if the configured locale has no registered data
     */
    public StateGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!StateDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                StateDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();

        StateDataProvider provider = StateDataRegistry.forLocale(locale);
        this.states = provider.getStates();
        this.abbreviations = provider.getAbbreviations();
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale the locale determining which states to generate; must not be {@code null}
     * @throws NullPointerException          if {@code locale} is {@code null}
     * @throws UnsupportedOperationException if the locale has no registered data
     */
    public StateGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a state name (full name) appropriate for the configured locale.
     */
    @Override
    public String generate() {
        return generate(false);
    }

    /**
     * Generates a random state name or abbreviation.
     *
     * @param useAbbreviation if {@code true}, returns an abbreviation when available;
     *                        if {@code false} or abbreviations are not available, returns full name
     * @return a state name or abbreviation; never {@code null}
     */
    public String generate(boolean useAbbreviation) {
        int index = random.nextInt(states.length);
        
        if (useAbbreviation && abbreviations.length > 0 && 
            index < abbreviations.length && !abbreviations[index].isEmpty()) {
            return abbreviations[index];
        }
        
        return states[index];
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
     * Returns the number of distinct state names available for the configured locale.
     *
     * @return the state count; always positive
     */
    public int getStateCount() {
        return states.length;
    }

    /**
     * Returns {@code true} if the configured locale has a registered data provider.
     *
     * @return {@code true} for all locales accepted by the constructor
     */
    public boolean isLocaleExplicitlySupported() {
        return StateDataRegistry.isRegistered(config.getLocale());
    }
}
