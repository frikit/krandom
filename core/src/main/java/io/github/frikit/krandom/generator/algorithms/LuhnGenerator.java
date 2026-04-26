/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.algorithms;

import io.github.frikit.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/**
 * A {@link Generator} that produces 10-digit strings whose last digit is a valid
 * <a href="https://en.wikipedia.org/wiki/Luhn_algorithm">Luhn check digit</a>.
 *
 * <p>The generated value:
 * <ul>
 *   <li>Is always exactly 10 characters long.</li>
 *   <li>Consists entirely of ASCII decimal digits ({@code 0}–{@code 9}).</li>
 *   <li>Passes a standard Luhn-algorithm check.</li>
 * </ul>
 */
public final class LuhnGenerator implements Generator<String> {

    private static final int PAYLOAD_LENGTH = 9;

    private final RandomGenerator random;

    /**
     * Creates a generator backed by {@link SecureRandom}.
     */
    public LuhnGenerator() {
        this.random = new SecureRandom();
    }

    /**
     * Generates a 10-digit Luhn-valid number string.
     *
     * @return a 10-character string of digits that passes the Luhn check
     */
    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder(PAYLOAD_LENGTH + 1);
        for (int i = 0; i < PAYLOAD_LENGTH; i++) {
            int digit = 1 + random.nextInt(9); // [1, 9]
            builder.append(digit);
        }
        builder.append(getCheckDigit(builder.toString()));
        return builder.toString();
    }

    private int getCheckDigit(String number) {
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = Integer.parseInt(number.substring(i, i + 1));
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }
            sum += digit;
        }
        int mod = sum % 10;
        return mod == 0 ? 0 : 10 - mod;
    }
}
