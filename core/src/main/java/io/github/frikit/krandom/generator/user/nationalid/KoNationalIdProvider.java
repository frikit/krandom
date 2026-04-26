/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Random;

/**
 * Generates Korean resident registration number style identifiers.
 *
 * <p>Values use a synthetic 1980-1999 birth date, a 1/2 gender-century digit, five random
 * digits, and the standard final checksum digit.
 */
public final class KoNationalIdProvider implements NationalIdProvider {

    private static final int[] WEIGHTS = { 2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5 };

    @Override
    public Locale getLocale() {
        return Locale.of("ko", "KR");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1980, 1, 1).plusDays(random.nextInt(7_305));
        int[] digits = new int[13];
        digits[0] = (date.getYear() % 100) / 10;
        digits[1] = date.getYear() % 10;
        digits[2] = date.getMonthValue() / 10;
        digits[3] = date.getMonthValue() % 10;
        digits[4] = date.getDayOfMonth() / 10;
        digits[5] = date.getDayOfMonth() % 10;
        digits[6] = random.nextInt(2) + 1;
        for (int i = 7; i < 12; i++) {
            digits[i] = random.nextInt(10);
        }
        digits[12] = computeCheckDigit(digits);
        return String.format("%d%d%d%d%d%d-%d%d%d%d%d%d%d",
                             digits[0], digits[1], digits[2], digits[3], digits[4], digits[5],
                             digits[6], digits[7], digits[8], digits[9], digits[10], digits[11],
                             digits[12]);
    }

    static int computeCheckDigit(int[] digits) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += digits[i] * WEIGHTS[i];
        }
        return (11 - (sum % 11)) % 10;
    }
}
