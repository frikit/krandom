package krandom.user.common

import krandom.common.KRandomCommon
import krandom.common.Randomizer
import krandom.exceptions.NegativeSizeException
import krandom.exceptions.SizeLimitExceedException
import krandom.utils.CSVParser
import krandom.utils.ResourceResolver
import java.lang.IllegalArgumentException

abstract class GenericUserGenerator {
    val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    open var maxAllowSize = 10_000

    protected fun initCache(relativePath: String): List<String> {
        val content = ResourceResolver.getResourceContent(relativePath)
        val list = CSVParser.parse(content, CSVParser.csvDelimiter).requireNoNulls()
        validateList(list)

        return list
    }

    protected fun randomData(list: List<String>): String {
        val index = kRandomCommon.randomInt(0, list.size - 1)
        return list[index]
    }

    protected fun randomDatas(list: List<String>, size: Int): List<String> {
        require(list.isNotEmpty()) { "List should have elements!!!" }
        isValidSize(size)
        val res = list.run { shuffled().take(size).toList() }
        require(res.isNotEmpty()) { "WTF?! [$res]" }
        return res
    }

    protected fun isValidSize(size: Int) {
        if (size > maxAllowSize) throw SizeLimitExceedException("Size cannot be > $maxAllowSize!")
        if (size < 1) throw NegativeSizeException("Size cannot be < 1!")
    }

    private fun validateList(list: List<String>) {
        if (list.isEmpty()) throw java.lang.IllegalArgumentException("List with options can't be empty!")
        if (list.none { it != "" }) throw java.lang.IllegalArgumentException("List with options can't be empty or consist only of empty elements!")
    }
}
