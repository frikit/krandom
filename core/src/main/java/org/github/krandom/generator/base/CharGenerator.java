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
 * <p>Use the {@link Builder} for standard character groups:
 * <pre>{@code
 *   CharGenerator letters = CharGenerator.builder().uppercase().lowercase().build();
 *   CharGenerator digit   = CharGenerator.builder().digits().build();
 *   CharGenerator all     = CharGenerator.builder().uppercase().lowercase().digits().special().build();
 * }</pre>
 *
 * <p>Or use factory methods for common patterns:
 * <pre>{@code
 *   CharGenerator letters = CharGenerator.letters();       // A-Z, a-z
 *   CharGenerator digits = CharGenerator.digits();         // 0-9
 *   CharGenerator alphanum = CharGenerator.alphanumeric(); // A-Z, a-z, 0-9
 * }</pre>
 *
 * <p>For custom character pools, use {@link #pool(String)} or {@link #pool(char...)}:
 * <pre>{@code
 *   CharGenerator vowels = CharGenerator.pool("aeiou");
 *   CharGenerator hex = CharGenerator.pool("0123456789ABCDEF");
 *   CharGenerator binary = CharGenerator.pool('0', '1');
 * }</pre>
 *
 * <p>At least one character group must be enabled when using the builder;
 * the builder enforces this at {@link Builder#build()}.
 */
public final class CharGenerator implements Generator<Character> {

    private final char[]          pool;
    private final RandomGenerator random;

    private CharGenerator(char[] pool, RandomGenerator random) {
        this.pool = pool;
        this.random = random;
    }

    /**
     * Uppercase + lowercase letters; no digits or special characters.
     */
    public static CharGenerator letters() {
        return builder().uppercase().lowercase().build();
    }

    /**
     * Digits 0–9 only.
     */
    public static CharGenerator digits() {
        return builder().digits().build();
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Uppercase + lowercase + digits.
     */
    public static CharGenerator alphanumeric() {
        return builder().uppercase().lowercase().digits().build();
    }

    /**
     * All groups: letters, digits, and special characters.
     */
    public static CharGenerator all() {
        return builder().uppercase().lowercase().digits().special().build();
    }

    /**
     * Creates a generator that selects characters from a custom pool.
     *
     * <p>Examples:
     * <pre>{@code
     *   CharGenerator vowels = CharGenerator.pool("aeiou");
     *   CharGenerator consonants = CharGenerator.pool("bcdfghjklmnpqrstvwxyz");
     *   CharGenerator hex = CharGenerator.pool("0123456789ABCDEF");
     *   CharGenerator custom = CharGenerator.pool('X', 'Y', 'Z');
     * }</pre>
     *
     * @param characters custom character pool (must not be empty)
     * @return a generator that selects from the given pool
     * @throws IllegalArgumentException if characters is null or empty
     */
    public static CharGenerator pool(String characters) {
        if (characters == null || characters.isEmpty()) {
            throw new IllegalArgumentException("Character pool must not be null or empty");
        }
        return new CharGenerator(characters.toCharArray(), new SecureRandom());
    }

    /**
     * Creates a generator that selects characters from a custom pool.
     *
     * <p>Examples:
     * <pre>{@code
     *   CharGenerator binary = CharGenerator.pool('0', '1');
     *   CharGenerator arrows = CharGenerator.pool('←', '↑', '→', '↓');
     * }</pre>
     *
     * @param characters custom character pool (must not be empty)
     * @return a generator that selects from the given pool
     * @throws IllegalArgumentException if characters is null or empty
     */
    public static CharGenerator pool(char... characters) {
        if (characters == null || characters.length == 0) {
            throw new IllegalArgumentException("Character pool must not be null or empty");
        }
        return new CharGenerator(characters.clone(), new SecureRandom());
    }

    /**
     * Creates a seeded generator that selects characters from a custom pool.
     *
     * <p>Useful for deterministic testing with custom character sets.
     *
     * @param seed       the random seed
     * @param characters custom character pool (must not be empty)
     * @return a seeded generator that selects from the given pool
     * @throws IllegalArgumentException if characters is null or empty
     */
    public static CharGenerator pool(long seed, String characters) {
        if (characters == null || characters.isEmpty()) {
            throw new IllegalArgumentException("Character pool must not be null or empty");
        }
        return new CharGenerator(characters.toCharArray(), new Random(seed));
    }

    /**
     * Creates a seeded generator that selects characters from a custom pool.
     *
     * @param seed       the random seed
     * @param characters custom character pool (must not be empty)
     * @return a seeded generator that selects from the given pool
     * @throws IllegalArgumentException if characters is null or empty
     */
    public static CharGenerator pool(long seed, char... characters) {
        if (characters == null || characters.length == 0) {
            throw new IllegalArgumentException("Character pool must not be null or empty");
        }
        return new CharGenerator(characters.clone(), new Random(seed));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Character generate() {
        return pool[random.nextInt(pool.length)];
    }

    /**
     * Returns a new generator with the same character pool and a deterministic seed.
     *
     * <p>This keeps the current pool intact while making output reproducible.
     *
     * @param seed the random seed
     * @return a new generator with identical pool configuration and seeded randomness
     */
    public CharGenerator withSeed(long seed) {
        return new CharGenerator(pool.clone(), new Random(seed));
    }

    // ── Builder ───────────────────────────────────────────────────────────────


    public static final class Builder {

        private boolean includeUppercase = false;
        private boolean includeLowercase = false;
        private boolean includeDigits    = false;
        private boolean includeSpecial   = false;
        private Long    seed             = null;

        /**
         * Add uppercase ASCII letters A–Z.
         */
        public Builder uppercase() {
            this.includeUppercase = true;
            return this;
        }

        /**
         * Add lowercase ASCII letters a–z.
         */
        public Builder lowercase() {
            this.includeLowercase = true;
            return this;
        }

        /**
         * Add decimal digits 0–9.
         */
        public Builder digits() {
            this.includeDigits = true;
            return this;
        }

        /**
         * Add printable special characters ({@code !@#$%^&*...}).
         */
        public Builder special() {
            this.includeSpecial = true;
            return this;
        }

        /**
         * Fix the PRNG seed for reproducible output.
         */
        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public CharGenerator build() {
            List<Character> chars = new ArrayList<>();
            if (includeUppercase) for (char c = 'A'; c <= 'Z'; c++) chars.add(c);
            if (includeLowercase) for (char c = 'a'; c <= 'z'; c++) chars.add(c);
            if (includeDigits) for (char c = '0'; c <= '9'; c++) chars.add(c);
            if (includeSpecial) {
                for (String s : new String[] { "!", "@", "#", "$", "%", "^", "&", "*",
                                               "(", ")", "-", "_", "=", "+", "[", "]",
                                               "{", "}", "|", ";", ":", ",", ".", "<",
                                               ">", "?", "/" }) {
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
