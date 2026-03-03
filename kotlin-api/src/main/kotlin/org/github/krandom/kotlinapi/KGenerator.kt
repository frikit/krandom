/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.github.krandom.generator.Generator

/**
 * Kotlin-native facade over [Generator].
 */
class KGenerator<T>(private val delegate: Generator<T>) {

    fun next(): T = delegate.generate()

    fun take(count: Int): List<T> = delegate.generateList(count)

    fun asSequence(): Sequence<T> = generateSequence { next() }

    fun nextOrNull(): T? = runCatching { next() }.getOrNull()

    fun nextResult(): Result<T> = runCatching { next() }

    fun underlying(): Generator<T> = delegate
}
