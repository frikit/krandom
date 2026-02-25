/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random hexadecimal hash strings.
 *
 * <p>This generator produces random hexadecimal strings that resemble hash values.
 * The default length is 40 characters (SHA-1 compatible), but custom lengths
 * and case control are supported.
 *
 * <p><strong>Basic Usage (40 characters, lowercase):</strong>
 * <pre>{@code
 * HashGenerator gen = new HashGenerator();
 * String hash = gen.generate();  // "d96eb2e7e85c3f4f4a7f0e51f5f3b9c6d89c3f4f"
 * }</pre>
 *
 * <p><strong>Custom Length:</strong>
 * <pre>{@code
 * HashGenerator gen = new HashGenerator();
 * String hash = gen.generate(16);  // "d96eb2e7e85c3f4f"
 * }</pre>
 *
 * <p><strong>Uppercase:</strong>
 * <pre>{@code
 * HashGenerator gen = new HashGenerator();
 * String hash = gen.generateUppercase();  // "D96EB2E7E85C3F4F4A7F0E51F5F3B9C6D89C3F4F"
 * String hash16 = gen.generateUppercase(16);  // "D96EB2E7E85C3F4F"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * HashGenerator gen = new HashGenerator(config);
 * String hash = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class HashGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;

    private static final int DEFAULT_LENGTH = 40;  // SHA-1 compatible
    private static final char[] HEX_CHARS_LOWER = "0123456789abcdef".toCharArray();
    private static final char[] HEX_CHARS_UPPER = "0123456789ABCDEF".toCharArray();

    /**
     * Creates a hash generator with default configuration.
     */
    public HashGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a hash generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public HashGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random 40-character hexadecimal hash string (lowercase).
     *
     * @return a 40-character hash string; never {@code null}
     */
    @Override
    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generates a random hexadecimal hash string of the specified length (lowercase).
     *
     * @param length the desired length; must be positive
     * @return a hash string of the specified length; never {@code null}
     * @throws IllegalArgumentException if {@code length} is not positive
     */
    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }
        return generateHash(length, HEX_CHARS_LOWER);
    }

    /**
     * Generates a random 40-character hexadecimal hash string (uppercase).
     *
     * @return a 40-character uppercase hash string; never {@code null}
     */
    public String generateUppercase() {
        return generateUppercase(DEFAULT_LENGTH);
    }

    /**
     * Generates a random hexadecimal hash string of the specified length (uppercase).
     *
     * @param length the desired length; must be positive
     * @return an uppercase hash string of the specified length; never {@code null}
     * @throws IllegalArgumentException if {@code length} is not positive
     */
    public String generateUppercase(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }
        return generateHash(length, HEX_CHARS_UPPER);
    }

    /**
     * Generates a hash string using the specified character set.
     *
     * @param length the length of the hash
     * @param chars the character set to use
     * @return a hash string
     */
    private String generateHash(int length, char[] chars) {
        StringBuilder hash = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            hash.append(chars[random.nextInt(16)]);
        }
        return hash.toString();
    }
}
