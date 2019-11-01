package krandom.utils

object RandomizerUtils {

    private fun checkSameValues(x: Number, y: Number): Boolean {
        return x == y
    }

    fun checkThrowException(x: Number, y: Number) {
        if (checkSameValues(x, y)) {
            throw IllegalArgumentException("Illegal argument passed start = $x and end = $y, they should be different!")
        }
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

    fun validateLength(length: Int) {
        if (length < 1) throw IllegalAccessException("Length can't be < 1")
    }
}
