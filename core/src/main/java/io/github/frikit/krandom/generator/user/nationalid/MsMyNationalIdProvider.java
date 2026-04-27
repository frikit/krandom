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
 * Generates Malaysian MyKad style identifiers in {@code YYMMDD-SS-XXXX} format (12 digits).
 */
public final class MsMyNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("ms", "MY");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        int state = random.nextInt(14) + 1;
        return String.format("%02d%02d%02d-%02d-%04d",
                             date.getYear() % 100,
                             date.getMonthValue(),
                             date.getDayOfMonth(),
                             state,
                             random.nextInt(10_000));
    }
}
