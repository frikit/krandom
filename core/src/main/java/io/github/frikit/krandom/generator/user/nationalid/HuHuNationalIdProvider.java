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
 * Generates Hungarian personal identification number style identifiers in
 * {@code X-YYMMDD-XXXX} format.
 */
public final class HuHuNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("hu", "HU");
    }

    @Override
    public String generate(Random random) {
        int gender = random.nextInt(2) + 1;
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        return String.format("%d-%02d%02d%02d-%04d",
                             gender,
                             date.getYear() % 100,
                             date.getMonthValue(),
                             date.getDayOfMonth(),
                             random.nextInt(10_000));
    }
}
