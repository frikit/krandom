/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Random;

/**
 * Generates Norwegian national identity number style identifiers.
 *
 * <p>The output is a deterministic fake-format value in {@code DDMMYYNNNNN} shape. It is intended
 * for synthetic fixture data and does not claim to be accepted by official validators.
 */
public final class NbNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("nb", "NO");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        return String.format("%02d%02d%02d%05d",
                             date.getDayOfMonth(),
                             date.getMonthValue(),
                             date.getYear() % 100,
                             random.nextInt(100_000));
    }
}
