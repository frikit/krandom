/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

/**
 * Safety policy for generated cryptocurrency destination-address shapes.
 *
 * <p>A plausible address is not evidence that it belongs to a test network, is unspendable, or is
 * safe to submit to a wallet or blockchain service.
 */
public enum CryptoAddressSafetyPolicy {

    /** Refuses to generate address shapes because no portable chain-safe fixture contract exists. */
    DISABLED,

    /** Restores prior plausible output without making a safety or network-routing claim. */
    REALISTIC_UNCLASSIFIED;

    void requireRealisticOutput() {
        if (this == DISABLED) {
            throw new IllegalStateException(
                "Crypto-address generation is disabled by default; select "
                + "cryptoAddressSafetyPolicy(REALISTIC_UNCLASSIFIED) only for isolated fixtures");
        }
    }
}
