/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.identifier;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates identifier-like values from mask templates.
 *
 * <p>Supported placeholders:
 * <ul>
 *   <li>{@code #} -> random digit {@code 0-9}</li>
 *   <li>{@code ?} -> random uppercase letter {@code A-Z}</li>
 * </ul>
 */
public final class IdentifierMaskGenerator implements Generator<String> {

    private static final String DEFAULT_MASK = "??##??##";

    private final Random random;

    public IdentifierMaskGenerator() {
        this(GeneratorConfig.defaults());
    }

    public IdentifierMaskGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    private static String requireMask(String mask) {
        Objects.requireNonNull(mask, "mask must not be null");
        if (mask.isBlank()) {
            throw new IllegalArgumentException("mask must not be blank");
        }
        return mask;
    }

    @Override
    public String generate() {
        return generate(DEFAULT_MASK);
    }

    /**
     * Applies both numeric and alpha placeholders to the provided mask.
     *
     * @param mask template using {@code #} and {@code ?}
     * @return generated identifier
     */
    public String generate(String mask) {
        return generateAlphaNumeric(mask);
    }

    /**
     * Applies only numeric placeholders to the provided mask.
     *
     * @param mask template using {@code #}
     * @return generated value
     */
    public String generateNumeric(String mask) {
        String normalized = requireMask(mask);
        StringBuilder out = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            out.append(ch == '#' ? randomDigit() : ch);
        }
        return out.toString();
    }

    /**
     * Applies numeric and alpha placeholders to the provided mask.
     *
     * @param mask template using {@code #} and {@code ?}
     * @return generated value
     */
    public String generateAlphaNumeric(String mask) {
        String normalized = requireMask(mask);
        StringBuilder out = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch == '#') {
                out.append(randomDigit());
            } else if (ch == '?') {
                out.append(randomUpperLetter());
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    private char randomDigit() {
        return (char) ('0' + random.nextInt(10));
    }

    private char randomUpperLetter() {
        return (char) ('A' + random.nextInt(26));
    }
}
