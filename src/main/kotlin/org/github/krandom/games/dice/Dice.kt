package org.github.krandom.games.dice

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer
import java.lang.IllegalArgumentException

class Dice(nrFaces: Int, values: List<String>, defaultValues: List<String> = emptyList()) : IDice {
    private val values: List<String>
    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    private val range = 0..nrFaces

    init {
        when {
            values.isEmpty() -> this.values = defaultValues
            nrFaces == values.size -> this.values = values
            else -> throw IllegalArgumentException("Value you passed is not valid one, actual:[${values.size}], expected:[$nrFaces]")
        }
    }

    override fun roll(): String {
        val index = kRandomCommon.randomInt(range)
        return values[index]
    }
}
