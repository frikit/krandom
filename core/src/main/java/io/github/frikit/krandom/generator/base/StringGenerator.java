/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random {@link String} values by composing a {@link CharGenerator}.
 *
 * <p>Use factory methods for common patterns:
 * <pre>{@code
 *   StringGenerator letters = StringGenerator.letters();       // A-Z, a-z, 5-20 chars
 *   StringGenerator digits = StringGenerator.digits();         // 0-9, 5-20 chars
 *   StringGenerator alphanum = StringGenerator.alphanumeric(); // A-Z, a-z, 0-9, 5-20 chars
 * }</pre>
 *
 * <p>For custom character pools:
 * <pre>{@code
 *   StringGenerator vowels = StringGenerator.pool("aeiou");         // 5-20 chars from pool
 *   StringGenerator hex = StringGenerator.pool("0123456789ABCDEF", 8); // 8 hex chars
 *   StringGenerator code = StringGenerator.pool("ABC123", 4, 8);    // 4-8 chars from pool
 * }</pre>
 *
 * <p>Or use the {@link Builder} for full control:
 * <pre>{@code
 *   // Alphanumeric string, 8–16 characters
 *   StringGenerator gen = StringGenerator.builder()
 *       .minLength(8)
 *       .maxLength(16)
 *       .charGenerator(CharGenerator.alphanumeric())
 *       .build();
 *
 *   // Fixed-length uppercase PIN
 *   StringGenerator pin = StringGenerator.builder()
 *       .length(4)
 *       .charGenerator(CharGenerator.digits())
 *       .build();
 * }</pre>
 */
public final class StringGenerator implements Generator<String> {

    private final CharGenerator   charGenerator;
    private final int             minLength;
    private final int             maxLength;
    private final RandomGenerator random;

    private StringGenerator(Builder b) {
        if (b.seed != null) {
            // Preserve configured character pool while making output deterministic.
            this.charGenerator = b.charGenerator.withSeed(b.seed);
            this.random = new Random(b.seed);
        } else {
            this.charGenerator = b.charGenerator;
            this.random = new SecureRandom();
        }
        this.minLength = b.minLength;
        this.maxLength = b.maxLength;
    }

    /**
     * Letters only, 5–20 characters.
     */
    public static StringGenerator letters() {
        return builder().charGenerator(CharGenerator.letters()).build();
    }

    // ── Convenience factories ─────────────────────────────────────────────────

    /**
     * Letters only, with a specified length range.
     *
     * @param minLength minimum length (inclusive, must be &gt;= 1)
     * @param maxLength maximum length (inclusive, must be &gt;= {@code minLength})
     * @return a generator producing letter strings in the given length range
     */
    public static StringGenerator letters(int minLength, int maxLength) {
        return builder().charGenerator(CharGenerator.letters()).minLength(minLength).maxLength(maxLength).build();
    }

    /**
     * Digits only, 5–20 characters.
     */
    public static StringGenerator digits() {
        return builder().charGenerator(CharGenerator.digits()).build();
    }

    /**
     * Alphanumeric (letters + digits), 5–20 characters.
     */
    public static StringGenerator alphanumeric() {
        return builder().charGenerator(CharGenerator.alphanumeric()).build();
    }

    /**
     * All character groups, 5–20 characters.
     */
    public static StringGenerator all() {
        return builder().charGenerator(CharGenerator.all()).build();
    }

    /**
     * Generates strings from a custom character pool with variable length (5-20).
     *
     * <p>Examples:
     * <pre>{@code
     *   StringGenerator vowels = StringGenerator.pool("aeiou");
     *   StringGenerator hex = StringGenerator.pool("0123456789ABCDEF");
     *   StringGenerator binary = StringGenerator.pool("01");
     * }</pre>
     *
     * @param characters custom character pool (must not be null or empty)
     * @return a generator that creates strings from the given pool
     * @throws IllegalArgumentException if characters is null or empty
     */
    public static StringGenerator pool(String characters) {
        return builder()
            .charGenerator(CharGenerator.pool(characters))
            .build();
    }

    /**
     * Generates fixed-length strings from a custom character pool.
     *
     * <p>Examples:
     * <pre>{@code
     *   StringGenerator code = StringGenerator.pool("ABC123", 8);    // 8 chars from pool
     *   StringGenerator id = StringGenerator.pool("0123456789", 6);  // 6-digit numeric ID
     * }</pre>
     *
     * @param characters custom character pool (must not be null or empty)
     * @param length     fixed string length (must be &gt;= 1)
     * @return a generator that creates fixed-length strings from the given pool
     * @throws IllegalArgumentException if characters is null/empty or length &lt; 1
     */
    public static StringGenerator pool(String characters, int length) {
        return builder()
            .charGenerator(CharGenerator.pool(characters))
            .length(length)
            .build();
    }

    /**
     * Generates variable-length strings from a custom character pool.
     *
     * <p>Examples:
     * <pre>{@code
     *   StringGenerator code = StringGenerator.pool("ABCDEF", 4, 8);  // 4-8 chars
     *   StringGenerator id = StringGenerator.pool("xyz", 2, 5);       // 2-5 chars
     * }</pre>
     *
     * @param characters custom character pool (must not be null or empty)
     * @param minLength  minimum string length (must be >= 1)
     * @param maxLength  maximum string length (must be >= minLength)
     * @return a generator that creates variable-length strings from the given pool
     * @throws IllegalArgumentException if validation fails
     */
    public static StringGenerator pool(String characters, int minLength, int maxLength) {
        return builder()
            .charGenerator(CharGenerator.pool(characters))
            .minLength(minLength)
            .maxLength(maxLength)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String generate() {
        int length = (minLength == maxLength)
                     ? minLength
                     : random.nextInt(minLength, maxLength + 1);

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charGenerator.generate());
        }
        return sb.toString();
    }

    // ── Builder ───────────────────────────────────────────────────────────────


    public static final class Builder {

        private CharGenerator charGenerator = CharGenerator.letters();
        private int           minLength     = 5;
        private int           maxLength     = 20;
        private Long          seed          = null;

        /**
         * Character source for string generation; defaults to {@link CharGenerator#letters()}.
         */
        public Builder charGenerator(CharGenerator charGenerator) {
            this.charGenerator = Objects.requireNonNull(charGenerator, "charGenerator");
            return this;
        }

        /**
         * Fixed length — equivalent to calling {@code minLength(n).maxLength(n)}.
         */
        public Builder length(int length) {
            if (length < 1) throw new IllegalArgumentException("length must be >= 1, was: " + length);
            this.minLength = length;
            this.maxLength = length;
            return this;
        }

        /**
         * Minimum string length (inclusive); defaults to 5.
         */
        public Builder minLength(int min) {
            if (min < 1) throw new IllegalArgumentException("minLength must be >= 1, was: " + min);
            this.minLength = min;
            return this;
        }

        /**
         * Maximum string length (inclusive); defaults to 20.
         */
        public Builder maxLength(int max) {
            if (max < 1) throw new IllegalArgumentException("maxLength must be >= 1, was: " + max);
            this.maxLength = max;
            return this;
        }

        /**
         * Fix the PRNG seed for reproducible output.
         */
        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public StringGenerator build() {
            if (maxLength < minLength) {
                throw new IllegalArgumentException(
                    "maxLength (" + maxLength + ") must be >= minLength (" + minLength + ")");
            }
            return new StringGenerator(this);
        }
    }
}
