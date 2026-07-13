/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

/**
 * Selects how {@link ObjectGenerator} creates non-record class instances.
 */
public enum ObjectConstructionPolicy {

    /**
     * Invoke a no-argument or one unambiguous declared constructor.
     *
     * <p>Named-module consumers must open packages containing reflectively accessed constructors
     * or mutable fields to module {@code io.github.frikit.krandom}.
     */
    SAFE_CONSTRUCTORS,

    /**
     * Permit constructor bypass through Objenesis when no no-argument constructor exists.
     *
     * <p>This compatibility mode can skip constructor invariants and field initializers. It does
     * not bypass JPMS access checks for mutable fields populated after allocation.
     */
    UNSAFE_CONSTRUCTOR_BYPASS
}
