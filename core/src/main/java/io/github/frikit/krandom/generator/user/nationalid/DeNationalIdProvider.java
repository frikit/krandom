/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates German Tax Identification Numbers (Steuer-Identifikationsnummer).
 *
 * <p>The Steuer-ID consists of 11 digits and follows the published structure rules:
 * <ul>
 *   <li>the first digit is 1–9 (never 0);
 *   <li>among digits 1–10 exactly one digit occurs twice or three times, every other digit at most
 *       once, and three occurrences of the same digit are never all directly consecutive;
 *   <li>digit 11 is a check digit computed using ISO 7064 Mod 11,10.
 * </ul>
 *
 * <p>The check digit algorithm iterates through digits 1–10:
 * <pre>
 *   product = 10  (initial value)
 *   for each digit d:
 *     sum = (d + product) % 10
 *     if (sum == 0) sum = 10
 *     product = (2 * sum) % 11
 *   check_digit = 11 - product  (a result of 10 becomes 0)
 * </pre>
 *
 * <p>Digits 1–10 are drawn uniformly from all sequences that satisfy the structure rules.
 *
 * <p>Example: {@code "86095742719"}
 */
public final class DeNationalIdProvider implements NationalIdProvider {

    /**
     * Creates a provider for German Steuer-ID numbers.
     */
    public DeNationalIdProvider() {
    }

    /**
     * Computes the ISO 7064 Mod 11,10 check digit for the first 10 elements of the given array.
     *
     * @param digits array of at least 10 integers, each in [0, 9]
     * @return check digit in [0, 9]
     */
    static int computeCheckDigit(int[] digits) {
        int product = 10;
        for (int i = 0; i < 10; i++) {
            int sum = (digits[i] + product) % 10;
            if (sum == 0) sum = 10;
            product = (2 * sum) % 11;
        }
        return (11 - product) % 10;
    }

    /**
     * Fills {@code digits[0..9]} with a random arrangement in which exactly one digit occurs twice or
     * three times and every other digit at most once.
     *
     * <p>The repeated digit and the single digits are taken from a shuffled pool of 0–9. The shapes are
     * chosen in proportion to their sequence counts (twice: 10 × 9 × 10!/2!; three times:
     * 45 × 8 × 10!/3!; ratio 3 : 4) so that, after rejecting a leading zero or three consecutive
     * repeats, every valid Steuer-ID prefix is equally likely.
     *
     * @param random the PRNG supplying the shuffles and the shape choice
     * @param digits array of at least 10 elements that receives the digits
     */
    private static void fillDigits(Random random, int[] digits) {
        int[] pool = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        shuffle(random, pool);
        int occurrences = random.nextInt(7) < 3 ? 2 : 3;
        for (int i = 0; i < 10; i++) {
            digits[i] = i < occurrences ? pool[0] : pool[i - occurrences + 1];
        }
        shuffle(random, digits);
    }

    /**
     * Returns whether any three consecutive elements of {@code digits[0..9]} are equal.
     *
     * @param digits array of at least 10 digits
     * @return {@code true} when three directly consecutive digits are equal
     */
    private static boolean hasThreeConsecutiveEqualDigits(int[] digits) {
        for (int i = 2; i < 10; i++) {
            if (digits[i] == digits[i - 1] && digits[i] == digits[i - 2]) {
                return true;
            }
        }
        return false;
    }

    private static void shuffle(Random random, int[] values) {
        for (int i = 9; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = values[i];
            values[i] = values[j];
            values[j] = swap;
        }
    }

    @Override
    public Locale getLocale() {
        return Locale.GERMANY;
    }

    @Override
    public String generate(Random random) {
        int[] digits = new int[11];
        do {
            fillDigits(random, digits);
        } while (digits[0] == 0 || hasThreeConsecutiveEqualDigits(digits));
        digits[10] = computeCheckDigit(digits);

        StringBuilder sb = new StringBuilder(11);
        for (int d : digits) {
            sb.append(d);
        }
        return sb.toString();
    }
}
