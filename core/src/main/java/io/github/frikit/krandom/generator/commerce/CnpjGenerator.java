/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Generates Brazilian company taxpayer registry numbers (CNPJ).
 *
 * <p>The legacy numeric variant has 14 digits: an 8-digit base, a 4-digit branch (this generator
 * uses the headquarters branch {@code 0001}), and 2 verifier digits computed with the standard
 * double mod-11 algorithm. The default form is dotted ({@code "NN.NNN.NNN/NNNN-NN"}); use
 * {@link #withoutFormatting()} for the bare 14-character form or
 * {@link #withAlphanumericFormat()} for the documented alphanumeric shape.
 *
 * <p>Configured generation is disabled by default. For an isolated compatibility fixture, select
 * {@link BusinessTaxIdentifierSafetyPolicy#REALISTIC_UNCLASSIFIED} explicitly.
 */
public final class CnpjGenerator implements Generator<String> {

    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Weights for the first verifier digit, applied to the 12 body digits. */
    private static final int[] WEIGHTS_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    /** Weights for the second verifier digit, applied to the 13 digits (body + first verifier). */
    private static final int[] WEIGHTS_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private final Random random;
    private final boolean formatted;
    private final boolean alphanumericFormat;
    private final BusinessTaxIdentifierSafetyPolicy safetyPolicy;


    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public CnpjGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.formatted = true;
        this.alphanumericFormat = false;
        this.safetyPolicy = config.getBusinessTaxIdentifierSafetyPolicy();
    }

    private CnpjGenerator(Random random,
                          boolean formatted,
                          boolean alphanumericFormat,
                          BusinessTaxIdentifierSafetyPolicy safetyPolicy) {
        this.random = random;
        this.formatted = formatted;
        this.alphanumericFormat = alphanumericFormat;
        this.safetyPolicy = safetyPolicy;
    }

    /**
     * Returns a generator that emits the bare 14-character form (no dots, slash, or dash), sharing this
     * generator's random source.
     *
     * @return a new generator configured to omit formatting characters
     */
    public CnpjGenerator withoutFormatting() {
        return new CnpjGenerator(this.random, false, this.alphanumericFormat, this.safetyPolicy);
    }

    /**
     * Returns a generator for the documented alphanumeric CNPJ shape, sharing this generator's
     * random source and safety policy.
     *
     * <p>The first twelve positions contain uppercase ASCII letters and digits, with at least one
     * letter. The output uses the Receita Federal's published check-digit algorithm, but it is not
     * an assigned or fictitious CNPJ; generation still requires
     * {@link BusinessTaxIdentifierSafetyPolicy#REALISTIC_UNCLASSIFIED}.
     *
     * @return a generator configured for the alphanumeric CNPJ shape
     */
    public CnpjGenerator withAlphanumericFormat() {
        return new CnpjGenerator(this.random, this.formatted, true, this.safetyPolicy);
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
        requireRealisticOutput();
        String cnpj = alphanumericFormat ? generateAlphanumeric() : generateNumeric();
        return formatted ? format(cnpj) : cnpj;
    }

    private String generateNumeric() {
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
        return digits.toString();
    }

    private String generateAlphanumeric() {
        StringBuilder body = new StringBuilder(12);
        body.append((char) ('A' + random.nextInt(26)));
        for (int index = 1; index < 12; index++) {
            body.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }

        int firstVerifier = alphanumericCheckDigit(body, WEIGHTS_1);
        body.append(firstVerifier);
        int secondVerifier = alphanumericCheckDigit(body, WEIGHTS_2);
        return body.append(secondVerifier).toString();
    }

    private static int alphanumericCheckDigit(CharSequence characters, int[] weights) {
        int sum = 0;
        for (int index = 0; index < characters.length(); index++) {
            sum += (characters.charAt(index) - '0') * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static String format(String cnpj) {
        return cnpj.substring(0, 2) + '.' + cnpj.substring(2, 5) + '.' + cnpj.substring(5, 8)
            + '/' + cnpj.substring(8, 12) + '-' + cnpj.substring(12, 14);
    }

    private void requireRealisticOutput() {
        if (safetyPolicy == BusinessTaxIdentifierSafetyPolicy.DISABLED) {
            throw new IllegalStateException(
                "Business tax-identifier generation is disabled by default; select "
                + "businessTaxIdentifierSafetyPolicy(REALISTIC_UNCLASSIFIED) only for isolated fixtures");
        }
    }
}
