package org.github.krandom.games.dice

import java.util.*

class Dice<E>(private val values: List<E>) : IDice<E> {

    override fun roll(nrTimes: Int): E {
        return rolls(nrTimes).last()
    }

    override fun rolls(nrTimes: Int): List<E> {
        require(nrTimes > 0) { "nr of times should be > 0" }
        var result = generateResult(nrTimes)

        //TODO improve this block somehow rewrite logic, ita happens a lot of times, turn on trace in testing logs and see logs
        if (nrTimes > values.size) {
            val r = generateResult(nrTimes - values.size).toMutableList()
            r.addAll(values)
            r.shuffle()
            result = r.toList()
        }

        return result
    }

    private fun generateResult(nrTimes: Int): List<E> {
        val indexes = Random().ints(nrTimes.toLong(), 0, values.size).toArray()
        return indexes.map { values[it] }.toList()
    }
}
