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
 * Generates job position titles.
 */
public final class PositionGenerator implements Generator<String> {

    private static final String[] POSITIONS = {
        "Software Engineer", "Product Manager", "Data Analyst", "UX Designer",
        "Account Executive", "Marketing Specialist", "Business Analyst",
        "DevOps Engineer", "Site Reliability Engineer", "Engineering Manager",
        "Solutions Architect", "Technical Writer", "QA Engineer", "Project Manager",
        "Security Engineer", "Database Administrator"
    };

    private final Random random;

    public PositionGenerator() {
        this(GeneratorConfig.defaults());
    }

    public PositionGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
    }

    @Override
    public String generate() {
        return POSITIONS[random.nextInt(POSITIONS.length)];
    }
}
