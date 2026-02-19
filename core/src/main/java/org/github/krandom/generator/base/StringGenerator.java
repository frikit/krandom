/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random {@link String} values by composing a {@link CharGenerator}.
 *
 * <p>Use the {@link Builder} for full control:
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
 *
 * <p>Or use the convenience factory methods on {@link StringGenerator} directly.
 */
public final class StringGenerator implements Generator<String> {

    private final CharGenerator charGenerator;
    private final int minLength;
    private final int maxLength;
    private final RandomGenerator random;

    private StringGenerator(Builder b) {
        this.charGenerator = b.charGenerator;
        this.minLength     = b.minLength;
        this.maxLength     = b.maxLength;
        this.random        = b.seed != null ? new Random(b.seed) : new SecureRandom();
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

    // ── Convenience factories ─────────────────────────────────────────────────

    /** Letters only, 5–20 characters. */
    public static StringGenerator letters() {
        return builder().charGenerator(CharGenerator.letters()).build();
    }

    /** Digits only, 5–20 characters. */
    public static StringGenerator digits() {
        return builder().charGenerator(CharGenerator.digits()).build();
    }

    /** Alphanumeric (letters + digits), 5–20 characters. */
    public static StringGenerator alphanumeric() {
        return builder().charGenerator(CharGenerator.alphanumeric()).build();
    }

    /** All character groups, 5–20 characters. */
    public static StringGenerator all() {
        return builder().charGenerator(CharGenerator.all()).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        private CharGenerator charGenerator = CharGenerator.letters();
        private int  minLength = 5;
        private int  maxLength = 20;
        private Long seed      = null;

        /** Character source for string generation; defaults to {@link CharGenerator#letters()}. */
        public Builder charGenerator(CharGenerator charGenerator) {
            this.charGenerator = Objects.requireNonNull(charGenerator, "charGenerator");
            return this;
        }

        /** Fixed length — equivalent to calling {@code minLength(n).maxLength(n)}. */
        public Builder length(int length) {
            if (length < 1) throw new IllegalArgumentException("length must be >= 1, was: " + length);
            this.minLength = length;
            this.maxLength = length;
            return this;
        }

        /** Minimum string length (inclusive); defaults to 5. */
        public Builder minLength(int min) {
            if (min < 1) throw new IllegalArgumentException("minLength must be >= 1, was: " + min);
            this.minLength = min;
            return this;
        }

        /** Maximum string length (inclusive); defaults to 20. */
        public Builder maxLength(int max) {
            if (max < 1) throw new IllegalArgumentException("maxLength must be >= 1, was: " + max);
            this.maxLength = max;
            return this;
        }

        /** Fix the PRNG seed for reproducible output. */
        public Builder seed(long seed) { this.seed = seed; return this; }

        public StringGenerator build() {
            if (maxLength < minLength) {
                throw new IllegalArgumentException(
                        "maxLength (" + maxLength + ") must be >= minLength (" + minLength + ")");
            }
            return new StringGenerator(this);
        }
    }
}
