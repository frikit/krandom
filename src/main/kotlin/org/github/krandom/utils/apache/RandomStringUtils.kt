package org.github.krandom.utils.apache

import java.util.*

object RandomStringUtils {

    private val RANDOM = Random()

    fun random(count: Int, start: Int, end: Int, letters: Boolean, numbers: Boolean, chars: CharArray? = null): String {

        require(!(chars != null && chars.isEmpty())) {
            "The chars array must not be empty"
        }

        val pair = getStartEndChars(start, end, chars, letters, numbers)
        val startChars = pair.first
        val endChars = pair.second

        validateEndChar(chars, numbers, letters, endChars)

        val gap = endChars - startChars
        return generateResult(count, chars, gap, startChars, letters, numbers).toString()
    }

    private fun validateEndChar(chars: CharArray?, numbers: Boolean, letters: Boolean, endChars: Int) {
        val zeroDigitAscii = 48
        val firstLetterAscii = 65

        //Split if for the easy track on code coverage

        require(!(chars == null && numbers && endChars <= zeroDigitAscii)) {
            "Parameter end (" + endChars + ") must be greater then (" + zeroDigitAscii + ") for generating digits " +
                    "or greater then (" + firstLetterAscii + ") for generating letters."
        }

        require(!(chars == null && letters && endChars <= firstLetterAscii)) {
            "Parameter end (" + endChars + ") must be greater then (" + zeroDigitAscii + ") for generating digits " +
                    "or greater then (" + firstLetterAscii + ") for generating letters."
        }
    }

    private fun generateResult(countChars: Int, chars: CharArray?, gap: Int, startChars: Int, letters: Boolean, numbers: Boolean): StringBuilder {
        var countChars1 = countChars
        val builder = StringBuilder(countChars1)

        loop@ while (countChars1-- != 0) {
            val codePoint: Int = getNextCodePoint(chars, gap, startChars)

            val numberOfChars = Character.charCount(codePoint)

            if (letters && Character.isLetter(codePoint)
                    || numbers && Character.isDigit(codePoint)
                    || !letters && !numbers) {
                builder.appendCodePoint(codePoint)

                if (numberOfChars == 2) {
                    countChars1--
                }

            } else {
                countChars1++
            }
        }
        return builder
    }

    private fun getNextCodePoint(chars: CharArray?, gap: Int, startChars: Int): Int {
        val randomInt = RANDOM.nextInt(gap) + startChars
        val result = if (chars == null) {
            randomInt
        } else {
            chars[randomInt].toInt()
        }

        return result
    }

    private fun getStartEndChars(startChars: Int, endChars: Int, chars: CharArray?, letters: Boolean, numbers: Boolean): Pair<Int, Int> {
        var startChars1 = startChars
        var endChars1 = endChars
        if (startChars1 == 0 && endChars1 == 0) {
            if (chars != null) {
                endChars1 = chars.size
            } else {
                if (!letters && !numbers) {
                    endChars1 = Character.MAX_CODE_POINT
                } else {
                    endChars1 = 'z'.toInt() + 1
                    startChars1 = ' '.toInt()
                }
            }
        } else {
            require(endChars1 > startChars1) {
                "Parameter end ($endChars1) must be greater than start ($startChars1)"
            }
        }
        return Pair(startChars1, endChars1)
    }
}
