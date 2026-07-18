/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    /**
     * Generates a password that satisfies the supplied immutable policy.
     *
     * @param policy password character and length requirements
     * @return policy-compliant password
     */
    public String generate(PasswordPolicy policy) {
        PasswordPolicy effectivePolicy = Objects.requireNonNull(policy, "policy must not be null");
        int length = effectivePolicy.minLength() + random.nextInt(effectivePolicy.maxLength() - effectivePolicy.minLength() + 1);
        List<Character> characters = new ArrayList<>(length);
        for (PasswordPolicy.Requirement requirement : effectivePolicy.requirements()) {
            for (int i = 0; i < requirement.minimumCount(); i++) {
                characters.add(randomCharacter(requirement.symbols()));
            }
        }
        while (characters.size() < length) {
            characters.add(randomCharacter(effectivePolicy.alphabet()));
        }
        Collections.shuffle(characters, random);
        StringBuilder result = new StringBuilder(length);
        characters.forEach(result::append);
        return result.toString();
    }

    private String randomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(randomCharacter(ALPHABET));
        }
        return sb.toString();
    }

    private char randomCharacter(String alphabet) {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }
}
