/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.file;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates common MIME content types.
 */
public final class MimeTypeGenerator implements Generator<String> {

    private static final String[] TYPES = {
        "application/json",
        "application/xml",
        "application/pdf",
        "application/zip",
        "application/octet-stream",
        "text/plain",
        "text/html",
        "text/css",
        "text/csv",
        "image/png",
        "image/jpeg",
        "image/gif",
        "audio/mpeg",
        "video/mp4"
    };

    private final Random random;

    public MimeTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public MimeTypeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return TYPES[random.nextInt(TYPES.length)];
    }
}
