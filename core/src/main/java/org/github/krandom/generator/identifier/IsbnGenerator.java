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
 * Generates syntactically valid ISBN-10 and ISBN-13 numbers (no hyphens).
 *
 * <p>By default the generator produces ISBN-13 values (GS1 prefix {@code 978} or {@code 979}).
 * Use {@link IsbnType#ISBN_10} to generate the legacy 10-digit format.
 *
 * <pre>{@code
 * // ISBN-13 (default)
 * IsbnGenerator gen13 = new IsbnGenerator();
 * String isbn13 = gen13.generate();           // "9781234567897"
 *
 * // ISBN-10
 * IsbnGenerator gen10 = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10);
 * String isbn10 = gen10.generate();           // "0306406152"  or  "159059384X"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * IsbnGenerator gen = new IsbnGenerator(config);
 * String isbn = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class IsbnGenerator implements Generator<String> {

    /** Selects between the ISBN-10 and ISBN-13 formats. */
    public enum IsbnType { ISBN_10, ISBN_13 }

    private final IsbnType type;
    private final GeneratorConfig config;
    private final Random random;

    /** Creates an ISBN-13 generator with default configuration. */
    public IsbnGenerator() {
        this(IsbnType.ISBN_13, GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the specified ISBN type with default configuration.
     *
     * @param type the ISBN format; must not be {@code null}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public IsbnGenerator(IsbnType type) {
        this(type, GeneratorConfig.defaults());
    }

    /**
     * Creates an ISBN-13 generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public IsbnGenerator(GeneratorConfig config) {
        this(IsbnType.ISBN_13, config);
    }

    /**
     * Creates a generator for the specified ISBN type and configuration.
     *
     * @param type   the ISBN format; must not be {@code null}
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public IsbnGenerator(IsbnType type, GeneratorConfig config) {
        this.type   = Objects.requireNonNull(type,   "type must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a valid ISBN string in the configured format (no hyphens).
     *
     * @return an ISBN string; never {@code null}
     */
    @Override
    public String generate() {
        return type == IsbnType.ISBN_10 ? generateIsbn10() : generateIsbn13();
    }

    /**
     * Generates a valid 10-character ISBN-10 string.
     * The check character is either a digit {@code '0'–'9'} or {@code 'X'} (representing 10).
     *
     * @return a 10-character ISBN-10 string; never {@code null}
     */
    public String generateIsbn10() {
        int[] digits = new int[9];
        for (int i = 0; i < 9; i++) {
            digits[i] = random.nextInt(10);
        }
        int check = computeIsbn10Check(digits);
        StringBuilder sb = new StringBuilder(10);
        for (int d : digits) sb.append(d);
        sb.append(check == 10 ? 'X' : (char) ('0' + check));
        return sb.toString();
    }

    /**
     * Generates a valid 13-character ISBN-13 string.
     * The first three digits form the GS1 prefix ({@code 978} or {@code 979}).
     *
     * @return a 13-character ISBN-13 string; never {@code null}
     */
    public String generateIsbn13() {
        int[] digits = new int[12];
        int prefix = random.nextBoolean() ? 978 : 979;
        digits[0] = prefix / 100;
        digits[1] = (prefix / 10) % 10;
        digits[2] = prefix % 10;
        for (int i = 3; i < 12; i++) {
            digits[i] = random.nextInt(10);
        }
        int check = computeIsbn13Check(digits);
        StringBuilder sb = new StringBuilder(13);
        for (int d : digits) sb.append(d);
        sb.append(check);
        return sb.toString();
    }

    /** Returns the {@link IsbnType} this generator was configured with. */
    public IsbnType getType() {
        return type;
    }

    // ── Package-private check-digit helpers (exposed for direct branch testing) ──

    /**
     * Computes the ISBN-10 check digit for the given 9-digit array.
     *
     * @param digits nine digits [0–9]; must not be {@code null}
     * @return check value in [0, 10]; value 10 is represented as {@code 'X'} in the ISBN string
     */
    static int computeIsbn10Check(int[] digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * digits[i];
        }
        return (11 - (sum % 11)) % 11;
    }

    /**
     * Computes the ISBN-13 check digit for the given 12-digit array.
     *
     * @param digits twelve digits [0–9]; must not be {@code null}
     * @return check digit in [0, 9]
     */
    static int computeIsbn13Check(int[] digits) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += digits[i] * (i % 2 == 0 ? 1 : 3);
        }
        return (10 - (sum % 10)) % 10;
    }
}
