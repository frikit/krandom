/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Turkish Republic Identification Number style identifiers.
 *
 * <p>The first digit is non-zero and the last two digits follow the public TCKN checksum rules.
 */
public final class TrNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("tr", "TR");
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[11];
        digits[0] = random.nextInt(9) + 1;
        for (int i = 1; i < 9; i++) {
            digits[i] = random.nextInt(10);
        }
        digits[9] = computeTenthDigit(digits);
        digits[10] = computeEleventhDigit(digits);
        return String.format("%d%d%d%d%d%d%d%d%d%d%d",
                             digits[0], digits[1], digits[2], digits[3], digits[4],
                             digits[5], digits[6], digits[7], digits[8], digits[9],
                             digits[10]);
    }

    static int computeTenthDigit(int[] digits) {
        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];
        return Math.floorMod(oddSum * 7 - evenSum, 10);
    }

    static int computeEleventhDigit(int[] digits) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i];
        }
        return sum % 10;
    }
}
