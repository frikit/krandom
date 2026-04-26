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
 * Generates job seniority labels.
 */
public final class SeniorityGenerator implements Generator<String> {

    private static final String[] SENIORITIES = {
        "Intern", "Junior", "Associate", "Mid-level", "Senior", "Lead",
        "Principal", "Staff", "Director", "Vice President"
    };

    private final Random random;

    public SeniorityGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SeniorityGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return SENIORITIES[random.nextInt(SENIORITIES.length)];
    }
}
