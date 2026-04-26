/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates TCP/UDP port numbers as strings.
 */
public final class PortGenerator implements Generator<String> {

    private final Random random;

    /**
     * Creates a port generator with default configuration.
     */
    public PortGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a port generator with the specified configuration.
     */
    public PortGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a port in the full range [1, 65535].
     */
    @Override
    public String generate() {
        return Integer.toString(1 + random.nextInt(65535));
    }

    /**
     * Generates a system port in [1, 1023].
     */
    public String generateSystemPort() {
        return Integer.toString(1 + random.nextInt(1023));
    }

    /**
     * Generates a user/registered port in [1024, 49151].
     */
    public String generateRegisteredPort() {
        return Integer.toString(1024 + random.nextInt(49151 - 1024 + 1));
    }

    /**
     * Generates a dynamic/private port in [49152, 65535].
     */
    public String generateDynamicPort() {
        return Integer.toString(49152 + random.nextInt(65535 - 49152 + 1));
    }
}
