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
 * Generates URL-friendly slug values.
 */
public final class SlugGenerator implements Generator<String> {

    private static final String[] WORDS = {
        "quick", "brown", "fox", "lazy", "dog", "modern", "cloud", "data", "api", "user",
        "profile", "settings", "secure", "simple", "smart", "alpha", "beta", "release",
        "guide", "tutorial", "platform", "service", "digital", "network", "system"
    };

    private final Random random;

    /**
     * Creates a slug generator with default configuration.
     */
    public SlugGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a slug generator with the specified configuration.
     */
    public SlugGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a slug with 2 to 4 words separated by '-'.
     */
    @Override
    public String generate() {
        int parts = 2 + random.nextInt(3);
        StringBuilder slug = new StringBuilder();
        for (int i = 0; i < parts; i++) {
            if (i > 0) {
                slug.append('-');
            }
            slug.append(WORDS[random.nextInt(WORDS.length)]);
        }
        return slug.toString();
    }

    /**
     * Converts arbitrary text into a normalized lowercase slug.
     */
    public String slugify(String input) {
        Objects.requireNonNull(input, "input must not be null");
        String normalized = input.trim().toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+|-+$", "");
        return normalized.isEmpty() ? "n-a" : normalized;
    }
}
