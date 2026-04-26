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
 * Generates US ABA routing transit numbers with valid checksum.
 */
public final class AbaRoutingGenerator implements Generator<String> {

    private final Random random;

    public AbaRoutingGenerator() {
        this(GeneratorConfig.defaults());
    }

    public AbaRoutingGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
    }

    @Override
    public String generate() {
        int[] digits = new int[9];
        for (int i = 0; i < 8; i++) {
            digits[i] = random.nextInt(10);
        }
        int checksum = (10 - (
                                 3 * (digits[0] + digits[3] + digits[6]) +
                                 7 * (digits[1] + digits[4] + digits[7]) +
                                 (digits[2] + digits[5])) % 10) % 10;
        digits[8] = checksum;
        StringBuilder out = new StringBuilder(9);
        for (int digit : digits) {
            out.append(digit);
        }
        return out.toString();
    }
}
