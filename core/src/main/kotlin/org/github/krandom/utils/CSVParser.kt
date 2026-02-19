/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
@file:Suppress("UNCHECKED_CAST")

package org.github.krandom.utils

object CSVParser {

    const val csvDelimiter = ",\n"

    fun <T> parse(content: String, delimiter: String): List<T> {
        return if (content.isBlank()) {
            emptyList()
        } else {
            content
                    .split(delimiter)
                    .map { it as T }
                    .toList()
        }
    }
}
