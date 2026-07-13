/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

/**
 * Safety policy for generated securities identifiers.
 *
 * <p>ISIN and CUSIP values identify real financial instruments. A plausible format or valid check
 * digit is not evidence that an identifier is assigned, non-routable, or safe for trading,
 * custody, clearing, settlement, or another external system.
 */
public enum SecuritiesIdentifierSafetyPolicy {

    /** Refuses to generate securities identifiers because no portable safe-fixture contract exists. */
    DISABLED,

    /** Restores prior plausible output without making a safety or non-routability claim. */
    REALISTIC_UNCLASSIFIED;

    void requireRealisticOutput() {
        if (this == DISABLED) {
            throw new IllegalStateException(
                "Securities-identifier generation is disabled by default; select "
                + "securitiesIdentifierSafetyPolicy(REALISTIC_UNCLASSIFIED) only for isolated fixtures");
        }
    }
}
