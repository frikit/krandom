package krandom.user.common

import krandom.common.KRandomCommon
import krandom.common.Randomizer
import krandom.utils.CSVParser
import krandom.utils.ResourceResolver

abstract class GenericUserGenerator {
    val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    open var maxAllowSize = 10_000

    fun initCache(relativePath: String): List<String> {
        val content = ResourceResolver.getResourceContent(relativePath)
        val list = CSVParser.parse(content, CSVParser.csvDelimiter).requireNoNulls()
        validateList(list)

        return list
    }

    private fun validateList(list: List<String>) {
        if (list.isEmpty()) throw java.lang.IllegalArgumentException("List with options can't be empty!")
        if (list.none { it != "" }) throw java.lang.IllegalArgumentException("List with options can't be empty or consist only of empty elements!")
    }
}