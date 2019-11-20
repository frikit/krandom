package org.github.krandom.user.common

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer
import org.github.krandom.exceptions.NegativeSizeException
import org.github.krandom.exceptions.SizeLimitExceedException
import org.github.krandom.utils.CSVParser
import org.github.krandom.utils.ResourceResolver

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

        //TODO optimize make linked list and extract a bunch of indexes, or not need to do so have no idea
        val randomInits = generateIntSeq(size)
        val res = randomInits.map { list[it] }.toMutableList()

        validateList(res)
        return res
    }

    private fun generateIntSeq(size: Int): MutableList<Int> {
        return (1..size).map {
            kRandomCommon.randomInt(0, size)
        }.toMutableList()
    }

    protected fun isValidSize(size: Int) {
        if (size > maxAllowSize) throw SizeLimitExceedException("Size cannot be > $maxAllowSize!")
        if (size < 1) throw NegativeSizeException("Size cannot be < 1!")
    }

    private fun validateList(list: List<String>) {
        require(list.isNotEmpty()) { "List with options can't be empty!" }
        require(!list.none { it != "" }) { "List with options can't be empty or consist only of empty elements!" }
    }
}
