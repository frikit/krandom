/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.utils.apache.RandomStringUtils
import java.security.SecureRandom
import java.util.*

open class Randomizer : KRandomCommon {

    private val random: Random by lazy { SecureRandom() }

    //double
    /**
     * This return standard implementation of Random.class().toDouble() in java
     * @return Double in range >=0 and <1
     */
    override fun randomDouble(): Double {
        return random.nextDouble()
    }

    override fun randomDouble(rangeTo: ClosedRange<Double>): Double {
        return randomDouble(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomDouble(start: Double, end: Double): Double {
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return random.doubles(1, startElem, endElem).findFirst().orElse(999.99)
    }

    //float
    override fun randomFloat(): Float {
        return random.nextFloat()
    }

    override fun randomFloat(rangeTo: ClosedRange<Float>): Float {
        return randomFloat(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomFloat(start: Float, end: Float): Float {
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return (startElem + (endElem - startElem) * randomDouble()).toFloat()
    }

    //long
    override fun randomLong(): Long {
        return random.nextLong()
    }

    override fun randomLong(rangeTo: ClosedRange<Long>): Long {
        return randomLong(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomLong(start: Long, end: Long): Long {
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return random.longs(1, startElem, endElem).findFirst().orElse(999L)
    }

    //byte
    override fun randomByte(): Byte = random.nextInt().toByte()

    override fun randomByte(rangeTo: ClosedRange<Byte>): Byte = randomByte(rangeTo.start, rangeTo.endInclusive)

    override fun randomByte(start: Byte, end: Byte): Byte {
        val (startElem, endElem) = normalizeStartEnd(start, end)
        return random.ints(1, startElem.toInt(), endElem.toInt()).findFirst().orElse(0).toByte()
    }

    //short
    override fun randomShort(): Short = random.nextInt().toShort()

    override fun randomShort(rangeTo: ClosedRange<Short>): Short = randomShort(rangeTo.start, rangeTo.endInclusive)

    override fun randomShort(start: Short, end: Short): Short {
        val (startElem, endElem) = normalizeStartEnd(start, end)
        return random.ints(1, startElem.toInt(), endElem.toInt()).findFirst().orElse(0).toShort()
    }

    //int
    override fun randomInt(): Int {
        return random.nextInt()
    }

    override fun randomInt(rangeTo: ClosedRange<Int>): Int {
        return randomInt(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomInt(start: Int, end: Int): Int {
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return random.ints(1, startElem, endElem).findFirst().orElse(999)
    }

    //char
    override fun randomChar(upperLetters: Boolean,
                            lowerLetters: Boolean,
                            numbers: Boolean,
                            specialCharacters: Boolean): Char {
        val combinations: List<Pair<Int, Int>> = getCharCombinations(upperLetters, lowerLetters, numbers, specialCharacters)
        val chooseWhich: Int = chooseWhichIndex(combinations)

        val pair = combinations[chooseWhich]

        val number = randomInt(pair.first, pair.second)
        return number.toChar()
    }

    //boolean
    override fun randomBoolean(): Boolean {
        return random.nextBoolean()
    }

    //string
    override fun randomString(length: Int, specialCharacters: Boolean, numbers: Boolean): String {
        require(length >= 1) { "Length can't be < 1" }

        val function: () -> String = { RandomStringUtils.random(length, 0, 0, true, numbers) }
        return RandomStringUtils.generateRandomString(function, numbers)
    }

    private fun getCharCombinations(upperLetters: Boolean,
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

    private fun <T : Comparable<T>> normalizeStartEnd(start: T, end: T): Pair<T, T> {
        require(start != end) {
            "Illegal argument passed start = $start and end = $end, they should be different!"
        }

        //swap val's to act easier
        return if (start > end) {
            Pair(end, start)
        } else {
            Pair(start, end)
        }
    }

    private fun chooseWhichIndex(combinations: List<Pair<Int, Int>>, index: Int = 0): Int {
        if (combinations.isEmpty())
            throw IllegalArgumentException("combinations can't be empty!")

        if (index > 99)
            throw IllegalArgumentException("Index can't be > 99, it fail to generate right index from combinations!")

        val chooseWhich: Int = chooseWhichOne(combinations)

        try {
            if (chooseWhich < 0 && chooseWhich >= combinations.size) {
                return chooseWhichIndex(combinations, index + 1)
            }
        } catch (exp: IllegalArgumentException) {
            //throw new exception don't keep full method call stack
            throw IllegalArgumentException("Index can't be > 99, it fail to generate right index from combinations!")
        }

        return chooseWhich
    }

    private fun chooseWhichOne(combinations: List<Pair<Int, Int>>): Int =
            if (combinations.size == 1) {
                0
            } else {
                randomInt(0, combinations.size)
            }
}
