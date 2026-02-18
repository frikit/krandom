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
