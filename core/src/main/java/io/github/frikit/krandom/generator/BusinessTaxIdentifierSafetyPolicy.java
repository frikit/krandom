/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

/**
 * Safety policy for generated corporate tax identifiers.
 *
 * <p>CNPJ and EIN values identify real organisations. A plausible format or valid check digit is
 * not evidence that an identifier is safe for tax, banking, licensing, or another external system.
 */
public enum BusinessTaxIdentifierSafetyPolicy {

    /** Refuses to generate corporate tax identifiers because no portable safe-fixture contract exists. */
    DISABLED,

    /** Restores prior plausible output without making a safety or non-routability claim. */
    REALISTIC_UNCLASSIFIED
}
