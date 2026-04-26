/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Dutch Burgerservicenummer (BSN) style identifiers.
 *
 * <p>The generated 9-digit value satisfies the Dutch 11-test checksum:
 * {@code 9*d0 + 8*d1 + ... + 2*d7 - d8} is divisible by 11.
 */
public final class NlNationalIdProvider implements NationalIdProvider {

    private static final int[][] CHECK_PAIRS = {
        { 0, 0 }, { 0, 1 }, { 0, 2 }, { 0, 3 }, { 0, 4 }, { 0, 5 },
        { 0, 6 }, { 0, 7 }, { 0, 8 }, { 0, 9 }, { 1, 1 }
    };

    @Override
    public Locale getLocale() {
        return Locale.of("nl", "NL");
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[9];
        digits[0] = random.nextInt(9) + 1;
        for (int i = 1; i < 7; i++) {
            digits[i] = random.nextInt(10);
        }
        int residue = (9 * digits[0]
                       + 8 * digits[1]
                       + 7 * digits[2]
                       + 6 * digits[3]
                       + 5 * digits[4]
                       + 4 * digits[5]
                       + 3 * digits[6]) % 11;
        digits[7] = CHECK_PAIRS[residue][0];
        digits[8] = CHECK_PAIRS[residue][1];
        return String.format("%d%d%d%d%d%d%d%d%d",
                             digits[0], digits[1], digits[2], digits[3], digits[4],
                             digits[5], digits[6], digits[7], digits[8]);
    }
}
