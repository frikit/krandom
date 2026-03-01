/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates SWIFT/BIC codes in 8 or 11-character form.
 */
public final class BicGenerator implements Generator<String> {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final GeneratorConfig config;
    private final Random random;

    public BicGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BicGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public BicGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return generate(config.getLocale(), random.nextBoolean());
    }

    /**
     * Generates a SWIFT/BIC code with either 8 or 11 characters.
     * Faker-style alias: {@code swift()}.
     *
     * @return SWIFT/BIC code
     */
    public String generateSwift() {
        return generate();
    }

    /**
     * Generates an 8-character SWIFT/BIC code.
     * Faker-style alias: {@code swift8()}.
     *
     * @return 8-character SWIFT/BIC
     */
    public String generateSwift8() {
        return generate(false);
    }

    /**
     * Generates an 11-character SWIFT/BIC code.
     * Faker-style alias: {@code swift11()}.
     *
     * @return 11-character SWIFT/BIC
     */
    public String generateSwift11() {
        return generate(true);
    }

    public String generate(boolean withBranch) {
        return generate(config.getLocale(), withBranch);
    }

    public String generate(Locale locale) {
        return generate(locale, random.nextBoolean());
    }

    public String generate(Locale locale, boolean withBranch) {
        Objects.requireNonNull(locale, "locale must not be null");
        String country = locale.getCountry();
        if (country.isBlank()) {
            country = "US";
        }
        if (country.length() != 2) {
            country = "US";
        }

        StringBuilder bic = new StringBuilder(11);
        appendRandom(bic, LETTERS, 4); // bank
        bic.append(country.toUpperCase(Locale.ROOT)); // country
        appendRandom(bic, ALNUM, 2); // location
        if (withBranch) {
            appendRandom(bic, ALNUM, 3); // branch
        }
        return bic.toString();
    }

    private void appendRandom(StringBuilder sb, String alphabet, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
    }
}
