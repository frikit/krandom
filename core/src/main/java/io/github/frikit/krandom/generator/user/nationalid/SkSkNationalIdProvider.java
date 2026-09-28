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
 * Generates Slovak birth number (rodné číslo) style identifiers in {@code YYMMDD/XXXX} format.
 *
 * <p>{@code YYMMDD} is a birth date between 1954 and 1999: ten-digit birth numbers were introduced
 * for births from 1954, so a ten-digit number with a year below 54 denotes a birth from 2000. The
 * four-digit suffix is random, so values are not guaranteed to satisfy the modulo-11 rule.
 */
public final class SkSkNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("sk", "SK");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        if (date.getYear() < 1954) {
            date = date.plusYears(4);
        }
        return String.format(Locale.ROOT, "%02d%02d%02d/%04d",
                             date.getYear() % 100,
                             date.getMonthValue(),
                             date.getDayOfMonth(),
                             random.nextInt(10_000));
    }
}
