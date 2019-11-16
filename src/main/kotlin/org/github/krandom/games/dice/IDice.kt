package org.github.krandom.games.dice

import org.github.krandom.games.dice.enum.DiceType

interface IDice {

    fun roll(): String

    companion object {
        fun init(diceType: DiceType, values: List<String> = emptyList()): IDice {
            return when (diceType) {
                DiceType.D4 -> Dice(diceType.nrFaces, values, diceType.defaultValues)
                DiceType.D6 -> Dice(diceType.nrFaces, values, diceType.defaultValues)
                DiceType.D8 -> Dice(diceType.nrFaces, values, diceType.defaultValues)
                DiceType.D10 -> Dice(diceType.nrFaces, values, diceType.defaultValues)
                DiceType.D12 -> Dice(diceType.nrFaces, values, diceType.defaultValues)
                DiceType.D20 -> Dice(diceType.nrFaces, values, diceType.defaultValues)
            }
        }
    }
}
