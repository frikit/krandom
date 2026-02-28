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

/**
 * Generates locale-aware full names by combining a {@link FirstNameGenerator} and a
 * {@link LastNameGenerator}.
 *
 * <p>Built-in support covers the same 10 locales as the underlying name generators
 * (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH).
 *
 * <pre>{@code
 * FullNameGenerator gen = new FullNameGenerator();
 * String name = gen.generate();               // "James Smith"
 * String male = gen.generate(Gender.MALE);    // "John Doe"
 *
 * FullNameGenerator de = new FullNameGenerator(Locale.GERMANY);
 * String deName = de.generate(Gender.FEMALE); // "Marie Müller"
 * }</pre>
 */
public final class FullNameGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final FirstNameGenerator firstNameGenerator;
    private final LastNameGenerator lastNameGenerator;

    /** Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}. */
    public FullNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /** Constructs a generator for the given locale. */
    public FullNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /** Full constructor using a {@link GeneratorConfig} (locale + optional seed). */
    public FullNameGenerator(GeneratorConfig config) {
        this.config             = Objects.requireNonNull(config, "config must not be null");
        this.firstNameGenerator = new FirstNameGenerator(config);
        this.lastNameGenerator  = new LastNameGenerator(config);
    }

    /**
     * Generates a random full name (first + last) with a randomly chosen gender.
     *
     * @return a full name string; never {@code null}
     */
    @Override
    public String generate() {
        return firstNameGenerator.generate() + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a full name string; never {@code null}
     * @throws NullPointerException if {@code gender} is {@code null}
     */
    public String generate(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return firstNameGenerator.generate(gender) + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name that includes a middle name.
     *
     * <p>For locales where middle names are not part of the naming model (for example
     * {@code es_ES}, {@code ja_JP}, {@code zh_CN}), this method throws
     * {@link UnsupportedOperationException}.
     *
     * @return a three-part full name string; never {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleName() {
        MiddleNameGenerator middleNameGenerator = new MiddleNameGenerator(config);
        return firstNameGenerator.generate()
                + " " + middleNameGenerator.generate()
                + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name that includes a middle name for the specified gender.
     *
     * <p>For locales where middle names are not part of the naming model (for example
     * {@code es_ES}, {@code ja_JP}, {@code zh_CN}), this method throws
     * {@link UnsupportedOperationException}.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a three-part full name string; never {@code null}
     * @throws NullPointerException if {@code gender} is {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleName(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        MiddleNameGenerator middleNameGenerator = new MiddleNameGenerator(config);
        return firstNameGenerator.generate(gender)
                + " " + middleNameGenerator.generate(gender)
                + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name with a middle initial (for example, {@code "John P. Smith"}).
     *
     * <p>For locales where middle names are not part of the naming model (for example
     * {@code es_ES}, {@code ja_JP}, {@code zh_CN}), this method throws
     * {@link UnsupportedOperationException}.
     *
     * @return a three-part full name with middle initial; never {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleInitial() {
        return firstNameGenerator.generate()
                + " " + new MiddleNameGenerator(config).generateInitial()
                + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name with a middle initial for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a three-part full name with middle initial; never {@code null}
     * @throws NullPointerException if {@code gender} is {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleInitial(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return firstNameGenerator.generate(gender)
                + " " + new MiddleNameGenerator(config).generateInitial(gender)
                + " " + lastNameGenerator.generate();
    }

    /** Returns the locale this generator was configured with. */
    public Locale getLocale() {
        return config.getLocale();
    }

    /** Returns {@code true} if the configured locale has registered name providers. */
    public boolean isLocaleExplicitlySupported() {
        return firstNameGenerator.isLocaleExplicitlySupported();
    }
}
