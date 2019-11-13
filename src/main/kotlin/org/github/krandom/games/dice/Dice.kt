package org.github.krandom.games.dice

import org.github.krandom.games.dice.enum.DiceType

interface Dice {

    fun roll(): String

    companion object {
        fun init(diceType: DiceType, values: List<String> = emptyList()): Dice {
            return when(diceType) {
                DiceType.D4 -> DiceD4(values)
                DiceType.D6 -> DiceD4(values)
                DiceType.D8 -> DiceD4(values)
                DiceType.D10 -> DiceD4(values)
                DiceType.D12 -> DiceD4(values)
                DiceType.D20 -> DiceD4(values)
            }
        }
    }
}
