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
package krandom.utils.apache

import java.util.*

/**
 *
 * Operations for random `String`s.
 *
 * Currently *private high surrogate* characters are ignored.
 * These are Unicode characters that fall between the values 56192 (db80)
 * and 56319 (dbff) as we don't know how to handle them.
 * High and low surrogates are correctly dealt with - that is if a
 * high surrogate is randomly chosen, 55296 (d800) to 56191 (db7f)
 * then it is followed by a low surrogate. If a low surrogate is chosen,
 * 56320 (dc00) to 57343 (dfff) then it is placed after a randomly
 * chosen high surrogate.
 *
 * RandomStringUtils is intended for simple use cases. For more advanced
 * use cases consider using commons-text
 * [
 * RandomStringGenerator](https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/RandomStringGenerator.html) instead.
 *
 *
 * Caveat: Instances of [Random], upon which the implementation of this
 * class relies, are not cryptographically secure.
 *
 *
 * Please note that the Apache Commons project provides a component
 * dedicated to pseudo-random number generation, namely
 * [Commons RNG](https://commons.apache.org/rng), that may be
 * a better choice for applications with more stringent requirements
 * (performance and/or correctness).
 *
 *
 * #ThreadSafe#
 * @since 1.0
 */
/**
 *
 * `RandomStringUtils` instances should NOT be constructed in
 * standard programming. Instead, the class should be used as
 * `RandomStringUtils.random(5);`.
 *
 *
 * This constructor is public to permit tools that require a JavaBean instance
 * to operate.
 */
object RandomStringUtils {

    /**
     *
     * Random object used by random method. This has to be not local
     * to the random method so as to not return the same value in the
     * same millisecond.
     */
    private val RANDOM = Random()

    /**
     *
     * Creates a random string whose length is the number of characters
     * specified.
     *
     *
     * Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.
     *
     * @param count  the length of random string to create
     * @param letters  if `true`, generated string may include
     * alphabetic characters
     * @param numbers  if `true`, generated string may include
     * numeric characters
     * @return the random string
     */
    fun random(count: Int, letters: Boolean = false, numbers: Boolean = false): String {
        return random(count, 0, 0, letters, numbers)
    }

    /**
     *
     * Creates a random string based on a variety of options, using
     * default source of randomness.
     *
     *
     * This method has exactly the same semantics as
     * [.random], but
     * instead of using an externally supplied source of randomness, it uses
     * the internal static [Random] instance.
     *
     * @param count  the length of random string to create
     * @param start  the position in set of chars to start at
     * @param end  the position in set of chars to end before
     * @param letters  only allow letters?
     * @param numbers  only allow numbers?
     * @param chars  the set of chars to choose randoms from.
     * If `null`, then it will use the set of all chars.
     * @return the random string
     * @throws ArrayIndexOutOfBoundsException if there are not
     * `(end - start) + 1` characters in the set array.
     */
    fun random(count: Int, start: Int, end: Int, letters: Boolean, numbers: Boolean, vararg chars: Char): String {
        return random(count, start, end, letters, numbers, chars, RANDOM)
    }

    /**
     *
     * Creates a random string based on a variety of options, using
     * supplied source of randomness.
     *
     *
     * If start and end are both `0`, start and end are set
     * to `' '` and `'z'`, the ASCII printable
     * characters, will be used, unless letters and numbers are both
     * `false`, in which case, start and end are set to
     * `0` and [Character.MAX_CODE_POINT].
     *
     *
     * If set is not `null`, characters between start and
     * end are chosen.
     *
     *
     * This method accepts a user-supplied [Random]
     * instance to use as a source of randomness. By seeding a single
     * [Random] instance with a fixed seed and using it for each call,
     * the same random sequence of strings can be generated repeatedly
     * and predictably.
     *
     * @param count  the length of random string to create
     * @param start  the position in set of chars to start at (inclusive)
     * @param end  the position in set of chars to end before (exclusive)
     * @param letters  only allow letters?
     * @param numbers  only allow numbers?
     * @param chars  the set of chars to choose randoms from, must not be empty.
     * If `null`, then it will use the set of all chars.
     * @param random  a source of randomness.
     * @return the random string
     * @throws ArrayIndexOutOfBoundsException if there are not
     * `(end - start) + 1` characters in the set array.
     * @throws IllegalArgumentException if `count` &lt; 0 or the provided chars array is empty.
     * @since 2.0
     */
    fun random(count: Int, start: Int, end: Int, letters: Boolean, numbers: Boolean,
               chars: CharArray = CharArray(0),
               random: Random = RANDOM): String {
        var countChars = count
        var startChar = start
        var endChar = end
        require(chars.isNotEmpty()) { "The chars array must not be empty" }

        if (startChar == 0 && endChar == 0) {
            if (chars.isNotEmpty()) {
                endChar = chars.size
            } else {
                if (!letters && !numbers) {
                    endChar = Character.MAX_CODE_POINT
                } else {
                    endChar = 'z'.toInt() + 1
                    startChar = ' '.toInt()
                }
            }
        } else {
            require(endChar > startChar) { "Parameter end ($endChar) must be greater than start ($startChar)" }
        }

        val zeroDigitAscii = 48
        val firstLetterAscii = 65

        require(!(chars.isEmpty() && (numbers && endChar <= zeroDigitAscii || letters && endChar <= firstLetterAscii))) {
            "Parameter end (" + endChar + ") must be greater then (" + zeroDigitAscii + ") for generating digits " +
                "or greater then (" + firstLetterAscii + ") for generating letters."
        }

        val builder = StringBuilder(countChars)
        val gap = endChar - startChar

        loop@ while (countChars-- != 0) {
            val codePoint: Int = if (chars.isEmpty()) {
                random.nextInt(gap) + startChar
            } else {
                chars[random.nextInt(gap) + startChar].toInt()
            }

            val numberOfChars = Character.charCount(codePoint)
            if (countChars == 0 && numberOfChars > 1) {
                countChars++
                continue
            }

            countChars = isRightChar(letters, codePoint, numbers, builder, numberOfChars, countChars)
        }
        return builder.toString()
    }

    private fun isRightChar(letters: Boolean, codePoint: Int, numbers: Boolean, builder: StringBuilder, numberOfChars: Int, countChars: Int): Int {
        var countChars1 = countChars
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
        return countChars1
    }
}
