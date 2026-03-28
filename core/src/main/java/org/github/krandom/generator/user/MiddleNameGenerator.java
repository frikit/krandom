/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Generates locale-aware middle names.
 *
 * <p>This generator is intentionally conservative and only allows locales where a middle-name
 * concept is broadly compatible with this library's first+middle+last name model.
 * Unsupported locales throw {@link UnsupportedOperationException}.
 */
public final class MiddleNameGenerator implements Generator<String> {

    private static final Set<String> MIDDLE_NAME_SUPPORTED = Set.of(
        "en_US", "en_GB", "en_AU", "fr_FR", "de_DE", "it_IT", "pt_BR"
    );

    private final GeneratorConfig    config;
    private final FirstNameGenerator firstNameGenerator;

    /**
     * Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}.
     */
    public MiddleNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Constructs a generator for the given locale.
     */
    public MiddleNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Full constructor using a {@link GeneratorConfig} (locale + optional seed).
     */
    public MiddleNameGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        if (!supportsMiddleName(config.getLocale())) {
            throw new UnsupportedOperationException(
                "Middle names are not supported for locale "
                + config.getLocale()
                + " (" + localeKey(config.getLocale()) + ")");
        }
        this.firstNameGenerator = new FirstNameGenerator(config);
    }

    /**
     * Returns {@code true} if the given locale supports middle-name generation in this model.
     */
    public static boolean supportsMiddleName(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");
        return MIDDLE_NAME_SUPPORTED.contains(localeKey(locale));
    }

    private static String localeKey(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    @Override
    public String generate() {
        return firstNameGenerator.generate();
    }

    /**
     * Generates a middle name for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a middle name
     */
    public String generate(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return firstNameGenerator.generate(gender);
    }

    /**
     * Generates a middle initial, for example {@code "P."}.
     *
     * @return middle initial with trailing period
     */
    public String generateInitial() {
        String middle = generate();
        return middle.charAt(0) + ".";
    }

    /**
     * Generates a gender-specific middle initial, for example {@code "P."}.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return middle initial with trailing period
     */
    public String generateInitial(Gender gender) {
        String middle = generate(gender);
        return middle.charAt(0) + ".";
    }

    /**
     * Returns the locale this generator was configured with.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns {@code true} if this locale has both name data and middle-name support.
     */
    public boolean isLocaleExplicitlySupported() {
        return true;
    }
}
