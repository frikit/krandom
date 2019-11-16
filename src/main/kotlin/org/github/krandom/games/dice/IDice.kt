package org.github.krandom.games.dice

import org.github.krandom.games.dice.enum.DiceType

interface IDice {

    fun roll(nrTimes: Int = 1): String

    fun rolls(nrTimes: Int = 1): List<String>

    companion object {
        fun init(diceType: DiceType, values: List<String> = emptyList()): IDice {
            return when (diceType) {
                DiceType.D4 -> Dice(diceType.nrFaces, getValue(values, diceType))
                DiceType.D6 -> Dice(diceType.nrFaces, getValue(values, diceType))
                DiceType.D8 -> Dice(diceType.nrFaces, getValue(values, diceType))
                DiceType.D10 -> Dice(diceType.nrFaces, getValue(values, diceType))
                DiceType.D12 -> Dice(diceType.nrFaces, getValue(values, diceType))
                DiceType.D20 -> Dice(diceType.nrFaces, getValue(values, diceType))
            }
        }

        private fun getValue(values: List<String>, diceType: DiceType): List<String> {
            return if (values.isEmpty()) diceType.defaultValues else values
        }
    }
}
