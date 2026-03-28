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
 * Generates locale-aware first names, with optional gender filtering.
 *
 * <p>Built-in support covers 10 locales (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH). Additional
 * locales — and overrides of built-in ones — can be registered at runtime via
 * {@link FirstNameDataRegistry#register(FirstNameDataProvider)}.
 *
 * <pre>{@code
 * // Default — Locale.US, random gender
 * FirstNameGenerator gen = new FirstNameGenerator();
 * String name = gen.generate();              // "James", "Mary", ...
 *
 * // Gender-specific
 * String male   = gen.generate(Gender.MALE);   // "James", "John", ...
 * String female = gen.generate(Gender.FEMALE); // "Mary", "Patricia", ...
 *
 * // Specific locale
 * FirstNameGenerator de = new FirstNameGenerator(Locale.GERMANY);
 * String deName = de.generate(Gender.FEMALE); // "Marie", "Sophie", ...
 * }</pre>
 */
public final class FirstNameGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        maleNames;
    private final String[]        femaleNames;

    /**
     * Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}.
     */
    public FirstNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Constructs a generator for the given locale.
     */
    public FirstNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Full constructor using a {@link GeneratorConfig} (locale + optional seed).
     */
    public FirstNameGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!FirstNameDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: "
                + FirstNameDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
        FirstNameDataProvider provider = FirstNameDataRegistry.forLocale(locale);
        this.maleNames = provider.getMaleFirstNames();
        this.femaleNames = provider.getFemaleFirstNames();
    }

    /**
     * Generates a random first name from both male and female lists.
     *
     * <p>The gender is chosen randomly on each call.
     */
    @Override
    public String generate() {
        boolean pickMale = random.nextBoolean();
        String[] pool = pickMale ? maleNames : femaleNames;
        return pool[random.nextInt(pool.length)];
    }

    /**
     * Generates a first name for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}
     */
    public String generate(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        String[] pool = gender == Gender.MALE ? maleNames : femaleNames;
        return pool[random.nextInt(pool.length)];
    }

    /**
     * Returns the locale this generator was configured with.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns the number of distinct male first names for the configured locale.
     */
    public int getMaleNameCount() {
        return maleNames.length;
    }

    /**
     * Returns the number of distinct female first names for the configured locale.
     */
    public int getFemaleNameCount() {
        return femaleNames.length;
    }

    /**
     * Returns {@code true} if the configured locale has a registered first-name provider.
     */
    public boolean isLocaleExplicitlySupported() {
        return FirstNameDataRegistry.isRegistered(config.getLocale());
    }
}
