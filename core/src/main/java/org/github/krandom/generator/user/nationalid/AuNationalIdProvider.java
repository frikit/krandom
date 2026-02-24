/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Australian Tax File Numbers (TFNs).
 *
 * <p>A TFN consists of 9 digits formatted as {@code NNN NNN NNN}. The digits satisfy a mod-11
 * weighted checksum where the weighted sum of all 9 digits (weights: 1,4,3,7,5,8,6,9,10) must
 * produce a non-zero remainder when divided by 11.
 *
 * <p>Example: {@code "123 456 782"}
 */
public final class AuNationalIdProvider implements NationalIdProvider {

    private static final int[] WEIGHTS = {1, 4, 3, 7, 5, 8, 6, 9, 10};

    /** Creates a provider for Australian TFNs. */
    public AuNationalIdProvider() {}

    @Override
    public Locale getLocale() {
        return Locale.of("en", "AU");
    }

    @Override
    public String generate(Random random) {
        int[] digits;
        do {
            digits = new int[9];
            digits[0] = random.nextInt(9) + 1;
            for (int i = 1; i < 9; i++) {
                digits[i] = random.nextInt(10);
            }
        } while (computeRemainder(digits) == 0);

        return String.format("%d%d%d %d%d%d %d%d%d",
                digits[0], digits[1], digits[2],
                digits[3], digits[4], digits[5],
                digits[6], digits[7], digits[8]);
    }

    /**
     * Computes the mod-11 weighted remainder for the given 9-digit array.
     *
     * <p>A non-zero remainder indicates a valid TFN checksum.
     *
     * @param digits array of exactly 9 digits
     * @return weighted sum modulo 11
     */
    static int computeRemainder(int[] digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += digits[i] * WEIGHTS[i];
        }
        return sum % 11;
    }
}
