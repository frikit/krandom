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
 * Generates Finnish personal identity code (henkilötunnus) style identifiers in
 * {@code DDMMYYAXXX} format where A is a century marker.
 */
public final class FiFiNationalIdProvider implements NationalIdProvider {

    private static final char[] CENTURY_MARKS = {'-', 'A'};

    @Override
    public Locale getLocale() {
        return Locale.of("fi", "FI");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        char centuryMark = CENTURY_MARKS[random.nextInt(CENTURY_MARKS.length)];
        return String.format("%02d%02d%02d%c%03d",
                             date.getDayOfMonth(),
                             date.getMonthValue(),
                             date.getYear() % 100,
                             centuryMark,
                             random.nextInt(1000));
    }
}
