/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Indian Aadhaar style identifiers.
 *
 * <p>The first eleven digits are synthetic and the final digit is computed with the Verhoeff
 * checksum algorithm used by Aadhaar numbers.
 */
public final class HiInNationalIdProvider implements NationalIdProvider {

    private static final int[][] D = {
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
        { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 },
        { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 },
        { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 },
        { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
        { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 },
        { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 },
        { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
        { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 },
        { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }
    };
    private static final int[][] P = {
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
        { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 },
        { 5, 8, 0, 3, 7, 9, 6, 1, 4, 2 },
        { 8, 9, 1, 6, 0, 4, 3, 5, 2, 7 },
        { 9, 4, 5, 3, 1, 2, 6, 8, 7, 0 },
        { 4, 2, 8, 6, 5, 7, 3, 9, 0, 1 },
        { 2, 7, 9, 3, 8, 0, 6, 4, 1, 5 },
        { 7, 0, 4, 6, 9, 1, 3, 2, 5, 8 }
    };
    private static final int[] INV = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

    @Override
    public Locale getLocale() {
        return Locale.of("hi", "IN");
    }

    @Override
    public String generate(Random random) {
        StringBuilder builder = new StringBuilder(12);
        builder.append(random.nextInt(8) + 2);
        for (int i = 1; i < 11; i++) {
            builder.append(random.nextInt(10));
        }
        builder.append(computeCheckDigit(builder));
        return builder.toString();
    }

    static int computeCheckDigit(CharSequence value) {
        int c = 0;
        int position = 1;
        for (int i = value.length() - 1; i >= 0; i--) {
            int digit = value.charAt(i) - '0';
            c = D[c][P[position % 8][digit]];
            position++;
        }
        return INV[c];
    }
}
