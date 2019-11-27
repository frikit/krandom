package org.github.krandom.games.dice

import java.util.*

class Dice<E>(private val values: List<E>) : IDice<E> {

    override fun roll(nrTimes: Int): E {
        return rolls(nrTimes).last()
    }

    override fun rolls(nrTimes: Int): List<E> {
        require(nrTimes > 0) { "nr of times should be > 0" }

        return if (nrTimes > values.size) {
            val firstPart = generateResult(nrTimes - values.size)
            (firstPart + values).shuffled()
        } else {
            generateResult(nrTimes)
        }
    }

    private fun generateResult(nrTimes: Int): List<E> {
        val indexes = Random().ints(nrTimes.toLong(), 0, values.size).toArray()
        return indexes.map { values[it] }.toList()
    }
}
