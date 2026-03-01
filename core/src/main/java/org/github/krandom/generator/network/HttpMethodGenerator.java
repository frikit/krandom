/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates HTTP request methods.
 */
public final class HttpMethodGenerator implements Generator<String> {

    private static final String[] METHODS = {
            "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"
    };

    private final Random random;

    public HttpMethodGenerator() {
        this(GeneratorConfig.defaults());
    }

    public HttpMethodGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return METHODS[random.nextInt(METHODS.length)];
    }
}
