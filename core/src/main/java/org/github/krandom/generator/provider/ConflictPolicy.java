/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.provider;

/**
 * Conflict handling policy for provider and alias registration.
 */
public enum ConflictPolicy {
    /**
     * Throw an exception if a target name already exists.
     */
    FAIL,
    /**
     * Replace the previous value when a target name already exists.
     */
    REPLACE
}
