/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.system;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.file.SemverGenerator;

import java.util.Objects;

/**
 * Generates software version strings.
 */
public final class VersionGenerator implements Generator<String> {

    private final SemverGenerator semverGenerator;

    public VersionGenerator() {
        this(GeneratorConfig.defaults());
    }

    public VersionGenerator(GeneratorConfig config) {
        this.semverGenerator = new SemverGenerator(Objects.requireNonNull(config, "config must not be null"));
    }

    @Override
    public String generate() {
        return semverGenerator.generate();
    }

    public String generateStable() {
        return semverGenerator.generateStable();
    }

    public String generatePrerelease() {
        return semverGenerator.generatePrerelease();
    }
}
