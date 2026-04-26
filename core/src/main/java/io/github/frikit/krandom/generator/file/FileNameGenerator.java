/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates simple file names that can be combined with extensions.
 */
public final class FileNameGenerator implements Generator<String> {

    private static final String[] BASE_NAMES = {
        "report", "monthly summary", "customer export", "invoice", "dataset",
        "image asset", "profile backup", "audit log", "release notes", "project plan"
    };

    private final Random random;

    public FileNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    public FileNameGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return BASE_NAMES[random.nextInt(BASE_NAMES.length)] + "-" + (100 + random.nextInt(900));
    }

    /**
     * Generates a file name with extension (extension without or with dot).
     */
    public String generateWithExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("extension must not be blank");
        }
        String normalized = extension.startsWith(".") ? extension.substring(1) : extension;
        return generate() + "." + normalized;
    }
}
