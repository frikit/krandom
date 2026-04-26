/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates French National Identification Register (NIR) numbers.
 *
 * <p>The NIR format is {@code G YY MM DD PPP OOO KK} (without spaces in output) where:
 * <ul>
 *   <li>G: gender (1 or 2)
 *   <li>YY: birth year (00–99)
 *   <li>MM: birth month (01–12)
 *   <li>DD: birth department (01–99)
 *   <li>PPP: commune (001–999)
 *   <li>OOO: birth order (001–999)
 *   <li>KK: control key = 97 − (13-digit number mod 97), range [01, 97]
 * </ul>
 *
 * <p>Example: {@code "1 83 05 42 123 456 43"} formatted without spaces.
 */
public final class FrNationalIdProvider implements NationalIdProvider {

    /**
     * Creates a provider for French NIR numbers.
     */
    public FrNationalIdProvider() {
    }

    @Override
    public Locale getLocale() {
        return Locale.FRANCE;
    }

    @Override
    public String generate(Random random) {
        int gender = random.nextInt(2) + 1;          // 1 or 2
        int year = random.nextInt(100);             // 00–99
        int month = random.nextInt(12) + 1;          // 01–12
        int dept = random.nextInt(99) + 1;          // 01–99
        int commune = random.nextInt(999) + 1;         // 001–999
        int order = random.nextInt(999) + 1;         // 001–999

        long number13 = Long.parseLong(String.format("%d%02d%02d%02d%03d%03d",
                                                     gender, year, month, dept, commune, order));
        int key = (int) (97 - (number13 % 97));

        return String.format("%d%02d%02d%02d%03d%03d%02d",
                             gender, year, month, dept, commune, order, key);
    }
}
