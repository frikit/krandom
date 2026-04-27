/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Thai national ID style identifiers — 13 digits in {@code X-XXXX-XXXXX-XX-X} format.
 */
public final class ThThNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("th", "TH");
    }

    @Override
    public String generate(Random random) {
        return String.format("%d-%04d-%05d-%02d-%d",
                             random.nextInt(9) + 1,
                             random.nextInt(10_000),
                             random.nextInt(100_000),
                             random.nextInt(100),
                             random.nextInt(10));
    }
}
