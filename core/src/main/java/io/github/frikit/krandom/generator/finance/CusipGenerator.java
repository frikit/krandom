/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates CUSIP values with valid check digits.
 */
public final class CusipGenerator implements Generator<String> {

    private static final String ALNUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Random random;

    public CusipGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CusipGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    static int computeCheckDigit(String valueWithoutCheck) {
        int sum = 0;
        for (int i = 0; i < valueWithoutCheck.length(); i++) {
            int val = ALNUM.indexOf(valueWithoutCheck.charAt(i));
            int adjusted = (i % 2 == 1) ? val * 2 : val;
            sum += adjusted / 10 + adjusted % 10;
        }
        return (10 - (sum % 10)) % 10;
    }

    @Override
    public String generate() {
        StringBuilder base = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            base.append(ALNUM.charAt(random.nextInt(ALNUM.length())));
        }
        int checkDigit = computeCheckDigit(base.toString());
        return base.append(checkDigit).toString();
    }
}
