package org.github.krandom.games.dice

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer
import java.lang.IllegalArgumentException

class Dice(nrFaces: Int, values: List<String>) : IDice {
    private val values: List<String>
    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    private val range = 0..nrFaces

    init {
        when (nrFaces) {
            values.size -> this.values = values
            else -> throw IllegalArgumentException("Value you passed is not valid one, actual:[${values.size}], expected:[$nrFaces]")
        }
    }

    override fun roll(nrTimes: Int): String {
        return rolls(nrTimes).last()
    }

    override fun rolls(nrTimes: Int): List<String> {
        require(nrTimes > 0) {"nr of times should be > 0"}
        val result = arrayListOf<String>()
        (1..nrTimes).forEach { _ ->
            val indexOfElem = kRandomCommon.randomInt(range)
            result.add(values[indexOfElem])
        }

        return result
    }
}
