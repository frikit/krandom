/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates UPC-A values (12 digits) with valid check digits.
 */
public final class UpcGenerator implements Generator<String> {

    private final Random random;

    public UpcGenerator() {
        this(GeneratorConfig.defaults());
    }

    public UpcGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        StringBuilder body = new StringBuilder(11);
        for (int i = 0; i < 11; i++) {
            body.append(random.nextInt(10));
        }
        int checkDigit = computeCheckDigit(body.toString());
        return body.append(checkDigit).toString();
    }

    static int computeCheckDigit(String body) {
        int sumOdd = 0;
        int sumEven = 0;
        for (int i = 0; i < body.length(); i++) {
            int digit = body.charAt(i) - '0';
            if (i % 2 == 0) {
                sumOdd += digit;
            } else {
                sumEven += digit;
            }
        }
        int total = sumOdd * 3 + sumEven;
        return (10 - (total % 10)) % 10;
    }
}
