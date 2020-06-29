@file:Suppress("UNCHECKED_CAST")

package org.github.krandom.utils

class CSVParser<T> {

    companion object {
        const val csvDelimiter = ",\n"
    }

    fun parse(content: String, delimiter: String): List<T> {
        return if (content.isBlank() || content.isEmpty()) {
            emptyList()
        } else {
            content
                    .split(delimiter)
                    .map { it as T }
                    .toList()
        }
    }
}
