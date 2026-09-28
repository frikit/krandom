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
 * Generates Romanian CNP (Cod Numeric Personal) style identifiers — 13 digits.
 *
 * <p>The first digit is 1 (male) or 2 (female), denoting a birth in the 1900s, followed by a
 * {@code YYMMDD} birth date between 1950 and 1999 and a county code from 01 to 46. The last four
 * digits are random: they are not guaranteed to form a valid serial number, and no CNP control digit
 * is computed.
 */
public final class RoRoNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("ro", "RO");
    }

    @Override
    public String generate(Random random) {
        int sex = random.nextInt(2) + 1;
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        int county = random.nextInt(46) + 1;
        int seq = random.nextInt(1000);
        int check = random.nextInt(10);
        return String.format(Locale.ROOT, "%d%02d%02d%02d%02d%03d%d",
                             sex,
                             date.getYear() % 100,
                             date.getMonthValue(),
                             date.getDayOfMonth(),
                             county,
                             seq,
                             check);
    }
}
