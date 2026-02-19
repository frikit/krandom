/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

/**
 * Java record — used to verify that {@link org.github.krandom.generator.object.ObjectGenerator}
 * discovers record components, generates values for each, and invokes the canonical constructor.
 *
 * Records are intentionally immutable: the generator must use the canonical constructor
 * rather than no-arg construction + field injection.
 */
public record PersonRecord(
        String firstName,
        String lastName,
        int    age,
        double salary,
        Status status,
        Address address
) { }
