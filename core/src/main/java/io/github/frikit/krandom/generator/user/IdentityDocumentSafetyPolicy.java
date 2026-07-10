/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

/**
 * Safety policy for generated passport and driving-license identifiers.
 *
 * <p>Generic document-number shapes can overlap with real issuers. A plausible format is not
 * evidence that a value is suitable for travel, licensing, identity verification, or an external
 * system.
 */
public enum IdentityDocumentSafetyPolicy {

    /** Refuses to generate document identifiers because no portable safe-fixture contract exists. */
    DISABLED,

    /** Restores prior plausible output without making a safety or non-routability claim. */
    REALISTIC_UNCLASSIFIED;

    void requireRealisticOutput() {
        if (this == DISABLED) {
            throw new IllegalStateException(
                "Identity-document generation is disabled by default; select "
                + "identityDocumentSafetyPolicy(REALISTIC_UNCLASSIFIED) only for isolated fixtures");
        }
    }
}
