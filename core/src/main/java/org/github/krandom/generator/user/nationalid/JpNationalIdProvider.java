/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Japanese My Number (個人番号, kojin bangō) identifiers.
 *
 * <p>My Number consists of 12 digits: 11 random digits (first is 1–9) followed by a check digit
 * computed using a weighted sum modulo 11:
 *
 * <pre>
 *   weights = {6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2}
 *   sum = sum(digit[i] * weight[i]) for i = 0..10
 *   Q = 11 - (sum % 11)
 *   check_digit = (Q &gt;= 10) ? 0 : Q
 * </pre>
 *
 * <p>Example: {@code "123456789018"}
 */
public final class JpNationalIdProvider implements NationalIdProvider {

    private static final int[] WEIGHTS = {6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    /** Creates a provider for Japanese My Number identifiers. */
    public JpNationalIdProvider() {}

    @Override
    public Locale getLocale() {
        return Locale.JAPAN;
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[11];
        digits[0] = random.nextInt(9) + 1;
        for (int i = 1; i < 11; i++) {
            digits[i] = random.nextInt(10);
        }
        int checkDigit = computeCheckDigit(digits);

        StringBuilder sb = new StringBuilder(12);
        for (int d : digits) {
            sb.append(d);
        }
        sb.append(checkDigit);
        return sb.toString();
    }

    /**
     * Computes the My Number check digit from the given 11-digit array.
     *
     * <p>Uses the formula: {@code Q = 11 - (sum % 11); return Q >= 10 ? 0 : Q}.
     *
     * @param digits array of exactly 11 integers, each in [0, 9]; first element must be in [1, 9]
     * @return check digit in [0, 9]
     */
    static int computeCheckDigit(int[] digits) {
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            sum += digits[i] * WEIGHTS[i];
        }
        int q = 11 - (sum % 11);
        return (q >= 10) ? 0 : q;
    }
}
