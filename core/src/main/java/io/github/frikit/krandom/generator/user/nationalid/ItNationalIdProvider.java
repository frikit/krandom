/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Italian Fiscal Code (Codice Fiscale) numbers.
 *
 * <p>The 16-character format is:
 * <ul>
 *   <li>3 uppercase consonants: surname portion
 *   <li>3 uppercase consonants: first-name portion
 *   <li>2 digits: birth year (YY)
 *   <li>1 letter: birth month (A=Jan, B=Feb, C=Mar, D=Apr, E=May, H=Jun,
 *                              L=Jul, M=Aug, P=Sep, R=Oct, S=Nov, T=Dec)
 *   <li>2 digits: birth day (01–31 male, 41–71 female)
 *   <li>4 chars: municipality code (1 letter + 3 digits)
 *   <li>1 char: check character (A–Z, computed from positions 1–15)
 * </ul>
 *
 * <p>The check character is computed by summing odd-position and even-position values (1-indexed)
 * using standard Codice Fiscale lookup tables, then taking the sum modulo 26 and converting to
 * the letter A–Z.
 *
 * <p>Example: {@code "RSSMRA85T10A562S"}
 */
public final class ItNationalIdProvider implements NationalIdProvider {

    private static final String CONSONANTS  = "BCDFGHJKLMNPQRSTVWXYZ";
    private static final String MONTH_CODES = "ABCDEHLMPRST";

    // Odd-position values for A–Z (0-indexed by alphabet position)
    private static final int[] ODD_LETTER = {
        1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23
    };

    /**
     * Creates a provider for Italian Codice Fiscale numbers.
     */
    public ItNationalIdProvider() {
    }

    private static String randomConsonants(Random random, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(CONSONANTS.charAt(random.nextInt(CONSONANTS.length())));
        }
        return sb.toString();
    }

    private static char computeCheckChar(String s) {
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            char c = s.charAt(i);
            int pos = i + 1; // 1-indexed
            if (pos % 2 == 1) {
                // Odd position
                sum += oddValue(c);
            } else {
                // Even position
                sum += evenValue(c);
            }
        }
        return (char) ('A' + (sum % 26));
    }

    private static int oddValue(char c) {
        if (c >= 'A') {
            return ODD_LETTER[c - 'A'];
        }
        // Digit: same mapping as the corresponding letter
        return ODD_LETTER[c - '0'];
    }

    private static int evenValue(char c) {
        if (c >= 'A') {
            return c - 'A';
        }
        return c - '0';
    }

    @Override
    public Locale getLocale() {
        return Locale.ITALY;
    }

    @Override
    public String generate(Random random) {
        String surname = randomConsonants(random, 3);
        String firstName = randomConsonants(random, 3);
        int year = random.nextInt(100);
        char monthCode = MONTH_CODES.charAt(random.nextInt(12));
        boolean male = random.nextBoolean();
        int day = male ? (random.nextInt(31) + 1) : (random.nextInt(31) + 41);
        char muniLetter = (char) ('A' + random.nextInt(26));
        int muniDigits = random.nextInt(1000);

        String first15 = String.format("%s%s%02d%c%02d%c%03d",
                                       surname, firstName, year, monthCode, day, muniLetter, muniDigits);

        char checkChar = computeCheckChar(first15);
        return first15 + checkChar;
    }
}
