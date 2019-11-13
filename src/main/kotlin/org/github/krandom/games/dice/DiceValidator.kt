package org.github.krandom.games.dice

object DiceValidator {

    fun validateSize(passedListSize: Int, expectedListSize: Int) {
        require(passedListSize == expectedListSize) {
            "Value you passed is not valid one actual:[$passedListSize], expected:[$expectedListSize]"
        }
    }
}
