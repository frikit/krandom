/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Chinese Resident Identity Card numbers.
 *
 * <p>The 18-character format is:
 * <ul>
 *   <li>6-digit region code (simplified plausible range: 100000–658999)
 *   <li>8-digit birth date (yyyyMMdd; year 1940–2005, month 01–12, day 01–28)
 *   <li>3-digit sequence number (001–999)
 *   <li>1 check character computed via ISO 7064 Mod 11,2 (may be '0'–'9' or 'X')
 * </ul>
 *
 * <p>Example: {@code "110101198001011237"}
 */
public final class CnNationalIdProvider implements NationalIdProvider {

    private static final int[]  WEIGHTS     = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
    private static final String CHECK_CHARS = "10X98765432";

    /**
     * Creates a provider for Chinese Resident Identity Card numbers.
     */
    public CnNationalIdProvider() {
    }

    @Override
    public Locale getLocale() {
        return Locale.CHINA;
    }

    @Override
    public String generate(Random random) {
        int region = 100000 + random.nextInt(559000);
        int year = 1940 + random.nextInt(66);    // 1940–2005
        int month = random.nextInt(12) + 1;        // 01–12
        int day = random.nextInt(28) + 1;        // 01–28
        int sequence = random.nextInt(999) + 1;       // 001–999

        String first17 = String.format("%06d%04d%02d%02d%03d",
                                       region, year, month, day, sequence);

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (first17.charAt(i) - '0') * WEIGHTS[i];
        }
        char checkChar = CHECK_CHARS.charAt((12 - (sum % 11)) % 11);

        return first17 + checkChar;
    }
}
