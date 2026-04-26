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
 * Generates company industry values.
 */
public final class IndustryGenerator implements Generator<String> {

    private static final String[] INDUSTRIES = {
        "Technology", "Healthcare", "Finance", "Education", "Retail", "Manufacturing",
        "Telecommunications", "Energy", "Transportation", "Construction", "Real Estate",
        "Hospitality", "Media", "Entertainment", "Agriculture", "Aerospace", "Automotive",
        "Biotechnology", "Consulting", "Cybersecurity"
    };

    private final Random random;

    /**
     * Creates an industry generator with default configuration.
     */
    public IndustryGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an industry generator with the specified configuration.
     */
    public IndustryGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return INDUSTRIES[random.nextInt(INDUSTRIES.length)];
    }
}
