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
 * Generates locale-aware gender labels.
 *
 * <p>Built-in support covers 10 locales (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH). Additional
 * locales — and overrides of built-in ones — can be registered at runtime via
 * {@link GenderDataRegistry#register(GenderDataProvider)}.
 *
 * <pre>{@code
 * // Default — Locale.US, random gender
 * GenderGenerator gen = new GenderGenerator();
 * String label = gen.generate();              // "Male" or "Female"
 *
 * // Gender-specific
 * String male   = gen.generate(Gender.MALE);   // "Male"
 * String female = gen.generate(Gender.FEMALE); // "Female"
 *
 * // German locale
 * GenderGenerator de = new GenderGenerator(Locale.GERMANY);
 * String deLabel = de.generate(Gender.MALE);   // "Männlich"
 * }</pre>
 */
public final class GenderGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;
    private final String maleLabel;
    private final String femaleLabel;

    /** Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}. */
    public GenderGenerator() {
        this(GeneratorConfig.defaults());
    }

    /** Constructs a generator for the given locale. */
    public GenderGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /** Full constructor using a {@link GeneratorConfig} (locale + optional seed). */
    public GenderGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!GenderDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                    "Locale " + locale + " is not supported. Registered locales: "
                            + GenderDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();

        GenderDataProvider provider = GenderDataRegistry.forLocale(locale);
        this.maleLabel   = provider.getMaleLabel();
        this.femaleLabel = provider.getFemaleLabel();
    }

    /**
     * Generates a random gender label (male or female chosen with equal probability).
     */
    @Override
    public String generate() {
        return random.nextBoolean() ? maleLabel : femaleLabel;
    }

    /**
     * Generates the localized label for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     */
    public String generate(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return gender == Gender.MALE ? maleLabel : femaleLabel;
    }

    /** Returns the locale this generator was configured with. */
    public Locale getLocale() {
        return config.getLocale();
    }

    /** Returns the localized label for the male gender. */
    public String getMaleLabel() {
        return maleLabel;
    }

    /** Returns the localized label for the female gender. */
    public String getFemaleLabel() {
        return femaleLabel;
    }

    /** Returns {@code true} if the configured locale has a registered gender-label provider. */
    public boolean isLocaleExplicitlySupported() {
        return GenderDataRegistry.isRegistered(config.getLocale());
    }
}
