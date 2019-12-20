package org.github.krandom.utils

class CSVParser<T> {

    companion object {
        const val csvDelimiter = ",\n"
    }

    fun parse(content: String, delimiter: String): List<T> {
        return content
                .split(delimiter)
                .map { it as T }
                .toList()
    }
}
