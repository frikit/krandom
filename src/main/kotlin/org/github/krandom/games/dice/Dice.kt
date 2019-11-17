package org.github.krandom.games.dice

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer

class Dice<E>(nrFaces: Int, private val values: List<E>) : IDice<E> {
    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    private val range = 0..nrFaces

    override fun roll(nrTimes: Int): E {
        return rolls(nrTimes).last()
    }

    override fun rolls(nrTimes: Int): List<E> {
        require(nrTimes > 0) { "nr of times should be > 0" }
        val result = arrayListOf<E>()
        (1..nrTimes).forEach { _ ->
            val indexOfElem = kRandomCommon.randomInt(range)
            result.add(values[indexOfElem])
        }

        return result
    }
}
