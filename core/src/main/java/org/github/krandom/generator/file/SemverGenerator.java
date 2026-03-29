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
 * Generates semantic version strings.
 */
public final class SemverGenerator implements Generator<String> {

    private static final String[] PRE_RELEASES = { "alpha", "beta", "rc" };

    private final Random random;

    public SemverGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SemverGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        int major = random.nextInt(0, 11);
        int minor = random.nextInt(0, 21);
        int patch = random.nextInt(0, 51);
        return major + "." + minor + "." + patch;
    }

    /**
     * Generates a stable semantic version string (same as {@link #generate()}).
     */
    public String generateStable() {
        return generate();
    }

    /**
     * Generates a pre-release semantic version (e.g. {@code 1.2.3-rc.2}).
     */
    public String generatePrerelease() {
        String base = generate();
        String tag = PRE_RELEASES[random.nextInt(PRE_RELEASES.length)];
        int iteration = random.nextInt(1, 10);
        return base + "-" + tag + "." + iteration;
    }
}
