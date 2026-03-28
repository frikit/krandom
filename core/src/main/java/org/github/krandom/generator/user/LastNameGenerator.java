/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware last names (family names / surnames).
 *
 * <p>Built-in support covers 10 locales (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH). Additional
 * locales — and overrides of built-in ones — can be registered at runtime via
 * {@link LastNameDataRegistry#register(LastNameDataProvider)}.
 *
 * <pre>{@code
 * // Default — Locale.US
 * LastNameGenerator gen = new LastNameGenerator();
 * String name = gen.generate(); // "Smith", "Johnson", ...
 *
 * // Specific locale
 * LastNameGenerator de = new LastNameGenerator(Locale.GERMANY);
 * String deName = de.generate(); // "Müller", "Schmidt", ...
 * }</pre>
 */
public final class LastNameGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        lastNames;

    /**
     * Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}.
     */
    public LastNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Constructs a generator for the given locale.
     */
    public LastNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Full constructor using a {@link GeneratorConfig} (locale + optional seed).
     */
    public LastNameGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!LastNameDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: "
                + LastNameDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
        this.lastNames = LastNameDataRegistry.forLocale(locale).getLastNames();
    }

    @Override
    public String generate() {
        return lastNames[random.nextInt(lastNames.length)];
    }

    /**
     * Generates a last name for male context (locale morphology not differentiated yet).
     *
     * @return last name
     */
    public String generateMale() {
        return generate();
    }

    /**
     * Generates a last name for female context (locale morphology not differentiated yet).
     *
     * @return last name
     */
    public String generateFemale() {
        return generate();
    }

    /**
     * Returns the locale this generator was configured with.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns the number of distinct last names for the configured locale.
     */
    public int getLastNameCount() {
        return lastNames.length;
    }

    /**
     * Returns {@code true} if the configured locale has a registered last-name provider.
     */
    public boolean isLocaleExplicitlySupported() {
        return LastNameDataRegistry.isRegistered(config.getLocale());
    }
}
