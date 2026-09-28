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
 * Generates Danish CPR number style identifiers in {@code DDMMYY-XXXX} format.
 *
 * <p>{@code DDMMYY} is a birth date between 1950 and 1999. As defined by CPR-kontoret, the first
 * sequence digit and the year together encode the birth century, so that digit is always 0–4 or 9,
 * which denote the 1900s for these years, and the sequence is never {@code 0000}. The remaining
 * sequence digits are random; no modulus-11 check digit is computed, which CPR-kontoret has not
 * guaranteed since October 2007.
 */
public final class DaDkNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("da", "DK");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        int sequence = random.nextInt(10_000);
        int centuryDigit = sequence / 1_000;
        if (centuryDigit >= 5 && centuryDigit <= 8) {
            // Century digits 5-8 denote births in 1858-1899 or 2000-2057; 0-3 denote the 1900s.
            sequence -= 5_000;
        }
        return String.format(Locale.ROOT, "%02d%02d%02d-%04d",
                             date.getDayOfMonth(),
                             date.getMonthValue(),
                             date.getYear() % 100,
                             Math.max(1, sequence));
    }
}
