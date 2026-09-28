/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Vietnamese citizen identity card (CCCD) style identifiers — 12 digits.
 *
 * <p>All digits are random, so they do not necessarily encode a real province code, sex and century,
 * or birth year.
 */
public final class ViVnNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("vi", "VN");
    }

    @Override
    public String generate(Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}
