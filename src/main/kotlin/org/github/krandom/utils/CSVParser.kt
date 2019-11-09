package org.github.krandom.utils

object CSVParser {

    const val csvDelimiter = ",\n"

    fun parse(content: String, delimiter: String): List<String> {
        return content
                .split(delimiter)
                .toList()
    }
}
