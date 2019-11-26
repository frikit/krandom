package org.github.krandom.games.dice

import org.github.krandom.games.dice.enum.DiceType

interface IDice<E> {

    fun roll(nrTimes: Int = 1): E

    fun rolls(nrTimes: Int = 1): List<E>

    companion object {
        fun <E> init(diceType: DiceType, values: List<E>): IDice<E> {
            validator(diceType.nrFaces, values)
            return when (diceType) {
                DiceType.D4 -> Dice(values)
                DiceType.D6 -> Dice(values)
                DiceType.D8 -> Dice(values)
                DiceType.D10 -> Dice(values)
                DiceType.D12 -> Dice(values)
                DiceType.D20 -> Dice(values)
            }
        }

        private fun <E> validator(nrFaces: Int, values: List<E>) {
            require(nrFaces == values.size) {
                "Value you passed is not valid one, actual:[${values.size}], expected:[$nrFaces]"
            }
        }
    }
}
