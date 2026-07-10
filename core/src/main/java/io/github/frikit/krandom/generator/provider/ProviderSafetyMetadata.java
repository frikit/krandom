/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import java.util.Objects;

/**
 * Capability claims for a provider's format, checksum, semantics, and test safety.
 *
 * <p>Metadata is intentionally conservative: {@link ProviderValidity#UNCLASSIFIED} means that
 * krandom makes no claim for the dimension. A checksum-valid or format-valid value is not thereby
 * a safe external credential.
 *
 * @param formatValidity whether the documented output format is guaranteed
 * @param checksumValidity whether checksum behavior is guaranteed
 * @param semanticPlausibility whether the output is semantically plausible
 * @param testSafety whether the output is non-routable or otherwise suitable as a test fixture
 */
public record ProviderSafetyMetadata(ProviderValidity formatValidity,
                                     ProviderValidity checksumValidity,
                                     ProviderValidity semanticPlausibility,
                                     ProviderTestSafety testSafety) {

    public ProviderSafetyMetadata {
        Objects.requireNonNull(formatValidity, "formatValidity must not be null");
        Objects.requireNonNull(checksumValidity, "checksumValidity must not be null");
        Objects.requireNonNull(semanticPlausibility, "semanticPlausibility must not be null");
        Objects.requireNonNull(testSafety, "testSafety must not be null");
    }

    static ProviderSafetyMetadata unclassified() {
        return new ProviderSafetyMetadata(ProviderValidity.UNCLASSIFIED,
                                          ProviderValidity.UNCLASSIFIED,
                                          ProviderValidity.UNCLASSIFIED,
                                          ProviderTestSafety.UNCLASSIFIED);
    }
}
