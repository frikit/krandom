/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.games.dice

import org.github.krandom.games.dice.enum.DiceType

interface IDice<E> {

    fun roll(nrTimes: Int = 1): E

    fun rolls(nrTimes: Int = 1): List<E>

    companion object {
        fun <E> init(diceType: DiceType, values: List<E>): IDice<E> {
            validator(diceType.nrFaces, values)
            return Dice(values)
        }

        private fun <E> validator(nrFaces: Int, values: List<E>) {
            require(nrFaces == values.size) {
                "Value you passed is not valid one, actual:[${values.size}], expected:[$nrFaces]"
            }
        }
    }
}
