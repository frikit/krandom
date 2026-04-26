/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates file extensions (without leading dot), e.g. {@code "png"} or {@code "pdf"}.
 */
public final class FileExtensionGenerator implements Generator<String> {

    private static final List<String> DEFAULT_EXTENSIONS = List.of(
        "gif", "jpg", "jpeg", "png", "svg", "pdf", "json", "xml", "txt", "csv", "zip"
    );

    private final Random       random;
    private final List<String> extensions;

    public FileExtensionGenerator() {
        this(GeneratorConfig.defaults());
    }

    public FileExtensionGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.extensions = DEFAULT_EXTENSIONS;
    }

    public static String[] defaultExtensions() {
        return DEFAULT_EXTENSIONS.toArray(String[]::new);
    }

    @Override
    public String generate() {
        return extensions.get(random.nextInt(extensions.size()));
    }

    /**
     * Picks a random extension from provided candidates.
     */
    public String generateFrom(String... candidates) {
        Objects.requireNonNull(candidates, "candidates must not be null");
        if (candidates.length == 0) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        String[] normalized = new String[candidates.length];
        for (int i = 0; i < candidates.length; i++) {
            String ext = candidates[i];
            if (ext == null || ext.isBlank()) {
                throw new IllegalArgumentException("candidate at index " + i + " must not be blank");
            }
            normalized[i] = ext.startsWith(".") ? ext.substring(1) : ext;
        }
        return normalized[random.nextInt(normalized.length)];
    }
}
