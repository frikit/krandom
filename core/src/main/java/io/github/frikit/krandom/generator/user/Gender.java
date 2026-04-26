/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

/**
 * Biological sex / gender used to select locale-appropriate first names.
 *
 * <p>Pass to {@link FirstNameGenerator#generate(Gender)} to restrict output to names
 * traditionally associated with that gender in the configured locale.
 */
public enum Gender {
    MALE,
    FEMALE
}
