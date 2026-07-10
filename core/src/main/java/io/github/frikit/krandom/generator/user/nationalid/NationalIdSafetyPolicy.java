/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

/**
 * Safety policy for generated national identity numbers.
 *
 * <p>National-ID formats and checksums can overlap with real identities. A checksum-valid or
 * realistic-looking value is not a safe test fixture or an authorization to use it with an
 * external system.
 */
public enum NationalIdSafetyPolicy {

    /** Refuses to generate a national ID because no cross-country safe fixture contract exists. */
    DISABLED,

    /** Restores prior realistic-looking output without making a safety or non-routability claim. */
    REALISTIC_UNCLASSIFIED
}
