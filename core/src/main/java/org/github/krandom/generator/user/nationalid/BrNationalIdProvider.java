/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Brazilian Individual Taxpayer Registry (CPF) numbers.
 *
 * <p>A CPF consists of 9 random digits followed by 2 verifier digits, formatted as
 * {@code NNN.NNN.NNN-NN}. The verifier digits are computed using a double mod-11 algorithm:
 *
 * <pre>
 *   v1 = 11 - (sum(digit[i] * (10 - i)) % 11)  for i = 0..8
 *   if v1 &gt;= 10 then v1 = 0
 *   v2 = 11 - ((sum(digit[i] * (11 - i)) + v1 * 2) % 11)  for i = 0..8
 *   if v2 &gt;= 10 then v2 = 0
 * </pre>
 *
 * <p>Use {@link #withoutFormatting()} to omit the dots and dash.
 *
 * <p>Example: {@code "123.456.789-09"} or {@code "12345678909"} without formatting.
 */
public final class BrNationalIdProvider implements NationalIdProvider {

    private final boolean formatted;

    /** Creates a provider that generates CPF numbers with formatting (e.g., {@code "123.456.789-09"}). */
    public BrNationalIdProvider() {
        this(true);
    }

    private BrNationalIdProvider(boolean formatted) {
        this.formatted = formatted;
    }

    /**
     * Returns a new provider that generates CPF numbers without dots and dash
     * (e.g., {@code "12345678909"}).
     *
     * @return a new provider configured to omit formatting characters
     */
    public BrNationalIdProvider withoutFormatting() {
        return new BrNationalIdProvider(false);
    }

    @Override
    public Locale getLocale() {
        return Locale.of("pt", "BR");
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[9];
        for (int i = 0; i < 9; i++) {
            digits[i] = random.nextInt(10);
        }
        int v1 = computeVerifier(digits, 10);
        int[] digits10 = new int[10];
        System.arraycopy(digits, 0, digits10, 0, 9);
        digits10[9] = v1;
        int v2 = computeVerifier(digits10, 11);

        if (formatted) {
            return String.format("%d%d%d.%d%d%d.%d%d%d-%d%d",
                    digits[0], digits[1], digits[2],
                    digits[3], digits[4], digits[5],
                    digits[6], digits[7], digits[8],
                    v1, v2);
        }
        return String.format("%d%d%d%d%d%d%d%d%d%d%d",
                digits[0], digits[1], digits[2],
                digits[3], digits[4], digits[5],
                digits[6], digits[7], digits[8],
                v1, v2);
    }

    /**
     * Computes a CPF verifier digit from the given digit array using the provided start weight.
     *
     * <p>The formula is:
     * <pre>
     *   sum = sum(digits[i] * (startWeight - i)) for i = 0..length-1
     *   result = 11 - (sum % 11)
     *   return result &gt;= 10 ? 0 : result
     * </pre>
     *
     * @param digits      array of digits to process
     * @param startWeight the initial weight (decrements by 1 for each subsequent digit)
     * @return the verifier digit, always in [0, 9]
     */
    static int computeVerifier(int[] digits, int startWeight) {
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i] * (startWeight - i);
        }
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }
}
