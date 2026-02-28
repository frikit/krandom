/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.selection;

import org.github.krandom.generator.Generator;

import java.util.List;
import java.util.Objects;

/**
 * Generates a fixed-size list by repeatedly invoking a source generator.
 *
 * @param <T> element type
 */
public final class RepeatGenerator<T> implements Generator<List<T>> {

    private final Generator<T> source;
    private final int count;

    /**
     * Creates a repeat generator.
     *
     * @param source source generator; must not be null
     * @param count number of values per call
     */
    public RepeatGenerator(Generator<T> source, int count) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
        this.count = count;
    }

    @Override
    public List<T> generate() {
        return source.generateList(count);
    }
}

