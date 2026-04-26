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
 * Generates US EIN (Employer Identification Number) values.
 */
public final class EinGenerator implements Generator<String> {

    private final Random random;

    public EinGenerator() {
        this(GeneratorConfig.defaults());
    }

    public EinGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates formatted EIN value ({@code NN-NNNNNNN}).
     */
    @Override
    public String generate() {
        int prefix = 1 + random.nextInt(99);
        int suffix = random.nextInt(10_000_000);
        return String.format("%02d-%07d", prefix, suffix);
    }

    /**
     * Generates EIN digits without separator.
     */
    public String generateUnformatted() {
        return generate().replace("-", "");
    }
}
