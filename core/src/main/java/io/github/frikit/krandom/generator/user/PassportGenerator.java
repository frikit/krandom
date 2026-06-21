/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates generic passport numbers in the form of one uppercase letter followed by eight digits,
 * e.g. {@code "A12345678"} (matching {@code [A-Z][0-9]{8}}).
 *
 * <p>The format is a generic nine-character form; country-specific passport formats are a future
 * enhancement.
 *
 * <pre>{@code
 *   String passport = new PassportGenerator().generate(); // e.g. "A12345678"
 * }</pre>
 */
public final class PassportGenerator implements Generator<String> {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public PassportGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public PassportGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a generic passport number: one uppercase letter followed by eight digits.
     *
     * @return a passport number such as {@code "A12345678"}; never {@code null}
     */
    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(9);
        sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
