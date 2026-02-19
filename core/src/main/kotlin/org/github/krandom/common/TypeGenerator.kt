package org.github.krandom.common

/**
 * Core generic abstraction for producing a random value of type [T].
 *
 * Implemented as a functional interface so it can be used as a lambda:
 *   val gen: TypeGenerator<Int> = TypeGenerator { (0..100).random() }
 */
fun interface TypeGenerator<T> {
    fun generate(): T
}

/**
 * Extension of [TypeGenerator] for types that support bounded generation.
 *
 * Range convention (mirrors Java's Random API): [start, end) — start inclusive, end exclusive.
 * The [generate] overload that accepts a [ClosedRange] delegates to [generate(start, end)],
 * so the inclusive upper bound of the range is treated as exclusive here too (matching
 * the existing KRandomInt / Randomizer behaviour in this codebase).
 */
interface RangedTypeGenerator<T : Comparable<T>> : TypeGenerator<T> {

    /** Generate a value in the half-open range [start, end). */
    fun generate(start: T, end: T): T

    /** Convenience overload accepting a Kotlin [ClosedRange]. */
    fun generate(range: ClosedRange<T>): T = generate(range.start, range.endInclusive)
}
