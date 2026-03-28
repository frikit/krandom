/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Generates UK National Insurance (NI) numbers.
 *
 * <p>The format is {@code AB 12 34 56 C} where:
 * <ul>
 *   <li>First two characters are letters with specific validity constraints
 *   <li>Six digits split into three pairs
 *   <li>Final character is a letter A–D
 * </ul>
 *
 * <p>Letters D, F, I, Q, U, V are excluded from the first position; D, F, I, O, Q, U, V from the
 * second. Certain two-letter prefix pairs are also disallowed: BG, GB, NK, KN, NT, TN, ZZ.
 *
 * <p>Example: {@code "AB 12 34 56 C"}
 */
public final class GbNationalIdProvider implements NationalIdProvider {

    private static final String      VALID_FIRST      = "ABCEGHJKLMNOPRSTWXYZ";
    private static final String      VALID_SECOND     = "ABCEGHJKLMNPRSTWXYZ";
    private static final Set<String> DISALLOWED_PAIRS =
        Set.of("BG", "GB", "NK", "KN", "NT", "TN", "ZZ");
    private static final char[]      SUFFIX_LETTERS   = { 'A', 'B', 'C', 'D' };

    /**
     * Creates a provider for UK NI numbers.
     */
    public GbNationalIdProvider() {
    }

    @Override
    public Locale getLocale() {
        return Locale.UK;
    }

    @Override
    public String generate(Random random) {
        char first, second;
        do {
            first = VALID_FIRST.charAt(random.nextInt(VALID_FIRST.length()));
            second = VALID_SECOND.charAt(random.nextInt(VALID_SECOND.length()));
        } while (DISALLOWED_PAIRS.contains("" + first + second));

        int n1 = random.nextInt(100);
        int n2 = random.nextInt(100);
        int n3 = random.nextInt(100);
        char suffix = SUFFIX_LETTERS[random.nextInt(SUFFIX_LETTERS.length)];

        return String.format("%c%c %02d %02d %02d %c", first, second, n1, n2, n3, suffix);
    }
}
