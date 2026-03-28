/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates German Tax Identification Numbers (Steuer-Identifikationsnummer).
 *
 * <p>The Steuer-ID consists of 11 digits: the first digit is 1–9 (never 0), digits 2–10 are
 * random 0–9, and digit 11 is a check digit computed using ISO 7064 Mod 11,10.
 *
 * <p>The check digit algorithm iterates through digits 1–10:
 * <pre>
 *   product = 10  (initial value)
 *   for each digit d:
 *     sum = (d + product) % 10
 *     if (sum == 0) sum = 10
 *     product = (2 * sum) % 11
 *   check_digit = 11 - product  (result must be in [1, 9])
 * </pre>
 *
 * <p>If the computed check digit equals 10, the first 10 digits are regenerated.
 *
 * <p>Example: {@code "86095742719"}
 */
public final class DeNationalIdProvider implements NationalIdProvider {

    /**
     * Creates a provider for German Steuer-ID numbers.
     */
    public DeNationalIdProvider() {
    }

    /**
     * Computes the ISO 7064 Mod 11,10 check digit for the first 10 elements of the given array.
     *
     * @param digits array of at least 10 integers, each in [0, 9]; first element must be in [1, 9]
     * @return check digit in [0, 10]; callers should regenerate if the result equals 10
     */
    static int computeCheckDigit(int[] digits) {
        int product = 10;
        for (int i = 0; i < 10; i++) {
            int sum = (digits[i] + product) % 10;
            if (sum == 0) sum = 10;
            product = (2 * sum) % 11;
        }
        return 11 - product;
    }

    @Override
    public Locale getLocale() {
        return Locale.GERMANY;
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[11];
        int checkDigit;
        do {
            digits[0] = random.nextInt(9) + 1;
            for (int i = 1; i < 10; i++) {
                digits[i] = random.nextInt(10);
            }
            checkDigit = computeCheckDigit(digits);
        } while (checkDigit == 10);
        digits[10] = checkDigit;

        StringBuilder sb = new StringBuilder(11);
        for (int d : digits) {
            sb.append(d);
        }
        return sb.toString();
    }
}
