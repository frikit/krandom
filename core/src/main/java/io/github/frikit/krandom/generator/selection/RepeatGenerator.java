/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Generates a fixed-size list by repeatedly invoking a source generator.
 *
 * <p>The list keeps every value the source generates, {@code null} included, so the element type may
 * be nullable, as in {@code RepeatGenerator<@Nullable Boolean>} over
 * {@link io.github.frikit.krandom.generator.Generators#ofNullableBoolean()}.
 *
 * @param <T> element type, which may be nullable
 */
public final class RepeatGenerator<T extends @Nullable Object> implements Generator<List<T>> {

    private final Generator<T> source;
    private final int count;

    /**
     * Creates a repeat generator.
     *
     * @param source source generator; must not be null
     * @param count  number of values per call
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
