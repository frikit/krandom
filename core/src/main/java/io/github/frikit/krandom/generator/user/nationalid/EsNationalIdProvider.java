/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Spanish National Identity Document (DNI) numbers.
 *
 * <p>A DNI consists of an 8-digit number followed by a check letter derived from the remainder
 * of the number divided by 23, using the lookup string {@code "TRWAGMYFPDXBNJZSQVHLCKE"}.
 *
 * <p>By default the format is {@code NNNNNNNNA} (no separator). Use {@link #withDash()} to
 * produce {@code NNNNNNNN-A}.
 *
 * <p>Example: {@code "12345678Z"} or {@code "12345678-Z"} with dash.
 */
public final class EsNationalIdProvider implements NationalIdProvider {

    private static final String LETTER_MAP = "TRWAGMYFPDXBNJZSQVHLCKE";

    private final boolean dash;

    /**
     * Creates a provider that generates DNI numbers without a separator.
     */
    public EsNationalIdProvider() {
        this(false);
    }

    private EsNationalIdProvider(boolean dash) {
        this.dash = dash;
    }

    /**
     * Returns a new provider that includes a dash between the numeric part and the check letter
     * (e.g., {@code "12345678-Z"}).
     *
     * @return a new provider configured to include the dash separator
     */
    public EsNationalIdProvider withDash() {
        return new EsNationalIdProvider(true);
    }

    @Override
    public Locale getLocale() {
        return Locale.of("es", "ES");
    }

    @Override
    public String generate(Random random) {
        int n = random.nextInt(100_000_000);
        char letter = LETTER_MAP.charAt(n % 23);
        if (dash) {
            return String.format("%08d-%c", n, letter);
        }
        return String.format("%08d%c", n, letter);
    }
}
