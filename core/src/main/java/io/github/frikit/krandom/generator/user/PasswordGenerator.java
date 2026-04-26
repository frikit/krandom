/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates passwords with configurable inclusive length ranges.
 */
public final class PasswordGenerator implements Generator<String> {

    private static final String ALPHABET           =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{};:,.?";
    private static final int    DEFAULT_MIN_LENGTH = 8;
    private static final int    DEFAULT_MAX_LENGTH = 16;

    private final Random random;

    public PasswordGenerator() {
        this(GeneratorConfig.defaults());
    }

    public PasswordGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    private static void validateRange(int minLength, int maxLength) {
        if (minLength <= 0) {
            throw new IllegalArgumentException("minLength must be positive, got: " + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("maxLength must be >= minLength, got: "
                                               + maxLength + " < " + minLength);
        }
    }

    @Override
    public String generate() {
        return generate(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
    }

    /**
     * Generates a password with fixed length.
     */
    public String generate(int length) {
        validateRange(length, length);
        return randomPassword(length);
    }

    /**
     * Generates a password with length sampled from an inclusive range.
     */
    public String generate(int minLength, int maxLength) {
        validateRange(minLength, maxLength);
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        return randomPassword(length);
    }

    private String randomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
