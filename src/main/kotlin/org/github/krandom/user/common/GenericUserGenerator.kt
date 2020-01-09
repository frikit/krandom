package org.github.krandom.user.common

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer
import org.github.krandom.utils.CSVParser
import org.github.krandom.utils.ResourceResolver

abstract class GenericUserGenerator<T> {
    protected val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    open var maxAllowSize = 100_000

    protected fun initCache(relativePath: String): List<T> {
        val content = ResourceResolver.getResourceContent(relativePath)
        val list = CSVParser<T>().parse(content, CSVParser.csvDelimiter)
        validateList(list)

        return list
    }

    protected fun randomData(list: List<T>): T {
        val index = kRandomCommon.randomInt(0, list.size)
        return list[index]
    }

    protected fun randomDatas(list: List<T>, size: Int): List<T> {
        validateList(list)
        isValidSize(size)

        //TODO optimize make linked list and extract a bunch of indexes, or not need to do, so have no idea
        val randomInits = generateIntSeq(size, list.size)
        val res = randomInits.map { list[it] }.toList()

        validateList(res)
        return res
    }

    private fun generateIntSeq(size: Int, maxIndex: Int): List<Int> {
        return (1..size).map { kRandomCommon.randomInt(0, maxIndex) }.toList()
    }

    protected fun isValidSize(size: Int) {
        require(size <= maxAllowSize) { "Size cannot be < $maxAllowSize!" }
        require(size >= 1) { "Size cannot be >= 1!" }
    }

    private fun validateList(list: List<T>) {
        require(list.isNotEmpty()) { "List with options can't be empty!" }
    }
}
