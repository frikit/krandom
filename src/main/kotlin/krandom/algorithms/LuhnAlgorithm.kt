package krandom.algorithms

import krandom.common.KRandom
import krandom.common.Randomizer

object LuhnAlgorithm {

    private val kRandom: KRandom by lazy { Randomizer() }

    fun randomNumber(): String {
        val randomNumberLength = 9

        val builder = StringBuilder()
        for (i in 0 until randomNumberLength) {
            val digit = kRandom.randomInt(1, 10)
            builder.append(digit)
        }

        // Do the Luhn algorithm to generate the check digit.
        val checkDigit = getCheckDigit(builder.toString())
        builder.append(checkDigit)

        return builder.toString()
    }

    private fun getCheckDigit(number: String): Int {
        var sum = 0
        for (i in 0 until number.length) {

            // Get the digit at the current position.
            var digit = Integer.parseInt(number.substring(i, i + 1))

            if (i % 2 == 0) {
                digit *= 2
                if (digit > 9) {
                    digit = digit / 10 + digit % 10
                }
            }
            sum += digit
        }

        // The check digit is the number required to make the sum a multiple of
        // 10.
        val mod = sum % 10
        return if (mod == 0) 0 else 10 - mod
    }
}