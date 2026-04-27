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
 * Generates Romanian CNP (Cod Numeric Personal) style identifiers ��� 13 digits.
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
        return String.format("%d%02d%02d%02d%02d%03d%d",
                             sex,
                             date.getYear() % 100,
                             date.getMonthValue(),
                             date.getDayOfMonth(),
                             county,
                             seq,
                             check);
    }
}
