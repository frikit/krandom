/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Croatian OIB (osobni identifikacijski broj) style identifiers — 11 digits.
 *
 * <p>All digits are random; no ISO 7064 MOD 11,10 control digit is computed.
 */
public final class HrHrNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("hr", "HR");
    }

    @Override
    public String generate(Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}
