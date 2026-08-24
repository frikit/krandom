/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.extension;

import io.github.frikit.krandom.generator.provider.ProviderDescriptor;

/**
 * Narrow contribution surface exposed while a {@link KRandomModule} is configured.
 */
public interface KRandomModuleContext {

    /**
     * Registers one metadata-complete provider descriptor.
     *
     * <p>The descriptor's provider names, schema names, safety metadata, and semantic keys are
     * installed as one atomic contribution. Conflicts with built-ins or another installed module
     * fail configuration construction.
     *
     * @param descriptor provider contribution
     */
    void registerProvider(ProviderDescriptor<?> descriptor);

    /**
     * Adds project vocabulary for a semantic key.
     *
     * @param semanticKey canonical semantic key
     * @param fieldNames field names that should resolve to the key
     */
    void registerSemanticAliases(String semanticKey, String... fieldNames);
}
