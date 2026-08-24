/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import java.util.Objects;
import java.util.Optional;

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
 * @param safetyPolicy optional configuration policy that determines the safety contract
 */
public record ProviderSafetyMetadata(ProviderValidity formatValidity,
                                     ProviderValidity checksumValidity,
                                     ProviderValidity semanticPlausibility,
                                     ProviderTestSafety testSafety,
                                     Optional<ProviderSafetyPolicy> safetyPolicy) {

    /**
     * Creates metadata without a configuration-dependent safety policy.
     *
     * @param formatValidity whether the documented output format is guaranteed
     * @param checksumValidity whether checksum behavior is guaranteed
     * @param semanticPlausibility whether the output is semantically plausible
     * @param testSafety whether the output is suitable as a test fixture
     */
    public ProviderSafetyMetadata(ProviderValidity formatValidity,
                                  ProviderValidity checksumValidity,
                                  ProviderValidity semanticPlausibility,
                                  ProviderTestSafety testSafety) {
        this(formatValidity, checksumValidity, semanticPlausibility, testSafety, Optional.empty());
    }

    /**
     * Creates metadata controlled by one configuration safety policy.
     *
     * @param formatValidity whether the documented output format is guaranteed
     * @param checksumValidity whether checksum behavior is guaranteed
     * @param semanticPlausibility whether the output is semantically plausible
     * @param testSafety whether the output is suitable as a test fixture
     * @param safetyPolicy policy that determines the safety contract
     */
    public ProviderSafetyMetadata(ProviderValidity formatValidity,
                                  ProviderValidity checksumValidity,
                                  ProviderValidity semanticPlausibility,
                                  ProviderTestSafety testSafety,
                                  ProviderSafetyPolicy safetyPolicy) {
        this(formatValidity,
             checksumValidity,
             semanticPlausibility,
             testSafety,
             Optional.of(Objects.requireNonNull(safetyPolicy, "safetyPolicy must not be null")));
    }

    public ProviderSafetyMetadata {
        Objects.requireNonNull(formatValidity, "formatValidity must not be null");
        Objects.requireNonNull(checksumValidity, "checksumValidity must not be null");
        Objects.requireNonNull(semanticPlausibility, "semanticPlausibility must not be null");
        Objects.requireNonNull(testSafety, "testSafety must not be null");
        safetyPolicy = Objects.requireNonNull(safetyPolicy, "safetyPolicy must not be null");
    }

    public static ProviderSafetyMetadata unclassified() {
        return new ProviderSafetyMetadata(ProviderValidity.UNCLASSIFIED,
                                          ProviderValidity.UNCLASSIFIED,
                                          ProviderValidity.UNCLASSIFIED,
                                          ProviderTestSafety.UNCLASSIFIED);
    }

    /**
     * Returns whether this value makes no validity or test-safety claims.
     *
     * @return true when every classification is unclassified and no policy is attached
     */
    public boolean isUnclassified() {
        return equals(unclassified());
    }
}
