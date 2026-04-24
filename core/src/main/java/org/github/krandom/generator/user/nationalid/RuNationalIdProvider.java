/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Russian SNILS style identifiers in {@code NNN-NNN-NNN CC} format.
 *
 * <p>The final two digits are computed from the common weighted SNILS checksum rule.
 */
public final class RuNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("ru", "RU");
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[9];
        for (int i = 0; i < digits.length; i++) {
            digits[i] = random.nextInt(10);
        }
        int check = computeCheckDigits(digits);
        return String.format("%d%d%d-%d%d%d-%d%d%d %02d",
                             digits[0], digits[1], digits[2],
                             digits[3], digits[4], digits[5],
                             digits[6], digits[7], digits[8],
                             check);
    }

    static int computeCheckDigits(int[] digits) {
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i] * (9 - i);
        }
        return (sum % 101) % 100;
    }
}
