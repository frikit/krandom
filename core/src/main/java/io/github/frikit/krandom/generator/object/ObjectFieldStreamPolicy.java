/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

/** Controls independent random streams for seed-owned object members. */
public enum ObjectFieldStreamPolicy {
    /** Preserves existing output: named streams only for portable configurations. */
    LEGACY,
    /** Uses named member streams even with custom rules, exclusions, or installed modules. */
    INDEPENDENT
}
