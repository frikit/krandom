/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.github.krandom.utils.apache

import java.util.*

object RandomStringUtils {

    private val RANDOM = Random()

    fun random(count: Int, start: Int, end: Int, letters: Boolean, numbers: Boolean, chars: CharArray? = null): String {

        require(!(chars != null && chars.isEmpty())) { "The chars array must not be empty" }

        val pair = getStartEndChars(start, end, chars, letters, numbers)
        val startChars = pair.first
        val endChars = pair.second

        val zeroDigitAscii = 48
        val firstLetterAscii = 65

        //TODO test this part with mock or somehow
        require(!(chars == null && (numbers && endChars <= zeroDigitAscii || letters && endChars <= firstLetterAscii))) {
            "Parameter end (" + endChars + ") must be greater then (" + zeroDigitAscii + ") for generating digits " +
                    "or greater then (" + firstLetterAscii + ") for generating letters."
        }

        val gap = endChars - startChars
        return generateResult(count, chars, gap, startChars, letters, numbers).toString()
    }

    private fun generateResult(countChars: Int, chars: CharArray?, gap: Int, startChars: Int, letters: Boolean, numbers: Boolean): StringBuilder {
        var countChars1 = countChars
        val builder = StringBuilder(countChars1)

        loop@ while (countChars1-- != 0) {
            val codePoint: Int = getNextCodePoint(chars, gap, startChars)

            val numberOfChars = Character.charCount(codePoint)
            if (countChars1 == 0 && numberOfChars > 1) {
                countChars1++
                continue
            }

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
        return if (chars == null) {
            RANDOM.nextInt(gap) + startChars
        } else {
            chars[RANDOM.nextInt(gap) + startChars].toInt()
        }
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
            require(endChars1 > startChars1) { "Parameter end ($endChars1) must be greater than start ($startChars1)" }
        }
        return Pair(startChars1, endChars1)
    }
}
