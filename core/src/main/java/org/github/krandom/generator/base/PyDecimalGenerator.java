/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates decimal values similar to Faker's {@code pydecimal()}.
 */
public final class PyDecimalGenerator implements Generator<BigDecimal> {

    private final Random random;

    public PyDecimalGenerator() {
        this(GeneratorConfig.defaults());
    }

    public PyDecimalGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public BigDecimal generate() {
        return generate(6, 2);
    }

    public BigDecimal generate(int leftDigits, int rightDigits) {
        if (leftDigits < 1) {
            throw new IllegalArgumentException("leftDigits must be >= 1, got: " + leftDigits);
        }
        if (rightDigits < 0) {
            throw new IllegalArgumentException("rightDigits must be >= 0, got: " + rightDigits);
        }
        long integerPart = 1 + random.nextLong((long) Math.pow(10, Math.min(leftDigits, 18)) - 1);
        BigDecimal value = BigDecimal.valueOf(integerPart);
        if (rightDigits > 0) {
            BigDecimal fraction = BigDecimal.valueOf(random.nextLong((long) Math.pow(10, Math.min(rightDigits, 9))))
                    .movePointLeft(rightDigits);
            value = value.add(fraction);
        }
        return value.setScale(rightDigits, RoundingMode.DOWN);
    }
}
