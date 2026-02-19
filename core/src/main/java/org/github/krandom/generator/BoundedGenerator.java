package org.github.krandom.generator;

/**
 * A {@link Generator} that additionally supports bounded (ranged) generation.
 *
 * <p>Range convention — mirrors Java's {@code Random} API and Java streams:
 * the range is <b>half-open [min, max)</b>: {@code min} is inclusive, {@code max} is exclusive.
 *
 * <p>The no-argument {@link #generate()} method always delegates to
 * {@link #generate(Comparable, Comparable)} using the bounds configured at construction time,
 * so a {@code BoundedGenerator} created with specific bounds behaves correctly when used as
 * a plain {@code Generator<T>}.
 *
 * @param <T> the numeric/comparable type produced; must implement {@link Comparable}
 */
public interface BoundedGenerator<T extends Comparable<T>> extends Generator<T> {

    /**
     * Produce a value in the half-open range [{@code min}, {@code max}).
     *
     * @param min lower bound, inclusive
     * @param max upper bound, exclusive
     * @throws IllegalArgumentException if {@code min >= max}
     */
    T generate(T min, T max);

    /** The lower bound (inclusive) configured at construction time. */
    T getMin();

    /** The upper bound (exclusive) configured at construction time. */
    T getMax();
}
