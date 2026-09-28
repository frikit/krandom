/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Indonesian NIK (Nomor Induk Kependudukan) style identifiers — 16 digits.
 *
 * <p>All digits are random, so they do not necessarily encode a real region code or birth date.
 */
public final class IdIdNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("id", "ID");
    }

    @Override
    public String generate(Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}
