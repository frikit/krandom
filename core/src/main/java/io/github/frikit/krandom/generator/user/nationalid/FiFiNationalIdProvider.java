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
 * {@code DDMMYY-XXX} format.
 *
 * <p>{@code DDMMYY} is a birth date between 1950 and 1999, so the century sign is always
 * {@code -}, which denotes the 1900s. {@code XXX} is random and the control character that
 * completes a real henkilötunnus is not appended, so values do not pass official validation.
 */
public final class FiFiNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("fi", "FI");
    }

    @Override
    public String generate(Random random) {
        LocalDate date = LocalDate.of(1950, 1, 1).plusDays(random.nextInt(18_262));
        // Former century-sign draw: still consumed so every other draw keeps its value.
        random.nextInt(2);
        return String.format(Locale.ROOT, "%02d%02d%02d-%03d",
                             date.getDayOfMonth(),
                             date.getMonthValue(),
                             date.getYear() % 100,
                             random.nextInt(1000));
    }
}
