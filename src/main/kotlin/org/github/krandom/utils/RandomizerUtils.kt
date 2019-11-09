package org.github.krandom.utils

object RandomizerUtils {

    fun checkThrowException(x: Number, y: Number) {
        require(x != y) { "Illegal argument passed start = $x and end = $y, they should be different!" }
    }

    fun getCharCombinations(upperLetters: Boolean,
                            lowerLetters: Boolean,
                            numbers: Boolean,
                            specialCharacters: Boolean): List<Pair<Int, Int>> {
        val combinations: MutableList<Pair<Int, Int>> = mutableListOf()
        if (upperLetters) combinations.add(66 to 90)
        if (lowerLetters) combinations.add(97 to 122)
        if (numbers) combinations.add(48 to 57)
        if (specialCharacters) combinations.addAll(
                listOf(
                        33 to 47,
                        58 to 64,
                        91 to 96,
                        123 to 126)
        )

        return combinations.toList()
    }

    fun validateLength(length: Int) = require(length >= 1) { "Length can't be < 1" }

    fun generateRandomString(function: () -> String, numbers: Boolean): String {
        var res: String = function.invoke()
        if (numbers) {
            for (i in 0..99) {
                if (!res.contains("[0-9]+".toRegex())) {
                    res = function.invoke()
                } else {
                    return res
                }
            }
            //TODO test somehow this method, if there is no way to test it with mocks
            res = ""
        }
        return res
    }
}
