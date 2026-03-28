/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.provider;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.GeneratorProfile;

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
