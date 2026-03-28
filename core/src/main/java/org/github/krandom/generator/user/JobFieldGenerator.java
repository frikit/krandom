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
 * Generates job field categories.
 */
public final class JobFieldGenerator implements Generator<String> {

    private static final String[] FIELDS = {
        "Engineering", "Marketing", "Sales", "Finance", "Operations", "Product",
        "Human Resources", "Legal", "Design", "Customer Support", "Research",
        "Data Science", "Security", "Procurement", "Logistics"
    };

    private final Random random;

    public JobFieldGenerator() {
        this(GeneratorConfig.defaults());
    }

    public JobFieldGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
    }

    @Override
    public String generate() {
        return FIELDS[random.nextInt(FIELDS.length)];
    }
}
