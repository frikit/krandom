/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Generates Brazilian company taxpayer registry numbers (CNPJ).
 *
 * <p>A CNPJ has 14 digits: an 8-digit base, a 4-digit branch (this generator uses the headquarters
 * branch {@code 0001}), and 2 verifier digits computed with the standard double mod-11 algorithm.
 * The default form is dotted ({@code "NN.NNN.NNN/NNNN-NN"}); use {@link #withoutFormatting()} for the
 * bare 14-digit form.
 *
 * <pre>{@code
 *   String cnpj = new CnpjGenerator().generate();                    // "12.345.678/0001-95"
 *   String bare = new CnpjGenerator().withoutFormatting().generate(); // "12345678000195"
 * }</pre>
 */
public final class CnpjGenerator implements Generator<String> {

    /** Weights for the first verifier digit, applied to the 12 body digits. */
    private static final int[] WEIGHTS_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    /** Weights for the second verifier digit, applied to the 13 digits (body + first verifier). */
    private static final int[] WEIGHTS_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private final Random random;
    private final boolean formatted;

    /**
     * Creates a generator that produces formatted CNPJs using the default configuration.
     */
    public CnpjGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public CnpjGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.formatted = true;
    }

    private CnpjGenerator(Random random, boolean formatted) {
        this.random = random;
        this.formatted = formatted;
    }

    /**
     * Returns a generator that emits the bare 14-digit form (no dots, slash, or dash), sharing this
     * generator's random source.
     *
     * @return a new generator configured to omit formatting characters
     */
    public CnpjGenerator withoutFormatting() {
        return new CnpjGenerator(this.random, false);
    }

    /**
     * Computes a CNPJ verifier digit for the given digits using the supplied weights.
     *
     * @param digits  the digits to weigh
     * @param weights the per-position weights; must be at least as long as {@code digits}
     * @return the verifier digit in {@code [0, 9]}
     */
    static int checkDigit(int[] digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i] * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @Override
    public String generate() {
        int[] body = new int[12];
        for (int i = 0; i < 8; i++) {
            body[i] = random.nextInt(10);
        }
        // Headquarters branch "0001".
        body[8] = 0;
        body[9] = 0;
        body[10] = 0;
        body[11] = 1;

        int v1 = checkDigit(body, WEIGHTS_1);
        int[] body13 = Arrays.copyOf(body, 13);
        body13[12] = v1;
        int v2 = checkDigit(body13, WEIGHTS_2);

        StringBuilder digits = new StringBuilder(14);
        for (int d : body) {
            digits.append(d);
        }
        digits.append(v1).append(v2);
        String s = digits.toString();

        if (formatted) {
            return s.substring(0, 2) + '.' + s.substring(2, 5) + '.' + s.substring(5, 8)
                + '/' + s.substring(8, 12) + '-' + s.substring(12, 14);
        }
        return s;
    }
}
