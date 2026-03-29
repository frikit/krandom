/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates marital status values.
 */
public final class MaritalStatusGenerator implements Generator<String> {

    private static final String[] STATUSES = {
        "Single", "Married", "Divorced", "Widowed", "Separated", "Domestic Partnership"
    };

    private final Random random;

    public MaritalStatusGenerator() {
        this(GeneratorConfig.defaults());
    }

    public MaritalStatusGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return STATUSES[random.nextInt(STATUSES.length)];
    }
}
