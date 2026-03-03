/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.github.krandom.generator.schema.Schema
import java.util.Locale

/**
 * Kotlin wrapper for schema bulk generation.
 */
class KSchema(private val delegate: Schema) {

    fun next(): Map<String, Any?> = delegate.generate().mapValues { it.value }

    fun take(count: Int): List<Map<String, Any?>> =
        delegate.generateBatch(count).map { row -> row.mapValues { it.value } }

    fun locale(): Locale = delegate.locale

    fun underlying(): Schema = delegate
}
