/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

/**
 * Safety policy for generated banking identifiers and account values.
 *
 * <p>IBANs, ABA routing numbers, BICs, and account numbers can identify real institutions or
 * accounts. A correct format or checksum is not evidence that a value is non-routable or suitable
 * for a payment system.
 */
public enum BankingSafetyPolicy {

    /** Refuses to generate banking identifiers because no portable safe-fixture contract exists. */
    DISABLED,

    /** Restores prior plausible output without making a safety or non-routability claim. */
    REALISTIC_UNCLASSIFIED;

    void requireRealisticOutput() {
        if (this == DISABLED) {
            throw new IllegalStateException(
                "Banking identifier generation is disabled by default; select "
                + "bankingSafetyPolicy(REALISTIC_UNCLASSIFIED) only for isolated fixtures");
        }
    }
}
