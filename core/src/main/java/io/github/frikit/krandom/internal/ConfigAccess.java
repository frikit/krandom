/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.Objects;

/**
 * What one krandom package may ask of {@link GeneratorConfig} that its public API does not offer.
 *
 * <p>This package is not exported from the {@code io.github.frikit.krandom} module, so it is not public API:
 * VERSIONING.md lets it change in any release. {@link GeneratorConfig} installs the implementation when its
 * class is initialized, which has happened by the time anything holds a configuration to pass here.
 */
public final class ConfigAccess {

    /** The copy that {@link #withSeed(GeneratorConfig, long)} makes. */
    @FunctionalInterface
    public interface Seeding {

        /**
         * The configuration that {@code config.toBuilder().seed(seed).build()} makes.
         *
         * @param config configuration to copy
         * @param seed   numeric seed of the copy
         * @return the copy
         */
        GeneratorConfig withSeed(GeneratorConfig config, long seed);
    }

    private static volatile @org.jspecify.annotations.Nullable Seeding seeding;

    private ConfigAccess() {}

    /**
     * Installs the copy; {@link GeneratorConfig} calls this once, from its static initializer.
     *
     * @param implementation the copy
     * @throws IllegalStateException if a copy is installed already
     */
    public static synchronized void install(Seeding implementation) {
        Objects.requireNonNull(implementation, "implementation must not be null");
        if (seeding != null) {
            throw new IllegalStateException("ConfigAccess is installed already");
        }
        seeding = implementation;
    }

    /**
     * The configuration that {@code config.toBuilder().seed(seed).build()} makes: for a configuration without
     * modules, a field-by-field copy, the one {@link GeneratorConfig#forChildStream(String)} makes for a child
     * stream, without the builder's two copies of every collection.
     *
     * @param config configuration to copy
     * @param seed   numeric seed of the copy
     * @return the copy
     */
    public static GeneratorConfig withSeed(GeneratorConfig config, long seed) {
        return Objects.requireNonNull(seeding, "GeneratorConfig is not initialized")
                .withSeed(config, seed);
    }
}
