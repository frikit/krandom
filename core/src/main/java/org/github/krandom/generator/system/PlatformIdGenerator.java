/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.system;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates platform and platform-id style values.
 */
public final class PlatformIdGenerator implements Generator<String> {

    private static final String[] PLATFORMS = { "windows", "linux", "macos", "android", "ios" };
    private static final String[] ARCH      = { "x86", "x64", "arm64" };

    private final Random random;

    public PlatformIdGenerator() {
        this(GeneratorConfig.defaults());
    }

    public PlatformIdGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return generatePlatformId();
    }

    public String generatePlatform() {
        return PLATFORMS[random.nextInt(PLATFORMS.length)];
    }

    public String generateArchitecture() {
        return ARCH[random.nextInt(ARCH.length)];
    }

    public String generatePlatformId() {
        return generatePlatform() + "-" + generateArchitecture();
    }
}
