/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates username strings using locale-aware first/last names plus numeric suffixes.
 */
public final class UsernameGenerator implements Generator<String> {

    private final GeneratorConfig    config;
    private final Random             random;
    private final FirstNameGenerator firstNameGenerator;
    private final LastNameGenerator  lastNameGenerator;

    public UsernameGenerator() {
        this(GeneratorConfig.defaults());
    }

    public UsernameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public UsernameGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.firstNameGenerator = new FirstNameGenerator(config);
        this.lastNameGenerator = new LastNameGenerator(config);
    }

    private static String normalize(String value) {
        return value.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private static String fallback(String value, String defaultValue) {
        return value.isBlank() ? defaultValue : value;
    }

    @Override
    public String generate() {
        String first = fallback(normalize(firstNameGenerator.generate()), "user");
        String last = fallback(normalize(lastNameGenerator.generate()), "name");
        return switch (random.nextInt(4)) {
            case 0 -> first + "." + last;
            case 1 -> first + "_" + last;
            case 2 -> first.charAt(0) + last + random.nextInt(10, 1000);
            default -> first + last + random.nextInt(10, 1000);
        };
    }

    public Locale getLocale() {
        return config.getLocale();
    }
}
