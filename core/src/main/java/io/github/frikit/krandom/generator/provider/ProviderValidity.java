/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

/**
 * Strength of a provider claim about one validity dimension.
 *
 * <p>A claim describes the provider's documented contract, not whether an emitted value is safe
 * to use outside a test. Consult {@link ProviderSafetyMetadata#testSafety()} separately.
 */
public enum ProviderValidity {

    /** The provider does not make a classification for this dimension. */
    UNCLASSIFIED,

    /** This dimension does not apply to the provider's output. */
    NOT_APPLICABLE,

    /** The provider guarantees this dimension for its documented output. */
    GUARANTEED,

    /** The selected {@code GeneratorConfig} determines this dimension. */
    CONFIGURATION_DEPENDENT
}
