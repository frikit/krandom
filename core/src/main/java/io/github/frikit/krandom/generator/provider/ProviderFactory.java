/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;

/**
 * Creates provider instances using generator configuration.
 */
@FunctionalInterface
public interface ProviderFactory {

    /**
     * Creates a provider instance.
     *
     * @param config generator config with locale/seed
     * @return provider instance
     */
    Object create(GeneratorConfig config);
}
