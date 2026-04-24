/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Polish PESEL style identifiers.
 *
 * <p>The first ten digits are synthetic; the eleventh digit is the standard PESEL checksum.
 */
public final class PlNationalIdProvider implements NationalIdProvider {

    private static final int[] WEIGHTS = { 1, 3, 7, 9, 1, 3, 7, 9, 1, 3 };

    @Override
    public Locale getLocale() {
        return Locale.of("pl", "PL");
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[11];
        for (int i = 0; i < 10; i++) {
            digits[i] = random.nextInt(10);
        }
        digits[10] = computeCheckDigit(digits);
        return String.format("%d%d%d%d%d%d%d%d%d%d%d",
                             digits[0], digits[1], digits[2], digits[3], digits[4],
                             digits[5], digits[6], digits[7], digits[8], digits[9],
                             digits[10]);
    }

    static int computeCheckDigit(int[] digits) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += digits[i] * WEIGHTS[i];
        }
        return (10 - (sum % 10)) % 10;
    }
}
