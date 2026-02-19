/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random {@link Character} values drawn from a configurable character pool.
 *
 * <p>Use the {@link Builder} for readable configuration:
 * <pre>{@code
 *   CharGenerator letters = CharGenerator.builder().uppercase().lowercase().build();
 *   CharGenerator digit   = CharGenerator.builder().digits().build();
 *   CharGenerator all     = CharGenerator.builder().uppercase().lowercase().digits().special().build();
 * }</pre>
 *
 * <p>At least one character group must be enabled; the builder enforces this at {@link Builder#build()}.
 */
public final class CharGenerator implements Generator<Character> {

    private final char[] pool;
    private final RandomGenerator random;

    private CharGenerator(char[] pool, RandomGenerator random) {
        this.pool   = pool;
        this.random = random;
    }

    @Override
    public Character generate() {
        return pool[random.nextInt(pool.length)];
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /** Uppercase + lowercase letters; no digits or special characters. */
    public static CharGenerator letters() {
        return builder().uppercase().lowercase().build();
    }

    /** Digits 0–9 only. */
    public static CharGenerator digits() {
        return builder().digits().build();
    }

    /** Uppercase + lowercase + digits. */
    public static CharGenerator alphanumeric() {
        return builder().uppercase().lowercase().digits().build();
    }

    /** All groups: letters, digits, and special characters. */
    public static CharGenerator all() {
        return builder().uppercase().lowercase().digits().special().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        private boolean includeUppercase = false;
        private boolean includeLowercase = false;
        private boolean includeDigits    = false;
        private boolean includeSpecial   = false;
        private Long    seed             = null;

        /** Add uppercase ASCII letters A–Z. */
        public Builder uppercase() { this.includeUppercase = true; return this; }

        /** Add lowercase ASCII letters a–z. */
        public Builder lowercase() { this.includeLowercase = true; return this; }

        /** Add decimal digits 0–9. */
        public Builder digits()    { this.includeDigits    = true; return this; }

        /** Add printable special characters ({@code !@#$%^&*...}). */
        public Builder special()   { this.includeSpecial   = true; return this; }

        /** Fix the PRNG seed for reproducible output. */
        public Builder seed(long seed) { this.seed = seed; return this; }

        public CharGenerator build() {
            List<Character> chars = new ArrayList<>();
            if (includeUppercase) for (char c = 'A'; c <= 'Z'; c++) chars.add(c);
            if (includeLowercase) for (char c = 'a'; c <= 'z'; c++) chars.add(c);
            if (includeDigits)    for (char c = '0'; c <= '9'; c++) chars.add(c);
            if (includeSpecial) {
                for (String s : new String[]{"!", "@", "#", "$", "%", "^", "&", "*",
                                             "(", ")", "-", "_", "=", "+", "[", "]",
                                             "{", "}", "|", ";", ":", ",", ".", "<",
                                             ">", "?", "/"}) {
                    chars.add(s.charAt(0));
                }
            }
            if (chars.isEmpty()) {
                throw new IllegalArgumentException(
                        "At least one character group must be enabled (uppercase, lowercase, digits, special)");
            }
            char[] pool = new char[chars.size()];
            for (int i = 0; i < chars.size(); i++) pool[i] = chars.get(i);

            RandomGenerator rng = seed != null ? new Random(seed) : new SecureRandom();
            return new CharGenerator(pool, rng);
        }
    }
}
