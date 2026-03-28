/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates URI strings (URL-form URIs).
 */
public final class UriGenerator implements Generator<String> {

    private final Random       random;
    private final URLGenerator urlGenerator;

    public UriGenerator() {
        this(GeneratorConfig.defaults());
    }

    public UriGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public UriGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                      ? new Random(effective.getSeed().getAsLong())
                      : new SecureRandom();
        this.urlGenerator = new URLGenerator(effective);
    }

    @Override
    public String generate() {
        if (random.nextBoolean()) {
            return urlGenerator.generateWithPathAndQuery();
        }
        return urlGenerator.generateWithPath();
    }

    /**
     * Generates a URI with path but without query parameters.
     *
     * @return URI string
     */
    public String generateWithPath() {
        return urlGenerator.generateWithPath();
    }

    /**
     * Generates a URI with path and query parameters.
     *
     * @return URI string
     */
    public String generateWithQuery() {
        return urlGenerator.generateWithPathAndQuery();
    }
}
