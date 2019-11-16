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
        val result = arrayListOf<String>()
        for (index in 0..nrTimes) {
            val indexOfElem = kRandomCommon.randomInt(range)
            result.add(values[indexOfElem])
        }

        val indexOfElem = kRandomCommon.randomInt(0, result.size - 1)

        //TODO decide last or random from a thrown
        return if (result.size == 1) result[0] else result[indexOfElem]
    }
}
