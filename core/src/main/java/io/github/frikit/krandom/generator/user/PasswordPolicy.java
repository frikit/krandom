/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable requirements for a generated password.
 *
 * <p>Requirements state the minimum number of characters to take from each character set.
 * All requirement sets participate in the password alphabet.
 */
public final class PasswordPolicy {

    /** Lowercase ASCII letters. */
    public static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    /** Uppercase ASCII letters. */
    public static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** ASCII digits. */
    public static final String DIGITS = "0123456789";
    /** Default punctuation symbols. */
    public static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.?";

    private final int                 minLength;
    private final int                 maxLength;
    private final List<Requirement>   requirements;
    private final String              alphabet;

    private PasswordPolicy(Builder builder) {
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.requirements = List.copyOf(builder.requirements);
        this.alphabet = alphabetFor(requirements);
        int requiredCharacters = requirements.stream().mapToInt(Requirement::minimumCount).sum();
        if (requiredCharacters > minLength) {
            throw new IllegalArgumentException(
                "required character count must be <= minLength, got: " + requiredCharacters + " > " + minLength);
        }
    }

    /**
     * Creates a password policy builder with the default 8–16 character range.
     *
     * @return policy builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the inclusive minimum length.
     *
     * @return minimum password length
     */
    public int minLength() {
        return minLength;
    }

    /**
     * Returns the inclusive maximum length.
     *
     * @return maximum password length
     */
    public int maxLength() {
        return maxLength;
    }

    List<Requirement> requirements() {
        return requirements;
    }

    String alphabet() {
        return alphabet;
    }

    private static String alphabetFor(List<Requirement> requirements) {
        Set<Character> characters = new LinkedHashSet<>();
        requirements.forEach(requirement -> requirement.symbols().chars().forEach(c -> characters.add((char) c)));
        if (characters.isEmpty()) {
            LOWERCASE.chars().forEach(c -> characters.add((char) c));
            UPPERCASE.chars().forEach(c -> characters.add((char) c));
            DIGITS.chars().forEach(c -> characters.add((char) c));
            SYMBOLS.chars().forEach(c -> characters.add((char) c));
        }
        StringBuilder result = new StringBuilder(characters.size());
        characters.forEach(result::append);
        return result.toString();
    }

    static final class Requirement {

        private final String symbols;
        private final int    minimumCount;

        Requirement(String symbols, int minimumCount) {
            this.symbols = symbols;
            this.minimumCount = minimumCount;
        }

        String symbols() {
            return symbols;
        }

        int minimumCount() {
            return minimumCount;
        }
    }

    /**
     * Builder for {@link PasswordPolicy}.
     */
    public static final class Builder {

        private int               minLength = 8;
        private int               maxLength = 16;
        private final List<Requirement> requirements = new ArrayList<>();

        private Builder() {
        }

        /**
         * Sets a fixed password length.
         *
         * @param length required length
         * @return this builder
         */
        public Builder length(int length) {
            return length(length, length);
        }

        /**
         * Sets an inclusive password length range.
         *
         * @param min inclusive minimum length
         * @param max inclusive maximum length
         * @return this builder
         */
        public Builder length(int min, int max) {
            if (min <= 0) {
                throw new IllegalArgumentException("min length must be positive, got: " + min);
            }
            if (max < min) {
                throw new IllegalArgumentException("max length must be >= min length, got: " + max + " < " + min);
            }
            minLength = min;
            maxLength = max;
            return this;
        }

        /**
         * Requires lowercase characters.
         *
         * @param minimumCount required count
         * @return this builder
         */
        public Builder requireLowercase(int minimumCount) {
            return require(LOWERCASE, minimumCount);
        }

        /**
         * Requires uppercase characters.
         *
         * @param minimumCount required count
         * @return this builder
         */
        public Builder requireUppercase(int minimumCount) {
            return require(UPPERCASE, minimumCount);
        }

        /**
         * Requires digits.
         *
         * @param minimumCount required count
         * @return this builder
         */
        public Builder requireDigits(int minimumCount) {
            return require(DIGITS, minimumCount);
        }

        /**
         * Requires characters from a custom symbol set.
         *
         * @param symbols character set
         * @param minimumCount required count
         * @return this builder
         */
        public Builder requireSymbols(String symbols, int minimumCount) {
            return require(symbols, minimumCount);
        }

        /**
         * Requires characters from a custom character set.
         *
         * @param symbols character set
         * @param minimumCount required count
         * @return this builder
         */
        public Builder require(String symbols, int minimumCount) {
            Objects.requireNonNull(symbols, "symbols must not be null");
            if (symbols.isEmpty()) {
                throw new IllegalArgumentException("symbols must not be empty");
            }
            if (minimumCount < 0) {
                throw new IllegalArgumentException("minimumCount must be >= 0, got: " + minimumCount);
            }
            requirements.add(new Requirement(symbols, minimumCount));
            return this;
        }

        /**
         * Creates an immutable policy.
         *
         * @return policy
         */
        public PasswordPolicy build() {
            return new PasswordPolicy(this);
        }
    }
}
