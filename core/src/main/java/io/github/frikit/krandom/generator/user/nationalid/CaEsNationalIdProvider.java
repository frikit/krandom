/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Spanish DNI style identifiers for the Catalan locale — 8 digits + letter.
 */
public final class CaEsNationalIdProvider implements NationalIdProvider {

    private static final String CHECK_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @Override
    public Locale getLocale() {
        return Locale.of("ca", "ES");
    }

    @Override
    public String generate(Random random) {
        int number = random.nextInt(100_000_000);
        char letter = CHECK_LETTERS.charAt(number % 23);
        return String.format("%08d%c", number, letter);
    }
}
