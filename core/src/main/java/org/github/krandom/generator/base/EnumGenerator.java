/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates a random constant from an enum type.
 *
 * <pre>{@code
 *   EnumGenerator<Status> gen = new EnumGenerator<>(Status.class);
 *   Status s = gen.generate();
 *
 *   // Or via the generic Generators factory after registration
 * }</pre>
 *
 * @param <E> the enum type
 */
public final class EnumGenerator<E extends Enum<E>> implements Generator<E> {

    private final E[] constants;
    private final RandomGenerator random;

    /** Uses {@link java.security.SecureRandom}. */
    public EnumGenerator(Class<E> enumClass) {
        this(enumClass, null);
    }

    /** Uses a seeded {@link Random} for reproducible output. */
    public EnumGenerator(Class<E> enumClass, Long seed) {
        Objects.requireNonNull(enumClass, "enumClass must not be null");
        this.constants = enumClass.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new IllegalArgumentException("Enum " + enumClass.getName() + " has no constants");
        }
        this.random = seed != null ? new Random(seed) : new SecureRandom();
    }

    @Override
    public E generate() {
        return constants[random.nextInt(constants.length)];
    }
}
