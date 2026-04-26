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
 * Generates Swedish personal identity number style identifiers.
 *
 * <p>Values are formatted as {@code YYYYMMDD-NNNC}; {@code C} is the Luhn checksum over
 * {@code YYMMDDNNN}.
 */
public final class SvNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("sv", "SE");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        int individual = random.nextInt(1_000);
        String body = String.format("%02d%02d%02d%03d",
                                    date.getYear() % 100,
                                    date.getMonthValue(),
                                    date.getDayOfMonth(),
                                    individual);
        int check = luhnCheckDigit(body);
        return String.format("%04d%02d%02d-%03d%d",
                             date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                             individual, check);
    }

    static int luhnCheckDigit(String body) {
        int sum = 0;
        for (int i = 0; i < body.length(); i++) {
            int digit = body.charAt(i) - '0';
            int product = digit * (2 - (i % 2));
            sum += (product / 10) + (product % 10);
        }
        return (10 - (sum % 10)) % 10;
    }
}
