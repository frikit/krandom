/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates educational attainment values.
 */
public final class EducationalAttainmentGenerator implements Generator<String> {

    private static final String[] LEVELS = {
        "High School", "Associate Degree", "Bachelor's Degree", "Master's Degree",
        "Doctorate", "Professional Degree", "Vocational Certificate"
    };

    private final Random random;

    public EducationalAttainmentGenerator() {
        this(GeneratorConfig.defaults());
    }

    public EducationalAttainmentGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return LEVELS[random.nextInt(LEVELS.length)];
    }
}
