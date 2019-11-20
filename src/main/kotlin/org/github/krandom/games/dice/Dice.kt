package org.github.krandom.games.dice

import mu.KLogging
import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer
import java.util.*

class Dice<E>(nrFaces: Int, private val values: List<E>) : IDice<E> {
    private val logger by lazy { KLogging().logger("Dice") }
    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    private val range = 0..nrFaces

    override fun roll(nrTimes: Int): E {
        return rolls(nrTimes).last()
    }

    override fun rolls(nrTimes: Int): List<E> {
        require(nrTimes > 0) { "nr of times should be > 0" }
        var result = generateResult(nrTimes)

        if (nrTimes > values.size * 2) {
            //make sure all values are different when generate them
            (0..99).forEach { index ->
                if (!isValidResult(index, result)) {
                    result = generateResult(nrTimes)
                } else {
                    return result
                }
            }
        }
        return result
    }

    private fun isValidResult(index: Int, result: List<E>): Boolean {
        for (it in values) {
            if (!result.contains(it)) {
                logger.trace { "[$index idx] Because there is no [$it] in results and need to regenerate {$result}" }
                return false
            }
        }

        return true
    }

    private fun generateResult(nrTimes: Int): List<E> {
        val indexes = Random().ints(nrTimes.toLong(), 0, values.size).toArray()
        return indexes.map { values[it] }.toList()
    }
}
