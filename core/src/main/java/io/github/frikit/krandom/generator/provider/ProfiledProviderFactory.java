/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GeneratorProfile;

/**
 * Creates provider instances using generator configuration and profile metadata.
 */
@FunctionalInterface
public interface ProfiledProviderFactory {

    /**
     * Creates a provider instance.
     *
     * @param profile selected profile metadata
     * @param config  generator config with locale/seed
     * @return provider instance
     */
    Object create(GeneratorProfile profile, GeneratorConfig config);
}
