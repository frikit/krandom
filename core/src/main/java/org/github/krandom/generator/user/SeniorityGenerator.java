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
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return SENIORITIES[random.nextInt(SENIORITIES.length)];
    }
}
