package org.github.krandom.games.dice.utils

object DiceTypeUtils {

    fun generateExpected(end: Int): List<String> {
        return (1..end).map { it.toString() }.toList()
    }

}
