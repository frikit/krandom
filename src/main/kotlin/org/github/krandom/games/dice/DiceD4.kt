package org.github.krandom.games.dice

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer
import org.github.krandom.games.dice.enum.DiceDefault

class DiceD4(values: List<String>) : Dice {
    private val values: List<String>
    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }

    init {
        if (values.isEmpty()) {
            val assign: List<String> = DiceDefault.diceD4Default
            this.values = assign
        } else {
            this.values = values
        }
    }

    override fun roll(): String {
        val index = kRandomCommon.randomInt(0..4)
        return values[index]
    }
}
